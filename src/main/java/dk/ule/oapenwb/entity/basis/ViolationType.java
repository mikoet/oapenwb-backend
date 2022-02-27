// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import lombok.Getter;

/**
 * Specifies the type of a violation.
 */
public enum ViolationType
{
	Login(ViolationType.CHAR_LOGIN, 10, 180, 360), // failed Login because user does not exist
	Password(ViolationType.CHAR_PASSWORD, 7, 180, 360), // failed login because of wrong Password.
	                                                    // The first violation here is only created after 3 failed attempts!
	                                                    // Then every further violation is created directly.
	Forgotten(ViolationType.CHAR_FORGOTTEN, 10, 180, 360), // request of Forgotten password
	Registration(ViolationType.CHAR_REGISTRATION, 10, 180, 720); // Registration of a new account

	private static final String CHAR_LOGIN = "L";
	private static final String CHAR_PASSWORD = "P";
	private static final String CHAR_FORGOTTEN = "F";
	private static final String CHAR_REGISTRATION = "R";

	@Getter
	private String character;

	// How many violations are needed until a ban is created?
	@Getter
	private int countTilBan;

	// Timeframe in which violations are checked (in minutes) for ban creation
	@Getter
	private int timeFrame;

	// How long will a ban last (in minutes) once countTilBan is fulfilled?
	@Getter
	private int banTime;

	ViolationType(String character, int countTilBan, int timeFrame, int banTime)
	{
		this.character = character;
		this.countTilBan = countTilBan;
		this.timeFrame = timeFrame;
		this.banTime = banTime;
	}

	public static ViolationType fromShortName(String character)
	{
		switch (character) {
			case CHAR_LOGIN:
				return ViolationType.Login;
			case CHAR_PASSWORD:
				return ViolationType.Password;
			case CHAR_FORGOTTEN:
				return ViolationType.Forgotten;
			case CHAR_REGISTRATION:
				return ViolationType.Registration;
			default:
				throw new IllegalArgumentException("Character [" + character
				+ "] not supported.");
		}
	}
}