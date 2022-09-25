// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.entity.ui.UiResultCategory;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.logic.admin.LexemeTypesController;
import dk.ule.oapenwb.logic.admin.UiResultCategoriesController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.logic.admin.lexeme.VariantController;
import dk.ule.oapenwb.logic.presentation.ControllerSet;
import dk.ule.oapenwb.logic.presentation.PresentationBuilder;
import dk.ule.oapenwb.logic.presentation.WholeLemmaBuilder;
import dk.ule.oapenwb.logic.presentation.options.PresentationOptions;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.TimeUtil;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.LongType;
import org.hibernate.type.ShortType;
import org.hibernate.type.StringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>SearchController in an early stadium.</p>
 *
 * TODO REFACT
 */
@Singleton
public class SearchController
{
	private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

	@AllArgsConstructor
	private static class MappingResult
	{
		String langPair;
		Long sememeOneID;
		Long sememeTwoID;
		short weight;
	}

	private final AppConfig appConfig;
	private final LexemesController lexemesController;
	private final LexemeTypesController lexemeTypesController;
	private final LangPairsController langPairsController;
	private final UiResultCategoriesController uiResultCategoriesController;
	private final ControllerSet lemmaControllers;
	private final VariantController variantsController = new VariantController();

	@Inject
	public SearchController(
		AppConfig appConfig,
		LexemesController lexemesController,
		LexemeTypesController lexemeTypesController,
		LangPairsController langPairsController,
		UiResultCategoriesController uiResultCategoriesController,
		ControllerSet lemmaControllers)
	{
		this.appConfig = appConfig;
		this.lexemesController = lexemesController;
		this.lexemeTypesController = lexemeTypesController;
		this.langPairsController = langPairsController;
		this.uiResultCategoriesController = uiResultCategoriesController;
		this.lemmaControllers = lemmaControllers;
	}

