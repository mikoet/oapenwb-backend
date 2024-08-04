// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import dk.ule.oapenwb.entity.content.basedata.LinkType;
import dk.ule.oapenwb.logic.admin.LinkTypesController;

/**
 * Javalin face to the {@link LinkTypesController}.
 */
public class LinkTypesFace extends EntityFace<LinkType, Integer>
{
	@Override
	protected LinkTypesController getController() {
		return (LinkTypesController) super.getController();
	}

	@Inject
	public LinkTypesFace(LinkTypesController controller)
	{
		super(controller);
	}
}
