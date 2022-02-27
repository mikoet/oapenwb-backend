// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ShowNameConverter implements AttributeConverter<ShowName, Byte>
{
	@Override
	public Byte convertToDatabaseColumn(ShowName scope)
	{
		return scope.getNumber();
	}

	@Override
	public ShowName convertToEntityAttribute(Byte number)
	{
		return ShowName.fromNumber(number);
	}
}