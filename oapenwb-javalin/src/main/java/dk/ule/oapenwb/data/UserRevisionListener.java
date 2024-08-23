// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb.data;

import dk.ule.oapenwb.entity.auditing.UserRevisionEntity;
import dk.ule.oapenwb.util.CurrentUser;
import dk.ule.oapenwb.util.HibernateUtil;
import org.hibernate.envers.RevisionListener;

/**
 * This implementation of a {@link RevisionListener} will add a userID (from the {@link CurrentUser} instance)
 * and a revision comment (from {@link HibernateUtil}) for every commit.
 */
public class UserRevisionListener implements RevisionListener
{
	public void newRevision(Object revisionEntity)
	{
		UserRevisionEntity revEntity = (UserRevisionEntity) revisionEntity;
		revEntity.setPlatform(UserRevisionEntity.PLATFORM_JAVALIN);
		revEntity.setUserID(CurrentUser.INSTANCE.get());
		revEntity.setComment(HibernateUtil.getRevisionComment());
	}
}
