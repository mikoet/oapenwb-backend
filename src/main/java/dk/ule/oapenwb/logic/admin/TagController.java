// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Tag;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeCreator;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeUpdater;
import org.hibernate.Session;

/**
 * <p>The TagController exists to manage the tags that can be assigned to
 * {@link Lexeme}s.
 *
 * <ul>
 *   <li>As of now the controller does that by offering the two methods {@link #useTag(Session, String)} and
 *     {@link #unuseTag(Session, String)} which are used by {@link LexemeCreator}
 *     as well as {@link LexemeUpdater}.</li>
 *   <li>Both of these methods are to be used in already opened transactions(!).</li>
 * </ul>
 *
 * TODO LOCKING In both methods the tag instances are loaded just before they are modified and then saved which
 *   will minimize collisions on editing. They can basically occur, though, and a better solution shall be found.
 * </p>
 */
@Singleton
public class TagController extends EntityController<Tag, String>
{
	public TagController()
	{
		super(Tag::new, Tag.class, ids -> ids[0], false);
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.usageCount DESC, E.tag ASC";
	}

	// has no context parameter as of now, so it can only be used within open transaction
	public void useTag(final Session session, final String tag) throws CodeException
	{
		Tag tagObj = this.get(tag);
		if (tagObj != null) {
			// Increment the usage count of an existing tag object
			tagObj.setUsageCount(tagObj.getUsageCount() + 1);
			session.update(tagObj);
		} else {
			// Create a new tag object
			tagObj = new Tag(tag, null, 1, false);
			session.persist(tagObj);
		}
	}

	// has no context parameter as of now, so it can only be used within open transaction
	public void unuseTag(final Session session, final String tag) throws CodeException
	{
		Tag tagObj = this.get(tag);
		if (tagObj != null) {
			if (tagObj.getUsageCount() > 0) {
				// Decrement the usage count of an existing tag object
				tagObj.setUsageCount(tagObj.getUsageCount() - 1);
			}
			if (tagObj.getUsageCount() == 0 && !tagObj.isGuarded()) {
				session.delete(tagObj);
			} else {
				session.update(tagObj);
			}
		}
	}
}