	/**
	 * TODO Has to look at active properties of the several entities!
	 *
	 * @param request
	 * @return
	 * @throws CodeException
	 */
	public SearchResult find(final SearchRequest request) throws CodeException
	{
		SearchResult result = new SearchResult();
		try {
			if (appConfig.isVerbose()) {
				LOG.info(String.format("New search request with data: pair = %s, direction = %s, term = %s",
					request.getPair(), request.getDirection(), request.getTerm()));
			}

			TimeUtil.startTimeMeasure();

			List<LangPair> langPairs = new ArrayList<>(10);
			if (request.getPair().equals("nds-*")) {
				// Special case 'nds-*'
				for (LangPair langPair : this.langPairsController.list()) {
					if (langPair.getId().startsWith("nds-")) {
						langPairs.add(langPair);
					}
				}
			} else {
				// Get the single LangPair, or throw an exception
				LangPair langPair = this.langPairsController.get(request.getPair());
				if (langPair == null) {
					throw new CodeException(ErrorCode.Search_QueryParameterInvalid,
						Arrays.asList(new Pair<>("param", "pair"),
							new Pair<>("value", request.getPair())));
				}
				if (appConfig.isVerbose()) {
					LOG.info(String.format("Lang pair loaded with ID %s, langOneID = %d, langTwoID = %d",
						langPair.getId(), langPair.getLangOneID(), langPair.getLangTwoID()));
				}
				langPairs.add(langPair);
			}

			// Query the Mappings
			List<MappingResult> mappingsList = new LinkedList<>();
			Map<Long, Sememe> sememesMap = new HashMap<>();

			NativeQuery<?> mappingsQuery = createMappingsSearchQuery(request, langPairs);
			List<Object[]> mappingsRows = HibernateUtil.listAndCast(mappingsQuery);
			for (Object[] row : mappingsRows) {
				MappingResult mappingResult = new MappingResult(
					(String) row[0],  // langPair ID
					(Long) row[1],    // sememeOneID
					(Long) row[2],    // sememeTwoID
					(short) row[3]    // weight
				);
				mappingsList.add(mappingResult);
				// Add all sememeIDs to the sememesMap for later loading of the sememes
				sememesMap.put(mappingResult.sememeOneID, null);
				sememesMap.put(mappingResult.sememeTwoID, null);

				if (appConfig.isVerbose()) {
					LOG.info(String.format("Mapping with sememeOne %d, sememeTwo %d and weight %d",
						mappingResult.sememeOneID, mappingResult.sememeTwoID, mappingResult.weight));
				}
			}

			if (appConfig.isVerbose()) {
				LOG.info(String.format("Number of mappings found: %d", mappingsList.size()));
			}

			if (mappingsList.size() > 0) {
				// Query the sememes
				Session session = HibernateUtil.getSession();
				Query<Sememe> sememesQuery = session.createQuery("FROM Sememe s WHERE s.id IN (:sememeIDs)",
					Sememe.class);
				sememesQuery.setParameterList("sememeIDs", sememesMap.keySet());
				List<Sememe> sememes = sememesQuery.list();

				// Catch all lexemeIDs to load essential data like the language and lexemeType of each sememe
				Set<Long> lexemeIDs = new HashSet<>();
				// Load all variants of the just loaded sememes
				Set<Long> variantIDs = new HashSet<>();
				for (Sememe sememe : sememes) {
					if (appConfig.isVerbose()) {
						LOG.info(String.format("Sememe with id %d", sememe.getId()));
					}
					// Replace the null value in the sememesMap and collect all lexemeIDs and variantIDs
					sememesMap.put(sememe.getId(), sememe);
					lexemeIDs.add(sememe.getLexemeID());
					variantIDs.addAll(sememe.getVariantIDs());
				}
				HashMap<Long, Variant> allVariantsMap = PresentationBuilder.variantListToHashMap(
					variantsController.loadByIDs(variantIDs));

				// Load the lexemes and transfer into hashmap
				List<Lexeme> lexemes = lexemesController.loadByIDs(lexemeIDs);
				HashMap<Long, Lexeme> lexemesMap = new HashMap<>(lexemes.size());
				for (Lexeme lexeme : lexemes) {
					lexemesMap.put(lexeme.getId(), lexeme);
				}

				// Build and fill the resultEntryList
				WholeLemmaBuilder lemmaBuilder = new WholeLemmaBuilder();
				// <resultCategory ID, SearchResult.ResultCategory>
				Map<Integer, SearchResult.ResultCategory> resultMap = new HashMap<>();
				for (MappingResult mappingResult : mappingsList) {
					Sememe sememeOne = sememesMap.get(mappingResult.sememeOneID);
					Sememe sememeTwo = sememesMap.get(mappingResult.sememeTwoID);

					if (sememeOne == null || sememeTwo == null) {
						LOG.error(String.format("Sememe with ID %d could not be loaded",
							sememeOne == null ? mappingResult.sememeOneID : mappingResult.sememeTwoID));
						LOG.error(String.format("Request data: pair = %s, direction = %s, term = %s",
							request.getPair(), request.getDirection(), request.getTerm()));
						continue;
					}

					Lexeme lexemeOne = lexemesMap.get(sememeOne.getLexemeID());
					Lexeme lexemeTwo = lexemesMap.get(sememeTwo.getLexemeID());

					if (lexemeOne == null || lexemeTwo == null) {
						LOG.error(String.format("Lexeme with ID %d could not be loaded",
							lexemeOne == null ? sememeOne.getLexemeID() : sememeTwo.getLexemeID()));
						LOG.error(String.format("Request data: pair = %s, direction = %s, term = %s",
							request.getPair(), request.getDirection(), request.getTerm()));
						continue;
					}

					LangPair langPair = null;
					if (langPairs.size() > 1) {
						langPair = langPairsController.get(mappingResult.langPair);
					}

					SearchResult.ResultEntry entry = new SearchResult.ResultEntry();
					// SememeEntry 1
					entry.sememeOne = new SearchResult.SememeEntry();
					entry.sememeOne.typeID = lexemeOne.getTypeID();
					entry.sememeOne.lemma = lemmaBuilder.build(
						PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, lemmaControllers, sememeOne, allVariantsMap);
					if (langPair != null) {
						entry.sememeOne.locale = Optional.of(langPair.getLangOne().getLocale());
					}
					// SememeEntry 2
					entry.sememeTwo = new SearchResult.SememeEntry();
					entry.sememeTwo.typeID = lexemeTwo.getTypeID();
					entry.sememeTwo.lemma = lemmaBuilder.build(
						PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, lemmaControllers, sememeTwo, allVariantsMap);
					if (langPair != null) {
						entry.sememeTwo.locale = Optional.of(langPair.getLangTwo().getLocale());
					}
					// Further properties
					entry.weight = mappingResult.weight;

					// Add entry to resultMap
					UiResultCategory uiResultCategory = uiResultCategoriesController.get(
						lexemeTypesController.get(lexemeOne.getTypeID()).getUiCategoryID());
					SearchResult.ResultCategory resultCategory = resultMap.computeIfAbsent(uiResultCategory.getId(),
						k -> new SearchResult.ResultCategory(uiResultCategory.getUitID()));
					resultCategory.totalWeight += entry.weight;
					resultCategory.entries.add(entry);
				}

				//
				List<SearchResult.ResultCategory> resultList = new ArrayList<>(resultMap.size());
				resultList.addAll(resultMap.values());
				resultList.sort((o1, o2) -> o2.totalWeight - o1.totalWeight);

				result.setEntries(resultList);
			}

			long duration = TimeUtil.durationInMilis();
			if (appConfig.isVerbose()) {
				LOG.info(String.format("Search request took %d ms", duration));
			}

			// TODO Create and store SearchRun instance
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		}
		return result;
	}

