// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search.autocomplete;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.persistency.entity.content.basedata.LangPair;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
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
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;
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
			checkTerm(request);

			final List<LangPair> langPairs = SearchController.getLangPairList(this.langPairsController, request.getPair());
			final Set<Integer> langIDs = getLangIDs(langPairs, request.getDirection());

			final int maxResults = Math.min(MAX_RESULTS, request.getMaxResults());
			final Set<Long> variantIDs = new HashSet<>(maxResults);
			final List<ACSearchResult.VariantEntry> resultList = new LinkedList<>();

			// 1) Query for the variants (sparse data) that match the search request
			final NativeQuery<AutoCompleteRow> query = createQuery(request, langIDs, maxResults);
			final List<AutoCompleteRow> autoCompleteRows = query.list();

			for (final AutoCompleteRow autoCompleteRow : autoCompleteRows) {
				// Catch all IDs of the variants to load
				variantIDs.add(autoCompleteRow.variantID);
			}

			// 2) Query the variants
			final Session session = HibernateUtil.getSession();
			final Query<Variant> variantsQuery = session.createQuery("FROM Variant e WHERE e.id IN (:variantIDs)",
				Variant.class);
			variantsQuery.setParameterList("variantIDs", variantIDs);
			final List<Variant> variants = variantsQuery.list();

			final Map<Long, Variant> variantsMap = new HashMap<>(variants.size());
			for (final Variant variant : variants) {
				variantsMap.put(variant.getId(), variant);
			}

			// 3) Build the result
			for (final AutoCompleteRow row : autoCompleteRows) {
				final ACSearchResult.VariantEntry entry = new ACSearchResult.VariantEntry();

				entry.setTypeID(row.typeID);

				final Integer langID = row.langID;
				if (langID != null) {
					Language language = lemmaControllers.getLanguagesController().get(langID);
					if (language != null) {
						entry.setLocale(language.getLocale());
					}
				}

				final Variant variant = variantsMap.get(row.variantID);
				final String lemma = singleLemmaBuilder.buildLemmaWithOrthographyOnly(
					PresentationOptions.DEFAULT_PRESENTATION_OPTIONS, lemmaControllers, variant);
				entry.setLemma(lemma);
				// TODO In a later stage (when the lemmata are seperately stored) the searchWord should – maybe –
				//  rathrr be the whole lemma in quotes instead of just the main part.
				entry.setSearchWord(variant.getLemma().getMain());

				resultList.add(entry);
			}
			result.setEntries(resultList);

			final long duration = TimeUtil.durationInMilis();
			if (appConfig.isVerbose() && duration > 80) {
				LOG.info(String.format("Auto completion request took >100 ms: %d ms", duration));
			}
		} catch (Exception e) {
			LOG.error("Error building autocompletion result", e);
			throw new CodeException(ErrorCode.Autocomplete_OperationFailed,
				List.of(new Pair<>("error", e.getMessage())));
		}
		return result;
	}

	private void checkTerm(final ACSearchRequest request) {
		final int quotesCount = StringUtils.countMatches(request.getTerm(), '\'');
		if (quotesCount % 2 == 1) {
			request.setTerm(request.getTerm().replace("'", ""));
		}
	}

	/*
	 * Result is made off of:
	 * - variantID, Long
	 * - typeID, Integer
	 * - langID, Integer
	 * - sememeID, Long
	 */
	private NativeQuery<AutoCompleteRow> createQuery(final ACSearchRequest request, final Set<Integer> langIDs, int limit)
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
			sb.append("    where Vi.id in (select variantID from LexemeForms where ")
				.append(filterStatement)
				.append("))\n");
		}
		sb.append("  and L.langID in (:langIDs)\n");

		sb.append("  and V.id = ( select min(Vi.id) from Variants Vi\n");
		sb.append("    where (Vi.pre=V.pre or (Vi.pre is null and V.pre is null)) and\n");
		sb.append("      Vi.main=V.main and\n");
		sb.append("      (Vi.post=V.post or (Vi.post is null and V.post is null))\n");

		/* If this part is included doublets with the same lemma can appear in the autocompletion
		   result, but those dubblets would then have different attributes like lexemeID, thus
		   belonging to different lexemes with e.g. different typeIDs.
		   It'd be better to use this in future. */
		//sb.append("      and Vi.lexemeID = V.lexemeID\n");
		sb.append("  )\n");

		// Add the order clause and the paging data
		sb.append("order by V.main\n");
		sb.append("limit :limit offset :offset");

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<AutoCompleteRow> query = session.createNativeQuery(sb.toString(), AutoCompleteRow.class)
			.addScalar("id", StandardBasicTypes.LONG)
			.addScalar("typeID", StandardBasicTypes.INTEGER)
			.addScalar("langID", StandardBasicTypes.INTEGER)
			.addScalar("sememeID", StandardBasicTypes.LONG);

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

	private record AutoCompleteRow(
		Long variantID,
		Integer typeID,
		Integer langID,
		Long sememeID
	) {}
}
