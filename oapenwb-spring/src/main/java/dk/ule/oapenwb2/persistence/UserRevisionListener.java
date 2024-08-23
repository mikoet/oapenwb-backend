// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.persistence;

import dk.ule.oapenwb2.config.persistence.RevisionInfo;
import dk.ule.oapenwb2.persistence.auditing.UserRevisionEntity;
import org.hibernate.envers.RevisionListener;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This implementation of a {@link RevisionListener} will add a userID and a revision comment
 * for every commit.
 */
public class UserRevisionListener implements RevisionListener
{
	@Autowired
	private RevisionInfo revisionInfo;

	public void newRevision(Object revisionEntity) {
		final UserRevisionEntity revEntity = (UserRevisionEntity) revisionEntity;
		revEntity.setPlatform(UserRevisionEntity.PLATFORM_SPRING);
		revEntity.setUserID(revisionInfo.getActiveUserID());
		revEntity.setComment(revisionInfo.getRevisionComment());
	}
}
