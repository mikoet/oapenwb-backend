// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer;

import dk.ule.oapenwb.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To pröven: TODO
 */
public class ExistenceChecker
{
	private static final Logger LOG = LoggerFactory.getLogger(ExistenceChecker.class);

	/**
	 * Checks the existence of a lexeme by only looking for/in the main part of the lemma
	 * and only looking into the main variants of a lexeme.
	 *
	 * @param lemma
	 * @param lexemeTypeID
	 * @return
	 */
	public boolean lexemeExists(String lemma, int lexemeTypeID, int langID)
	{
		try {
			final NativeQuery<Object> lexemesQuery = createSimpleCheckQuery(lemma, lexemeTypeID, langID);
			final Integer result = (Integer) HibernateUtil.getSingleResult(lexemesQuery);

			return result != null && result == 1;
		} catch (Exception e) {
			LOG.error("Error quering lexeme for lemma '{}', typeID {}, langID {}", lemma, lexemeTypeID, langID);
			LOG.error("  Exception thrown: ", e);
			throw new RuntimeException("Error quering lexeme for lemma '" + lemma + "', typeID " + lexemeTypeID);
		}
	}

	private NativeQuery<Object> createSimpleCheckQuery(String lemma, int lexemeTypeID, int langID)
	{
		final StringBuilder sb = new StringBuilder();

		// Query (Q900)
		sb.append("select 1 as id from Lexemes l\n");
		sb.append("  inner join Variants v on l.id = v.lexemeID\n");
		sb.append("where v.mainVariant and v.main = :lemma\n");
		sb.append("  and l.typeID = :typeID, l.langID = :langID");

		// Create the query
		Session session = HibernateUtil.getSession();
		NativeQuery<Object> query = session.createNativeQuery(sb.toString(), Object.class)
			.addScalar("id", StandardBasicTypes.INTEGER);

		query.setParameter("lemma", lemma);
		query.setParameter("typeID", lexemeTypeID);
		query.setParameter("langID", langID);

		return query;
	}
}