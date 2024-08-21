// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.config;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.persistency.entity.content.basedata.*;
import dk.ule.oapenwb.persistency.entity.ui.UiLanguage;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>Base configuration packed in an entity to be delivered to the dictionary's frontend.</p>
 *
 * TODO Probably split this into smaller entities: DetailedSearchConfig (Orthographies, Languages/dialects,
 *   LangOrthoMappings), BaseConfig (remains with langPairs of DictionarySettings)
 */
@Data
public class BaseConfig
{
	@JsonView(Views.BaseConfig.class)
	private List<UiLanguage> uiLanguages = new LinkedList<>();

	@JsonView(Views.BaseConfig.class)
	private List<Orthography> orthographies = new LinkedList<>();

	@JsonView(Views.BaseConfig.class)
	private List<Language> langs = new LinkedList<>();

	@JsonView(Views.BaseConfig.class)
	private List<LangOrthoMapping> loMappings = new LinkedList<>();

	@JsonView(Views.BaseConfig.class)
	private List<LexemeType> lexemeTypes = new LinkedList<>();

	@JsonView(Views.BaseConfig.class)
	private DictionarySettings dictionarySettings = new DictionarySettings();

	@Data
	public class DictionarySettings {
		private List<LangPair> langPairs = new LinkedList<>();
		private int defaultDirection = 0; // TODO ??
		private int defaultOccurrence = 0; // TODO ??
	}

	@Override
	public String toString() {
		return "BaseConfig";
	}
}