// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.TimeUtil;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

/**
 * <p>SearchController in an easy stadium. Does not work now for sure.</p>
 *
 * TODO REFACT
 */
public class SearchController
{
	private static final Logger LOG = LoggerFactory.getLogger(SearchController.class);

	/**
	 * Path: /s/:pair/:str?d=0&o=1…
	 *
	 * @throws Exception
	 */
	public ResultObject executeQuery(QueryObject queryData) throws Exception
	{
		LOG.info("Got search query");
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
