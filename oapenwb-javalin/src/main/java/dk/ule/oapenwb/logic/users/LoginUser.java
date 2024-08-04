// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.users;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The class LoginUser contains the data received by the user after its login.
 */
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginUser
{
	private String username;
	private String firstname;
	private String lastname;
	private int failedLogins;
	private LoginToken token;
}