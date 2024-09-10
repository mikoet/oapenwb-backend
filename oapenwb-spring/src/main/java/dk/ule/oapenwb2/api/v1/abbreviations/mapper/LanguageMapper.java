// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations.mapper;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.LanguageDto;
import dk.ule.oapenwb2.service.content.LanguageService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class LanguageMapper
{
	@Autowired
	protected LanguageService languageService;

	@Mapping(target = "ownName", source = "localName")
	@Mapping(target = "uitId", source = "uitID")
	@Mapping(target = "uitIdAbbr", source = "uitID_abbr")
	@Mapping(target = "dialects", expression = "java(this.map(languageService.getDialectsFor(language)))")
	public abstract LanguageDto map(Language language);

	public abstract List<LanguageDto> map(List<Language> languageList);
}
