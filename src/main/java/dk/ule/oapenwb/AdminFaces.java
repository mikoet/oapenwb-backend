// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
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
import lombok.Setter;

/**
 * This class creates the Javalin faces for all the {@link AdminControllers}.
 */
@Singleton
class AdminFaces
{
	public static final String FACE_UI_LANGUAGES = "Face_UiLanguages";
	public static final String FACE_UI_SCOPES = "Face_UiScopes";
	public static final String FACE_UI_TRANSLATIONS = "Face_UiTranslations";
	public static final String FACE_UI_RESULT_CATEGORIES = "Face_UiResultCategories";

	public static final String FACE_ORTHOGRAPHIES = "Face_Orthographies";
	public static final String FACE_LO_MAPPINGS = "Face_LoMappings";
	public static final String FACE_LANGUAGES = "Face_Languages";
	public static final String FACE_LANG_PAIRS = "Face_LangPairs";
	public static final String FACE_LEXEME_TYPES = "Face_LexemeTypes";
	public static final String FACE_LEXEME_FORM_TYPES = "Face_LexemeFormTypes";
	public static final String FACE_TL_CONFIGS = "Face_TlConfigs";
	public static final String FACE_LEMMA_TEMPLATES = "Face_LemmaTemplates";
	public static final String FACE_CATEGORIES = "Face_Categories";
	public static final String FACE_LEVELS = "Face_Levels";
	public static final String FACE_LINK_TYPES = "Face_LinkTypes";

	public static final String FACE_TAGS = "Face_Tags";
	public static final String FACE_SYN_GROUPS = "Face_SynGroups";
	public static final String FACE_SEMEMES = "Face_Sememes";
	public static final String FACE_LEXEMES = "Face_Lexemes";


	@Getter
	@Setter
	@Inject
	@Named(FACE_UI_LANGUAGES)
	private EntityFace<UiLanguage, String> uiLanguagesFace;

	@Getter
	@Inject
	@Named(FACE_UI_SCOPES)
	private EntityFace<UiTranslationScope, String> uiScopesFace;

	@Getter
	@Inject
	@Named(FACE_UI_TRANSLATIONS)
	private UiTranslationSetFace uiTranslationsFace;

	@Getter
	@Inject
	@Named(FACE_UI_RESULT_CATEGORIES)
	private EntityFace<UiResultCategory, Integer> uiResultCategoriesFace;

	@Getter
	@Inject
	@Named(FACE_ORTHOGRAPHIES)
	private EntityFace<Orthography, Integer> orthographiesFace;

	@Getter
	@Inject
	@Named(FACE_LO_MAPPINGS)
	private EntityFace<LangOrthoMapping, Integer> loMappingsFace;

	@Getter
	@Inject
	@Named(FACE_LANGUAGES)
	private EntityFace<Language, Integer> languagesFace;

	@Getter
	@Inject
	@Named(FACE_LANG_PAIRS)
	private EntityFace<LangPair, String> langPairsFace;

	@Getter
	@Inject
	@Named(FACE_LEXEME_TYPES)
	private EntityFace<LexemeType, Integer> lexemeTypesFace;

	@Getter
	@Inject
	@Named(FACE_LEXEME_FORM_TYPES)
	private EntityFace<LexemeFormType, Integer> lexemeFormTypesFace;

	@Getter
	@Inject
	@Named(FACE_TL_CONFIGS)
	private EntityFace<TypeLanguageConfig, Integer> tlConfigsFace;

	@Getter
	@Inject
	@Named(FACE_LEMMA_TEMPLATES)
	private EntityFace<LemmaTemplate, Integer> lemmaTemplatesFace;

	@Getter
	@Inject
	@Named(FACE_CATEGORIES)
	private EntityFace<Category, Integer> categoriesFace;

	@Getter
	@Inject
	@Named(FACE_LEVELS)
	private EntityFace<Level, Integer> unitLevelsFace;

	@Getter
	@Inject
	@Named(FACE_LINK_TYPES)
	private EntityFace<LinkType, Integer> linkTypesFace;

	@Getter
	@Inject
	@Named(FACE_TAGS)
	private EntityFace<Tag, String> tagsFace;

	@Getter
	@Inject
	@Named(FACE_SYN_GROUPS)
	private SynGroupFace synGroupsFace;

	@Getter
	@Inject
	@Named(FACE_SEMEMES)
	private SememeFace sememesFace;

	@Getter
	@Inject
	@Named(FACE_LEXEMES)
	private LexemeFace lexemesFace;

	AdminFaces(AdminControllers adminControllers, DictControllers dictControllers)
	{
		//this.uiLanguagesFace = new EntityFace<>(adminControllers.getUiLanguagesController());
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
