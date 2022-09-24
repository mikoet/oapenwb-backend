// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.entity.ui.UiResultCategory;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;

@Singleton
public class UiResultCategoriesController extends CEntityController<UiResultCategory, Integer>
{
	public UiResultCategoriesController() {
		super(UiResultCategory::new, UiResultCategory.class, ids -> Integer.parseInt(ids[0]));
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.position ASC";
	}
}
