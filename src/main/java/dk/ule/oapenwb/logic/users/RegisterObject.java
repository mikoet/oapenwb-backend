// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.users;

import lombok.Data;

@Data
public class RegisterObject
{
	private String email;
	private String username;
	private String password;
	private String firstname;
	private String lastname;
	private String token;
}