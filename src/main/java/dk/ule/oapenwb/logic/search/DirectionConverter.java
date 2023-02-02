// SPDX-FileCopyrightText: © 2023 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DirectionConverter implements AttributeConverter<Direction, Character>
{
	@Override
	public Character convertToDatabaseColumn(Direction d)
	{
		return d.getCharacter();
	}

	@Override
	public Direction convertToEntityAttribute(Character dbData)
	{
		return Direction.fromChar(dbData);
	}
}
