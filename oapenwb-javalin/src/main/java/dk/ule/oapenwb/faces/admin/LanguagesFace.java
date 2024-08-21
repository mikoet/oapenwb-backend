// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.logic.admin.LanguagesController;

public class LanguagesFace extends EntityFace<Language, Integer>
{
	@Inject
	public LanguagesFace(LanguagesController controller)
	{
		super(controller);
	}
}
