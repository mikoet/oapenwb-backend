// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import dk.ule.oapenwb.persistency.entity.content.lexemes.Link;
import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;

/**
 * <p>The LinksController is no full controller by now as the persisting is currently done by the
 * LexemeController's LexemeCreator and LexemeUpdater classes.</p>
 * <p>Thus, this controller will for now only contain helper methods in regard to links.</p>
 */
public class LinksController
{
	// no exception handling done within this method!
	public static boolean linkExists(final Session session, final Link link) {
		final String queryString = String.format(
			"SELECT COUNT(*) AS col from %ss E where E.typeID = :typeID AND E.startSememeID = :startSememeID "
				+ "AND E.endSememeID = :endSememeID", Link.class.getSimpleName());
		final NativeQuery<Object> query = session.createNativeQuery(queryString, Object.class)
			.addScalar("col", StandardBasicTypes.LONG);
		query.setParameter("typeID", link.getTypeID());
		query.setParameter("startSememeID", link.getStartSememeID());
		query.setParameter("endSememeID", link.getEndSememeID());

		final long count = (Long) query.getSingleResult();
		return count > 0;
	}
}
