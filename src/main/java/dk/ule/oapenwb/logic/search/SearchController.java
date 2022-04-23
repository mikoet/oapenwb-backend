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
import dk.ule.oapenwb.logic.admin.LangPairsController;
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
		Long sememeOneID;
		Long sememeTwoID;
		short weight;
	}

	private final AppConfig appConfig;
	// TODO ENHANCE Maybe create a read-only interface
	private final LangPairsController langPairsController;
	private final ControllerSet lemmaControllers;
	private final VariantController variantsController = new VariantController();

	@Inject
	public SearchController(
		AppConfig appConfig,
		LangPairsController langPairsController,
		ControllerSet lemmaControllers)
	{
		this.appConfig = appConfig;
		this.langPairsController = langPairsController;
		this.lemmaControllers = lemmaControllers;
	}

	public ResultObject find(final QueryObject request) throws CodeException
	{
		ResultObject result = new ResultObject();
		try {
			if (appConfig.isVerbose()) {
				LOG.info(String.format("New search request with data: pair = %s, direction = %s, term = %s",
					request.getPair(), request.getDirection(), request.getTerm()));

				TimeUtil.startTimeMeasure();
			}

			// Get the LangPair, or throw an exception
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

			// Query the Mappings
			List<MappingResult> mappingsList = new LinkedList<>();
			Map<Long, Sememe> sememesMap = new HashMap<>();

			NativeQuery<?> mappingsQuery = createMappingsSearchQuery(request, langPair);
			List<Object[]> mappingsRows = HibernateUtil.listAndCast(mappingsQuery);
			for (Object[] row : mappingsRows) {
				MappingResult mappingResult = new MappingResult(
					(Long) row[0],    // sememeOneID
					(Long) row[1],    // sememeTwoID
					(short) row[2]    // weight
				);
				mappingsList.add(mappingResult);
				// Add all sememeIDs to the sememesMap for later loading of the sememes
				sememesMap.put(mappingResult.sememeOneID, null);
				sememesMap.put(mappingResult.sememeTwoID, null);

				if (appConfig.isVerbose())
					LOG.info(String.format("Mapping with sememeOne %d, sememeTwo %d and weight %d",
						mappingResult.sememeOneID, mappingResult.sememeTwoID, mappingResult.weight));
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

				// Load all variants of the just loaded sememes
				Set<Long> variantIDs = new HashSet<>();
				for (Sememe sememe : sememes) {
					if (appConfig.isVerbose()) {
						LOG.info(String.format("Sememe with id %d", sememe.getId()));
					}
					// Replace the null value in the sememesMap and collect all variant IDs
					sememesMap.put(sememe.getId(), sememe);
					variantIDs.addAll(sememe.getVariantIDs());
				}
				HashMap<Long, Variant> allVariantsMap = PresentationBuilder.variantListToHashMap(
					variantsController.loadByIDs(variantIDs));

				// Build and fill the resultEntryList
				WholeLemmaBuilder lemmaBuilder = new WholeLemmaBuilder();
				List<ResultObject.ResultEntry> resultEntryList = new LinkedList<>();
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

					ResultObject.ResultEntry entry = new ResultObject.ResultEntry();
					// SememeEntry 1
					entry.sememeOne = new ResultObject.SememeEntry();
					entry.sememeOne.lemma = lemmaBuilder.build(
						PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, lemmaControllers, sememeOne, allVariantsMap);
					// SememeEntry 2
					entry.sememeTwo = new ResultObject.SememeEntry();
					entry.sememeTwo.lemma = lemmaBuilder.build(
						PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, lemmaControllers, sememeTwo, allVariantsMap);
					// Further properties
					entry.typeID = 0; // TODO How to get the typeID(s)? They are stored on the lexemes.
					entry.weight = mappingResult.weight;

					// Add entry to resultEntryList
					resultEntryList.add(entry);
				}
				result.setEntries(resultEntryList);
			}

			if (appConfig.isVerbose()) {
				LOG.info(String.format("Search request took %d ms", TimeUtil.durationInMilis()));
			}

			// TODO Create and store SearchRun instance
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		}
		return result;
	}

	private NativeQuery<?> createMappingsSearchQuery(final QueryObject request, final LangPair langPair)
	{
		StringBuilder sb = new StringBuilder();

		// Basis query (Q500)
		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Right) {
			// First part of possible union for left to right search
			sb.append("select sememeOneID, sememeTwoID, weight\n");
			sb.append("from Mappings\n");
			sb.append("where sememeOneID in (\n");
			sb.append("\tselect s.id from Sememes s inner join Lexemes l on s.lexemeID = l.id,\n");
			sb.append("\t\tjsonb_array_elements(s.variantIDs) va(variantID)\n");
			sb.append("\twhere l.langID = :langOneID and variantID\n");
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
			sb.append("select sememeOneID, sememeTwoID, weight\n");
			sb.append("from Mappings\n");
			sb.append("where sememeTwoID in (\n");
			sb.append("\tselect s.id from Sememes s inner join Lexemes l on s.lexemeID = l.id,\n");
			sb.append("\t\tjsonb_array_elements(s.variantIDs) va(variantID)\n");
			sb.append("\twhere l.langID = :langTwoID and variantID");
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
			.addScalar("sememeOneID", new LongType())
			.addScalar("sememeTwoID", new LongType())
			.addScalar("weight", new ShortType());

		// Set the parameters
		query.setParameter("term", request.getTerm());

		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Right) {
			query.setParameter("langOneID", langPair.getLangOneID());
		}
		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Left) {
			query.setParameter("langTwoID", langPair.getLangTwoID());
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
	public ResultObject executeQuery(QueryObject queryData) throws Exception
	{
		ResultObject result = new ResultObject();

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


	private boolean checkQueryObject(QueryObject queryData) {
		// TODO implement the checks here
		return true;
	}

}
