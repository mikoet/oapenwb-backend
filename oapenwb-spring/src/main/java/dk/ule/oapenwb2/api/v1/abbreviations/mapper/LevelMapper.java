// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations.mapper;

import dk.ule.oapenwb.persistency.entity.content.basedata.Level;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.LevelDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LevelMapper
{
	@Mapping(target = "uitId", source = "uitID")
	@Mapping(target = "abbreviatedUitId", source = "uitID_abbr")
	LevelDto map(Level level);

	List<LevelDto> mapLevelList(List<Level> levelList);
}
