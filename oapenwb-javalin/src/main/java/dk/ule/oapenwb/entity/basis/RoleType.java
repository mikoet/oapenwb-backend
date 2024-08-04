// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import io.javalin.security.RouteRole;
import lombok.Getter;

/**
 * Each user/visitor of the dictionary has got a RoleType that is expressed via this enumeration.
 */
public enum RoleType implements RouteRole
{
	Anyone("-"),
	User("U"),
	Moderator("M"),
	Editor("E"),
	Admin("A");

	@Getter
	private String character;

	RoleType(String character) { this.character = character; }

	public static RoleType fromShortName(String character)
	{
		switch (character) {
			case "-":
				return RoleType.Anyone;
			case "U":
				return RoleType.User;
			case "M":
				return RoleType.Moderator;
			case "E":
				return RoleType.Editor;
			case "A":
				return RoleType.Admin;

			default:
				throw new IllegalArgumentException("Character [" + character
				+ "] not supported.");
		}
	}
}