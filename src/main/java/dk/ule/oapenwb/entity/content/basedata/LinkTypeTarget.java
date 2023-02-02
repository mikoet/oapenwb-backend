// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.content.basedata;

import lombok.Getter;

public enum LinkTypeTarget {
	Lexeme('L'),
	SynGroup('S');

	@Getter
	private final char character;

	LinkTypeTarget(char character) { this.character = character; }

	public static LinkTypeTarget fromChar(char character)
	{
		return switch (character) {
			case 'L' -> LinkTypeTarget.Lexeme;
			case 'S' -> LinkTypeTarget.SynGroup;
			default -> throw new IllegalArgumentException("Character [" + character
				+ "] not supported.");
		};
	}
}