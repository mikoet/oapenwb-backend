// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The VariantController can load a batch of variants for a given set of variant IDs.
 * That's all it does by now.
 */
public class VariantController
{
	private static final Logger LOG = LoggerFactory.getLogger(VariantController.class);

	public List<Variant> loadByIDs(final Set<Long> variantIDs, boolean activeOnly) throws CodeException {
		List<Variant> entities;
		try {
			Session session = HibernateUtil.getSession();
			Query<Variant> qVariants = session.createQuery(
				activeOnly ? "FROM Variant V WHERE V.active = true AND V.id IN (:variantIDs)"
					: "FROM Variant V WHERE V.id IN (:variantIDs)",Variant.class);
			qVariants.setParameterList("variantIDs", variantIDs);
			entities = qVariants.list();
		} catch (Exception e) {
			LOG.error("Error fetching instances of type Variant", e);
			throw new CodeException(ErrorCode.Admin_EntityOperation,
				Arrays.asList(new Pair<>("operation", "GET-BY-IDS"), new Pair<>("entity", "Variant")));
		}
		return entities;
	}

	public List<Variant> loadByIDs(final Set<Long> variantIDs) throws CodeException {
		return this.loadByIDs(variantIDs, false);
	}
}
