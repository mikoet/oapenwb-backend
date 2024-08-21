// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.config;

import dk.ule.oapenwb.persistency.entity.content.basedata.*;
import dk.ule.oapenwb.persistency.entity.ui.UiLanguage;
import lombok.Getter;

import java.util.List;

/**
 * TODO This is used for..eh..nothing?
 */
public class Config
{
	@Getter
	private List<UiLanguage> uiLanguages;
	@Getter
	private List<Orthography> orthographies;
	@Getter
	private List<Language> langs;
	@Getter
	private List<LangOrthoMapping> loMappings;
	@Getter
	private List<LangPair> langPairs;
	@Getter
	private List<LexemeType> lexemeTypes;

	Config(List<UiLanguage> uiLanguages, List<Orthography> orthographies, List<Language> langs,
		   List<LangOrthoMapping> loMappings, List<LangPair> langPairs, List<LexemeType> lexemeTypes)
	{
		this.uiLanguages = uiLanguages;
		this.orthographies = orthographies;
		this.langs = langs;
		this.loMappings = loMappings;
		this.langPairs = langPairs;
		this.lexemeTypes = lexemeTypes;
	}
}