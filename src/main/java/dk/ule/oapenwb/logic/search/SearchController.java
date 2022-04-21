// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.TimeUtil;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;
import org.hibernate.type.ShortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * <p>SearchController in an early stadium.</p>
 *
 * TODO REFACT
 */
@Singleton
public class SearchController
{
	private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

	// TODO Hm. This was not as designed, eh? Using an admin controller
	//   Maybe they could get a read-only interface.
	private /*final*/ LangPairsController langPairsController;

	public SearchController(/*LangPairsController langPairsController*/)
	{
		//this.langPairsController = langPairsController;
	}

	public ResultObject find(final QueryObject request) throws CodeException
	{
		ResultObject result = new ResultObject();
		try {
			// Query the Mappings
			/*
			List<SynGroupItem> synGroupList = new LinkedList<>();
			NativeQuery<?> synGroupQuery = createSearchQuery(request);
			List<Object[]> synGroupRows = HibernateUtil.listAndCast(synGroupQuery);
			for (Object[] row : synGroupRows) {
				synGroupList.add(new SynGroupItem(
					(Integer) row[0],	// id
					(String) row[1],	// description
					(String) row[2]		// presentation
				));
			}
			 */
			// -- result.setSynGroups(synGroupList);

			/*
			// Query the Sememe / Lexeme data
			List<LexemeSlimPlus> lexemesList = new LinkedList<>();
			NativeQuery<?> lexemesQuery = createLexemesQuery(request);
			List<Object[]> lexemeRows = HibernateUtil.listAndCast(lexemesQuery);
			for (Object[] row : lexemeRows) {
				LexemeSlimPlus lexemeSlimPlus = new LexemeSlimPlus(
					(Long) row[0],		// id
					(String) row[1],	// parserID
					(Long) row[2],		// typeID
					(Integer) row[3],	// langID
					(String) row[4],	// pre
					(String) row[5],	// main
					(String) row[6],	// post
					(Boolean) row[7],	// active
					(Integer) row[8],	// condition
					JsonUtil.convertJsonbStringToLinkedHashSet((String) row[9]) // tags
				);
				lexemesList.add(lexemeSlimPlus);

				// Query the sememes for each lexeme
				Session session = HibernateUtil.getSession();
				Query<Sememe> query = session.createQuery(
					"FROM " + Sememe.class.getSimpleName() + " S WHERE S.lexemeID = :lexemeID ORDER BY S.id ASC", Sememe.class);
				query.setParameter("lexemeID", lexemeSlimPlus.getId());
				lexemeSlimPlus.setSememes(query.list());

			}
			result.setLexemes(lexemesList);
			 */
		} catch (Exception e) {
			LOG.error("Error fetching instances of type " + Lexeme.class.getSimpleName(), e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-ALL"), new Pair<>("entity", Lexeme.class.getSimpleName())));
		}
		return result;
	}

	private NativeQuery<?> createSearchQuery(final QueryObject request)
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
			query.setParameter("langOneID", 1 /* TODO Take from the LangPair */);
		}
		if (request.getDirection() == Direction.Both || request.getDirection() == Direction.Left) {
			query.setParameter("langTwoID", 2 /* TODO Take from the LangPair */);
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

		Session session = HibernateUtil.getSession();
		/* Search the LexemeForms for the search text */

		String sql =
			"SELECT em.sememeOneId, em.sememeTwoId FROM Mapping em " +
			"WHERE" +
			"  em.lexemeOneId IN (SELECT e.id FROM Lexeme e WHERE e.id IN (" +
			"    SELECT ef.lexemeId FROM LexemeForm ef" +
			"    WHERE MATCH (ef.text) AGAINST (:terms IN NATURAL LANGUAGE MODE)" +
			"      AND ef.orthoId IN (:orthosLangOne))" +
			"    AND e.langID = :langOneID)" +
			"  OR" +
			"  em.lexemeTwoId IN (SELECT e.id FROM Lexeme e WHERE e.id IN (" +
			"    SELECT ef.lexemeId FROM LexemeForm ef" +
			"    WHERE MATCH (ef.text) AGAINST (:terms IN NATURAL LANGUAGE MODE)" +
			"      AND ef.orthoId IN (orthosLangTwo))" +
			"    AND e.langID = :langTwoID)";

		NativeQuery query = session.createSQLQuery(sql)
			.addScalar("emp_id", new LongType())
			.addScalar("emp_id", new LongType());

		query.setParameter("", "");

		Stream<Object[]> mappings = query.stream();
		mappings.map(m -> (Long) m[0] + ", " + (Long)m[1])
			.forEach(m -> LOG.info("Found Mapping: " + m));
		mappings.close();

		/*List<Object[]> rows = query.list();
		for(Object[] row : rows) {
			LOG.info("Found Mapping: " + (Long) row[0] + ", " + (Long) row[1]);
		}*/

		// TODO Create and store SearchRun instance

		return result;
	}

	public void getSuggestions() throws Exception {
	}


	private boolean checkQueryObject(QueryObject queryData) {
		// TODO implement the checks here
		return true;
	}

	/*
	public static QueryObject createQueryObject() throws CodeException {
		QueryObject q = new QueryObject();

		String langPairStr = ""; // TODO request.params(":pair");
		if (langPairStr == null || !langPairStr.matches("[0-9]+")) {
			throw new CodeException(ErrorCode.Search_ParameterNotNumeric, new Object[]{"lang pair", langPairStr});
		}

		// Maximum length of search text is 100 characters
		String searchText = ""; // TODO request.params(":str").substring(0, 100);

//		int direction = readNumbericQueryParam(request, "d", "0");
//		int occurence = readNumbericQueryParam(request, "o", "0");
//		int dialectOne = readNumbericQueryParam(request, "d1", null);
//		int dialectTwo = readNumbericQueryParam(request, "d2", null);
//
//		int displayOrthoOne = readNumbericQueryParam(request, "do1", null);
//		int displayOrthoTwo = readNumbericQueryParam(request, "do2", null);

//		q.setLangPair(Integer.parseInt(langPairStr));
//		q.setSearchText(searchText);
//		q.setDirection(direction);
//		q.setOccurrence(occurence);

		return q;
	}
	*/

	/*
	private int readNumbericQueryParam(Request request, String key, String defaultValue) throws CodeException {
		String value = defaultValue != null
				? request.queryParamOrDefault(key, defaultValue)
				: request.params(key);
		if (value == null || !value.matches("[0-9]+")) {
			throw new CodeException(ErrorCode.Search_ParameterNotNumeric, new Object[]{key, value});
		}
		return Integer.parseInt(value);
	}
	 */
}
