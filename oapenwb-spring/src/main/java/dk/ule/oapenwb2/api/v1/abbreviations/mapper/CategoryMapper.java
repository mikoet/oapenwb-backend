// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations.mapper;

import dk.ule.oapenwb.persistency.entity.content.basedata.Category;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.CategoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper
{
	@Mapping(target = "uitId", source = "uitID")
	@Mapping(target = "abbreviatedUitId", source = "uitID_abbr")
	CategoryDto map(Category category);

	List<CategoryDto> mapCategoryList(List<Category> categoryList);
}
