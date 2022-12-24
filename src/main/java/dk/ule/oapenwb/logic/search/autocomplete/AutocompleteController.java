// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search.autocomplete;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.logic.admin.lexeme.TextSearchType;
import dk.ule.oapenwb.logic.presentation.ControllerSet;
import dk.ule.oapenwb.logic.presentation.SingleLemmaBuilder;
import dk.ule.oapenwb.logic.presentation.options.PresentationOptions;
import dk.ule.oapenwb.logic.search.Direction;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.TimeUtil;
import lombok.AllArgsConstructor;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.IntegerType;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Singleton
public class AutocompleteController
{
	private static final Logger LOG = LoggerFactory.getLogger(AutocompleteController.class);
	private static final int MAX_RESULTS = 10;
	private final SingleLemmaBuilder singleLemmaBuilder = new SingleLemmaBuilder();

	private final AppConfig appConfig;
	//private final LexemeTypesController lexemeTypesController; // TODO Later use for resolving the lexemeTypes name?
	private final LangPairsController langPairsController;
	private final ControllerSet lemmaControllers;

	@Inject
	public AutocompleteController(
		AppConfig appConfig,
		//LexemeTypesController lexemeTypesController,
		LangPairsController langPairsController,
		ControllerSet lemmaControllers
	) {
		this.appConfig = appConfig;
		this.langPairsController = langPairsController;
		this.lemmaControllers = lemmaControllers;
	}

	public ACSearchResult autocomplete(final ACSearchRequest request) throws CodeException
	{
		ACSearchResult result = new ACSearchResult();

		if (request.getTerm() == null || request.getTerm().isBlank() || request.getPair() == null
			|| request.getPair().isBlank() || request.getDirection() == null)
		{
			return result;
		}

		try {
			TimeUtil.startTimeMeasure();

			final List<LangPair> langPairs = SearchController.getLangPairList(this.langPairsController, request.getPair());
			final Set<Integer> langIDs = getLangIDs(langPairs, request.getDirection());

			final int maxResults = Math.min(MAX_RESULTS, request.getMaxResults());
			final List<ResultSet> resultSets = new LinkedList<>();
			final Set<Long> variantIDs = new HashSet<>(maxResults);
			final List<ACSearchResult.VariantEntry> resultList = new LinkedList<>();

			// 1) Query for the variants (sparse data) that match the search request
			NativeQuery<?> query = createQuery(request, langIDs, maxResults);
			List<Object[]> rows = HibernateUtil.listAndCast(query);
			for (Object[] row : rows) {
				final Long variantID = (Long) row[0];
				resultSets.add(new ResultSet(variantID, (Integer) row[1], (Integer) row[2], (Long) row[3]));
				// Catch all IDs of the variants to load
				variantIDs.add(variantID);
			}

			// 2) Query the variants
			Session session = HibernateUtil.getSession();
			Query<Variant> variantsQuery = session.createQuery("FROM Variant e WHERE e.id IN (:variantIDs)",
				Variant.class);
			variantsQuery.setParameterList("variantIDs", variantIDs);
			List<Variant> variants = variantsQuery.list();

			Map<Long, Variant> variantsMap = new HashMap<>(variants.size());
			for (final Variant variant : variants) {
				variantsMap.put(variant.getId(), variant);
			}

			// 3) Build the result
			for (ResultSet rs : resultSets) {
				ACSearchResult.VariantEntry entry = new ACSearchResult.VariantEntry();

				entry.setTypeID(rs.typeID);

				Integer langID = rs.langID;
				if (langID != null) {
					Language language = lemmaControllers.getLanguagesController().get(langID);
					if (language != null) {
						entry.setLocale(language.getLocale());
					}
				}

				final Variant variant = variantsMap.get(rs.variantID);
				final String lemma = singleLemmaBuilder.buildLemmaWithOrthographyOnly(
					PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, lemmaControllers, variant);
				entry.setLemma(lemma);
				// TODO In a later stage (when the lemmata are seperately stored) the searchWord should – maybe –
				//  rathrr be the whole lemma in quotes instead of just the main part.
				entry.setSearchWord(variant.getLemma().getMain());

				resultList.add(entry);
			}
			result.setEntries(resultList);

			long duration = TimeUtil.durationInMilis();
			if (appConfig.isVerbose()) {
				LOG.info(String.format("Auto completion request took %d ms", duration));
			}
		} catch (Exception e) {
			LOG.error("Error building autocompletion result", e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", Variant.class.getSimpleName())));
		}
		return result;
	}

	/*
	 * Result is made off of:
	 * - variantID, Long
	 * - typeID, Integer
	 * - langID, Integer
	 * - sememeID, Long
	 */
	private NativeQuery<?> createQuery(final ACSearchRequest request, final Set<Integer> langIDs, int limit)
	{
		StringBuilder sb = new StringBuilder();

		// Autocomplete query (Q550)
		sb.append("select V.id as id, L.typeID as typeID, L.langID as langID, S.id as sememeID\n");
		sb.append("from Lexemes L left join Variants V on (L.id = V.lexemeID)\n"); // and V.mainVariant=true)\n
		sb.append("  left join Sememes S on (L.id = S.lexemeID AND S.id = (SELECT MIN(lexemeID) FROM Sememes WHERE lexemeID = L.id))\n");
		sb.append("where L.active = true AND V.active = true AND s.active = true\n");

		String filterText = request.getTerm();
		if (filterText != null && !filterText.isEmpty()) {
			final Pair<String, String> filterResult = LexemesController.buildFilterStatementAndText(request.getTerm(),
				Optional.of(TextSearchType.Prefixed));
			final String filterStatement = filterResult.getLeft();
			filterText = filterResult.getRight();
			// Add the text filtering part if it's set
			sb.append("  and L.id in (select lexemeID from Variants Vi\n");
			sb.append("    where Vi.id in (select variantID from LexemeForms where " + filterStatement + "))\n");
		}
		sb.append("  and L.langID in (:langIDs)\n");

		// Add the order clause and the paging data
		sb.append("order by V.main\n");
		sb.append("limit :limit offset :offset");

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<?> query = session.createSQLQuery(sb.toString())
			.addScalar("id", new LongType())
			.addScalar("typeID", new IntegerType())
			.addScalar("langID", new IntegerType())
			.addScalar("sememeID", new LongType());

		query.setParameter("offset", 0);
		query.setParameter("limit", limit);

		// Set the parameters
		if (filterText != null) {
			query.setParameter("filter", filterText);
		}
		query.setParameterList("langIDs", langIDs);

		return query;
	}

	private Set<Integer> getLangIDs(final List<LangPair> langPairs, Direction direction) {
		Set<Integer> result = new HashSet<>();

		for (LangPair pair : langPairs) {
			if (direction == Direction.Both || direction == Direction.Left) {
				result.add(pair.getLangOneID());
			}
			if (direction == Direction.Both || direction == Direction.Right) {
				result.add(pair.getLangTwoID());
			}
		}

		return result;
	}

	@AllArgsConstructor
	private static class ResultSet
	{
		public Long variantID;
		public Integer typeID;
		public Integer langID;
		public Long sememeID;
	}
}
