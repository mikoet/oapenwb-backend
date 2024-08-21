// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity.content.basedata;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LinkTypeTargetConverter implements AttributeConverter<LinkTypeTarget, Character>
{
	@Override
	public Character convertToDatabaseColumn(LinkTypeTarget target)
	{
		return target.getCharacter();
	}

	@Override
	public LinkTypeTarget convertToEntityAttribute(Character dbChar)
	{
		return LinkTypeTarget.fromChar(dbChar);
	}
}