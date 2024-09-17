// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations.mapper;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.basedata.Orthography;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.LanguageDto;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.OrthographyDto;
import dk.ule.oapenwb2.service.content.LanguageService;
import dk.ule.oapenwb2.service.content.OrthographyService;
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

	@Autowired
	protected OrthographyService orthographyService;

	@Mapping(target = "ownName", source = "localName")
	@Mapping(target = "uitId", source = "uitID")
	@Mapping(target = "uitIdAbbr", source = "uitID_abbr")
	@Mapping(target = "dialects", expression = "java(this.mapLanguageList(languageService.getDialectsFor(language)))")
	@Mapping(target = "orthographies", expression = "java(mapOrthographyList(orthographyService.getOrthographiesForLanguage(language)))")
	public abstract LanguageDto mapLanguageList(Language language);

	public abstract List<LanguageDto> mapLanguageList(List<Language> languageList);

	@Mapping(target = "uitId", source = "uitID")
	public abstract OrthographyDto map(Orthography orthography);

	public abstract List<OrthographyDto> mapOrthographyList(List<Orthography> orthographyList);
}