	private NativeQuery<?> createMappingsSearchQuery(final SearchRequest request, final List<LangPair> langPairs)
	{
		StringBuilder sb = new StringBuilder();

		// Basis query (Q500)
		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Right) {
			// First part of possible union for left to right search
			sb.append("select langPair, sememeOneID, sememeTwoID, weight\n");
			sb.append("from Mappings m\n");
			sb.append("where m.langPair in (:langPairs) and sememeOneID in (\n");
			sb.append("\tselect s.id from Sememes s inner join Lexemes l on s.lexemeID = l.id,\n");
			sb.append("\t\tjsonb_array_elements(s.variantIDs) va(variantID)\n");
			sb.append("\twhere l.langID in (:langOneIDs) and variantID");
			sb.append(HibernateUtil.CONSTANT_INT); // escape the :: (!)
			sb.append(" in (\n"); // escape the :: (!)
			sb.append("\t\tselect variantID from LexemeForms\n");
			sb.append("\t\t\twhere searchableText @@ websearch_to_tsquery('simple', :term)\n");
			sb.append("\t)\n");
			sb.append(")\n");
		}

		// Only needed when both directions are taken into account
		if (request.getDirection() == Direction.Both) {
			sb.append("union\n");
		}

		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Left) {
			// Second part of possible union for right to left search
			sb.append("select langPair, sememeOneID, sememeTwoID, weight\n");
			sb.append("from Mappings m\n");
			sb.append("where m.langPair in (:langPairs) and sememeTwoID in (\n");
			sb.append("\tselect s.id from Sememes s inner join Lexemes l on s.lexemeID = l.id,\n");
			sb.append("\t\tjsonb_array_elements(s.variantIDs) va(variantID)\n");
			sb.append("\twhere l.langID in (:langTwoIDs) and variantID");
			sb.append(HibernateUtil.CONSTANT_INT); // escape the :: (!)
			sb.append(" in (\n");
			sb.append("\t\tselect variantID from LexemeForms\n");
			sb.append("\t\t\twhere searchableText @@ websearch_to_tsquery('simple', :term)\n");
			sb.append("\t)\n");
			sb.append(")\n");
		}

		// Ordering by weight is applied always
		sb.append("order by weight desc");

		// TODO limit, offset
		// TODO Also see SearchController.createSynGroupQuery() for creation of filter statement

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<?> query = session.createSQLQuery(sb.toString())
			.addScalar("langPair", new StringType())
			.addScalar("sememeOneID", new LongType())
			.addScalar("sememeTwoID", new LongType())
			.addScalar("weight", new ShortType());

		// -- Set the parameters
		query.setParameter("term", request.getTerm());

		String[] langPairIDs = new String[langPairs.size()];
		for (int i = 0; i < langPairs.size(); i++) {
			langPairIDs[i] = langPairs.get(i).getId();
		}
		query.setParameterList("langPairs", langPairIDs);

		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Right) {
			Set<Integer> langIDs = new HashSet<>(langPairs.size());
			for (int i = 0; i < langPairs.size(); i++) {
				langIDs.add(langPairs.get(i).getLangOneID());
			}
			query.setParameterList("langOneIDs", langIDs);
		}
		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Left) {
			Set<Integer> langIDs = new HashSet<>(langPairs.size());
			for (int i = 0; i < langPairs.size(); i++) {
				langIDs.add(langPairs.get(i).getLangTwoID());
			}
			query.setParameter("langTwoIDs", langIDs);
		}

		//query.setParameter("offset", 0);
		//query.setParameter("limit", TODO);

		return query;
	}



	// ------ Olden kråm -------

	/**
	 * Path: /s/:pair/:str?d=0&o=1…
	 *
	 * @throws Exception
	 */
	public SearchResult executeQuery(SearchRequest queryData) throws Exception
	{
		SearchResult result = new SearchResult();

		TimeUtil.startTimeMeasure();

		/* Check the query data for consistency */
		if (queryData == null) {
			throw new CodeException(ErrorCode.Search_NoQueryData);
		}
		if (!this.checkQueryObject(queryData)) {
			throw new CodeException(ErrorCode.Search_QueryDataInconsistent);
		}

		// ...

		// TODO Create and store SearchRun instance

		return result;
	}

	public void getSuggestions() throws Exception {
	}


	private boolean checkQueryObject(SearchRequest queryData) {
		// TODO implement the checks here
		return true;
	}

}
