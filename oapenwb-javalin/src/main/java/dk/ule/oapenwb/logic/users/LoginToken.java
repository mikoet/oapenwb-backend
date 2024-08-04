// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.users;

import dk.ule.oapenwb.entity.basis.RoleType;
import lombok.Data;

@Data
public class LoginToken
{
	private Integer id;
	private RoleType role;
}