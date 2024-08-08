// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ViolationTypeConverter implements AttributeConverter<ViolationType, String>
{
	@Override
	public String convertToDatabaseColumn(ViolationType type)
	{
		return type.getCharacter();
	}

	@Override
	public ViolationType convertToEntityAttribute(String dbData)
	{
		return ViolationType.fromShortName(dbData);
	}
}