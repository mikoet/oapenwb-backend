// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Tag;
import dk.ule.oapenwb.entity.ui.UiLanguage;
import dk.ule.oapenwb.entity.ui.UiResultCategory;
import dk.ule.oapenwb.entity.ui.UiTranslationScope;
import dk.ule.oapenwb.faces.UiTranslationSetFace;
import dk.ule.oapenwb.faces.admin.EntityFace;
import dk.ule.oapenwb.faces.admin.LexemeFace;
import dk.ule.oapenwb.faces.admin.SememeFace;
import dk.ule.oapenwb.faces.admin.SynGroupFace;
import lombok.Getter;

/**
 * This class creates the Javalin faces for all the {@link AdminControllers}.
 */
class AdminFaces {
	@Getter
	private EntityFace<UiLanguage, String> uiLanguagesFace;

	@Getter
	private EntityFace<UiTranslationScope, String> uiScopesFace;

	@Getter
	private UiTranslationSetFace uiTranslationsFace;

	@Getter
	private EntityFace<UiResultCategory, Integer> uiResultCategoriesFace;

	@Getter
	private EntityFace<Orthography, Integer> orthographiesFace;

	@Getter
	private EntityFace<LangOrthoMapping, Integer> loMappingsFace;

	@Getter
	private EntityFace<Language, Integer> languagesFace;

	@Getter
	private EntityFace<LangPair, String> langPairsFace;

	@Getter
	private EntityFace<LexemeType, Integer> lexemeTypesFace;

	@Getter
	private EntityFace<LexemeFormType, Integer> lexemeFormTypesFace;

	@Getter
	private EntityFace<TypeLanguageConfig, Integer> tlConfigsFace;

	@Getter
	private EntityFace<LemmaTemplate, Integer> lemmaTemplatesFace;

	@Getter
	private EntityFace<Category, Integer> categoriesFace;

	@Getter
	private EntityFace<Level, Integer> unitLevelsFace;

	@Getter
	private EntityFace<LinkType, Integer> linkTypesFace;

	@Getter
	private EntityFace<Tag, String> tagsFace;

	@Getter
	private SynGroupFace synGroupsFace;

	@Getter
	private SememeFace sememesFace;

	@Getter
	private LexemeFace lexemesFace;

	AdminFaces(AdminControllers adminControllers, DictControllers dictControllers) {
		this.uiLanguagesFace = new EntityFace<>(adminControllers.getUiLanguagesController());
		this.uiScopesFace = new EntityFace<>(adminControllers.getUiScopesController());
		this.uiTranslationsFace = new UiTranslationSetFace(adminControllers.getUiTranslationsController());
		this.uiResultCategoriesFace = new EntityFace<>(adminControllers.getUiResultCategoriesController());

		this.orthographiesFace = new EntityFace<>(adminControllers.getOrthographiesController());
		this.loMappingsFace = new EntityFace<>(adminControllers.getLoMappingsController());
		this.languagesFace = new EntityFace<>(adminControllers.getLanguagesController());
		this.langPairsFace = new EntityFace<>(adminControllers.getLangPairsController());
		this.lexemeTypesFace = new EntityFace<>(adminControllers.getLexemeTypesController());
		this.lexemeFormTypesFace = new EntityFace<>(adminControllers.getLexemeFormTypesController());
		this.tlConfigsFace = new EntityFace<>(adminControllers.getTlConfigsController());
		this.lemmaTemplatesFace = new EntityFace<>(adminControllers.getLemmaTemplatesController());
		this.categoriesFace = new EntityFace<>(adminControllers.getCategoriesController());
		this.unitLevelsFace = new EntityFace<>(adminControllers.getUnitLevelsController());
		this.linkTypesFace = new EntityFace<>(adminControllers.getLinkTypesController());
		this.tagsFace = new EntityFace<>(adminControllers.getTagsController());

		this.synGroupsFace = new SynGroupFace(adminControllers.getSynGroupsController());
		this.sememesFace = new SememeFace(adminControllers.getSememeController());
		this.lexemesFace = new LexemeFace(adminControllers.getLexemesController());
	}
}