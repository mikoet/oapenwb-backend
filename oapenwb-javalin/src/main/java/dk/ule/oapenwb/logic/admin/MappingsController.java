// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import dk.ule.oapenwb.persistency.entity.content.lexemes.Mapping;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

/**
 * <p>The MappingsController is no full controller by now as the persisting is currently done by the
 * LexemeController's LexemeCreator and LexemeUpdater classes.</p>
 * <p>Thus, this controller will for now only contain helper methods in regard to mappings.</p>
 */
public class MappingsController
{
	// no exception handling done within this method!
	public static boolean mappingExists(final Session session, final Mapping mapping) {
		final String queryString = String.format(
			"SELECT COUNT(*) AS col from %ss E where E.langPair = :langPair AND E.sememeOneID = :sememeOneID "
				+ "AND E.sememeTwoID = :sememeTwoID", Mapping.class.getSimpleName());
		final NativeQuery<Object> query = session.createNativeQuery(queryString, Object.class)
			.addScalar("col", StandardBasicTypes.LONG);
		query.setParameter("langPair", mapping.getLangPair());
		query.setParameter("sememeOneID", mapping.getSememeOneID());
		query.setParameter("sememeTwoID", mapping.getSememeTwoID());

		final long count = (Long) query.getSingleResult();
		return count > 0;
	}
}
