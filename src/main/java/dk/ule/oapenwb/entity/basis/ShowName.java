// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import lombok.Getter;

/**
 * A user can specifiy the way its name is visualized.
 */
public enum ShowName
{
	Firstname_FirstLetterLastname((byte) 1),
	FullName((byte) 2),
	Username((byte) 3);

	@Getter
	private byte number;

	ShowName(byte number) { this.number = number; }

	public static ShowName fromNumber(byte number)
	{
		switch (number) {
			case 1:
				return ShowName.Firstname_FirstLetterLastname;
			case 2:
				return ShowName.FullName;
			case 3:
				return ShowName.Username;

			default:
				throw new IllegalArgumentException("Number [" + number
				+ "] not supported.");
		}
	}
}