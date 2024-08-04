// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.users;

import dk.ule.oapenwb.entity.basis.User;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * <p>The class PublicUser represents the public view onto the
 * {@link User} entity. That means some properties of the User are
 * left out here. Some very internal data like the password hash and salt must never be made available
 * outside the backend itself.</p>
 *
 * TODO REFACT Keep or delete this class?
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicUser {
	private Long id;
	PublicUser(User user) {
	}
}