// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Tag;
import dk.ule.oapenwb.logic.admin.TagController;

@Singleton
public class TagsFace extends EntityFace<Tag, String>
{
	@Inject
	public TagsFace(TagController controller)
	{
		super(controller);
	}
}
