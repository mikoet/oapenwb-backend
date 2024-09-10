// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.api.v1.abbreviations;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.LanguageDto;
import dk.ule.oapenwb2.api.v1.abbreviations.mapper.LanguageMapper;
import dk.ule.oapenwb2.service.content.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("abbreviations")
@RequiredArgsConstructor
public class AbbreviationsController
{
	private final LanguageMapper languageMapper;
	private final LanguageService languageService;

	@GetMapping(path = "languages")
	public List<LanguageDto> allLanguages() {
		final List<Language> languageList = this.languageService.getAllLanguages();
		return languageMapper.map(languageList);
	}
}
