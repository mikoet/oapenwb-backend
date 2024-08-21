// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.persistency.entity.content.basedata.LinkType;

/**
 * <p>The LinkTypesController manages the {@link LinkType} instances and therefor utilises the
 * {@link CEntityController} (Cached Entity Controller).</p>
 */
@Singleton
public class LinkTypesController extends CEntityController<LinkType, Integer>
{
	public LinkTypesController()
	{
		super(LinkType::new, LinkType.class, ids -> Integer.parseInt(ids[0]));
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.target ASC, E.description ASC";
	}
}
