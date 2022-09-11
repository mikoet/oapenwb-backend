// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;

@Singleton
public class LanguagesController extends CEntityController<Language, Integer>
{
	public LanguagesController() {
		super(Language::new, Language.class, ids -> Integer.parseInt(ids[0]));
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.locale ASC";
	}
}
