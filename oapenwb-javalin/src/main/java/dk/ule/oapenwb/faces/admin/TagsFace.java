// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.logic.admin.TagsController;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Tag;

/**
 * Javalin face to the {@link TagsController}.
 */
@Singleton
public class TagsFace extends EntityFace<Tag, String>
{
	@Inject
	public TagsFace(TagsController controller)
	{
		super(controller);
	}
}
