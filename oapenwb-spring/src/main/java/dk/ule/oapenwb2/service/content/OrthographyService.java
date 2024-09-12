// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.service.content;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb2.api.v1.abbreviations.domain.OrthographyDto;
import dk.ule.oapenwb2.persistence.content.basedata.OrthographyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrthographyService
{
	private final OrthographyRepository orthographyRepository;

	public List<OrthographyDto> getOrthographyDtosForLanguage(Language language) {
		if (!"nds".equals(language.getLocale())) {
			return null;
		}
		return this.orthographyRepository.findAllDtosByLangId(language.getId());
	}
}
