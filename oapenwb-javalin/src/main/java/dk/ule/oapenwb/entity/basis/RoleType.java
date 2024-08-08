// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import io.javalin.security.RouteRole;
import lombok.Getter;

/**
 * Each user/visitor of the dictionary has got a RoleType that is expressed via this enumeration.
 */
@Getter
public enum RoleType implements RouteRole
{
	Anyone("-"),
	User("U"),
	Moderator("M"),
	Editor("E"),
	Admin("A");

	private final String character;

	RoleType(String character) { this.character = character; }

	public static RoleType fromShortName(String character)
	{
		return switch (character) {
			case "-" -> RoleType.Anyone;
			case "U" -> RoleType.User;
			case "M" -> RoleType.Moderator;
			case "E" -> RoleType.Editor;
			case "A" -> RoleType.Admin;

			default -> throw new IllegalArgumentException("Character [" + character
				+ "] not supported.");
		};
	}
}