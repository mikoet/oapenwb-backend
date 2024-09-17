// SPDX-FileCopyrightText: © 2024 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb2.service.content;

import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb2.persistence.content.basedata.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LanguageService
{
	private final LanguageRepository languageRepository;

	public List<Language> getAllLanguages() {
		return this.languageRepository.findAllTopLevelLanguages();
	}

	public List<Language> getDialectsFor(Language language) {
		return this.languageRepository.findAllByParentIDOrderByLocalName(language.getId());
	}
}
