// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.logic.admin.UiResultCategoriesController;
import dk.ule.oapenwb.persistency.entity.ui.UiResultCategory;

@Singleton
public class UiResultCategoriesFace extends EntityFace<UiResultCategory, Integer>
{
	@Override
	protected UiResultCategoriesController getController() {
		return (UiResultCategoriesController) super.getController();
	}

	@Inject
	public UiResultCategoriesFace(UiResultCategoriesController controller) {
		super(controller);
	}
}
