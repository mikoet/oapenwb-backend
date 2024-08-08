// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.basis;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleTypeConverter implements AttributeConverter<RoleType, String>
{
	@Override
	public String convertToDatabaseColumn(RoleType scope)
	{
		return scope.getCharacter();
	}

	@Override
	public RoleType convertToEntityAttribute(String dbData)
	{
		return RoleType.fromShortName(dbData);
	}
}