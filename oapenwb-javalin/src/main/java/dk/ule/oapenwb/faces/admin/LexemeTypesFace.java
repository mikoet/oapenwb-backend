// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.logic.admin.LexemeTypesController;

@Singleton
public class LexemeTypesFace extends EntityFace<LexemeType, Integer>
{
	@Override
	protected LexemeTypesController getController() {
		return (LexemeTypesController) super.getController();
	}

	@Inject
	public LexemeTypesFace(LexemeTypesController controller)
	{
		super(controller);
	}
}
