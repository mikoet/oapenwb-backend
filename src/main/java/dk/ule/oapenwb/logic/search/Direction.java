// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import lombok.Getter;

/**
 * <p>Direction of search according to arrangement in the {@link dk.ule.oapenwb.entity.content.basedata.LangPair},
 * i.e. direction Right means from language one to language two.</p>
 */
public enum Direction
{
	Both('B'),
	Left('L'),
	Right('R');

	@Getter
	private final char character;

	Direction(char character) {
		this.character = character;
	}

	public static Direction fromChar(char character)
	{
		return switch (character) {
			case 'B' -> Direction.Both;
			case 'L' -> Direction.Left;
			case 'R' -> Direction.Right;
			default -> throw new IllegalArgumentException("Character [" + character
				+ "] not supported.");
		};
	}
}
