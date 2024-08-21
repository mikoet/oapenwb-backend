// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;

@Singleton
public class LexemeTypesController extends CEntityController<LexemeType, Integer>
{
	public LexemeTypesController() {
		super(LexemeType::new, LexemeType.class, ids -> Integer.parseInt(ids[0]));
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.name ASC";
	}
}
