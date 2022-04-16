// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.entity.ui.UiLanguage;
import dk.ule.oapenwb.entity.ui.UiResultCategory;
import dk.ule.oapenwb.entity.ui.UiTranslationScope;
import dk.ule.oapenwb.logic.admin.LangPairController;
import dk.ule.oapenwb.logic.admin.TagController;
import dk.ule.oapenwb.logic.admin.UiTranslationSetController;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.generic.CGEntityController;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememeController;
import dk.ule.oapenwb.logic.admin.locking.LockController;
import dk.ule.oapenwb.logic.admin.syngroup.SynGroupController;
import lombok.Getter;

/**
 * <p>This class contains the controllers necessary to manage the entities of the administration interface.</p>
 */
@Singleton
public class AdminControllers
{
	public static final String CONTROLLER_UI_LANGUAGES = "Controller_UiLanguages";
	public static final String CONTROLLER_UI_SCOPES = "Controller_UiScopes";
	public static final String CONTROLLER_UI_RESULT_CATEGORIES = "Controller_UiResultCategories";
	// Only for injection into UiTranslationSetController
	public static final String CONTROLLER_UI_TRANSLATIONS = "Controller_UiTranslations";

	public static final String CONTROLLER_ORTHOGRAPHIES = "Controller_Orthographies";
	public static final String CONTROLLER_LO_MAPPINGS = "Controller_LoMappings";
	public static final String CONTROLLER_LANGUAGES = "Controller_Languages";
	public static final String CONTROLLER_LEXEME_TYPES = "Controller_LexemeTypes";
	public static final String CONTROLLER_LEXEME_FORM_TYPES = "Controller_LexemeFormTypes";
	public static final String CONTROLLER_TL_CONFIGS = "Controller_TlConfigs";
	public static final String CONTROLLER_LEMMA_TEMPLATES = "Controller_LemmaTemplates";
	public static final String CONTROLLER_CATEGORIES = "Controller_Categories";
	public static final String CONTROLLER_UNIT_LEVELS = "Controller_UnitLevels";
	public static final String CONTROLLER_LINK_TYPES = "Controller_LinkTypes";


	@Getter
	@Inject
	@Named(CONTROLLER_UI_LANGUAGES)
	private EntityController<UiLanguage, String> uiLanguagesController;

	@Getter
	@Inject
	@Named(CONTROLLER_UI_SCOPES)
	private EntityController<UiTranslationScope, String> uiScopesController;

	@Getter
	@Inject
	private UiTranslationSetController uiTranslationsController;

	@Getter
	@Inject
	@Named(CONTROLLER_UI_RESULT_CATEGORIES)
	private EntityController<UiResultCategory, Integer> uiResultCategoriesController;


	@Getter
	@Inject
	@Named(CONTROLLER_ORTHOGRAPHIES)
	private CEntityController<Orthography, Integer> orthographiesController;

	@Getter
	@Inject
	@Named(CONTROLLER_LO_MAPPINGS)
	private EntityController<LangOrthoMapping, Integer> loMappingsController;

	@Getter
	@Inject
	@Named(CONTROLLER_LANGUAGES)
	private CEntityController<Language, Integer> languagesController;

	@Getter
	@Inject
	private LangPairController langPairsController;

	@Getter
	@Inject
	@Named(CONTROLLER_LEXEME_TYPES)
	private EntityController<LexemeType, Integer> lexemeTypesController;

	@Getter
	@Inject
	@Named(CONTROLLER_LEXEME_FORM_TYPES)
	private CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesController;

	@Getter
	@Inject
	@Named(CONTROLLER_TL_CONFIGS)
	private CGEntityController<TypeLanguageConfig, Integer, Integer> tlConfigsController;

	// Groups by lexeme type ID
	// <LemmaTemplate, LemmaTemplate ID, LexemeType ID>
	@Getter
	@Inject
	@Named(CONTROLLER_LEMMA_TEMPLATES)
	private CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesController;

	@Getter
	@Inject
	@Named(CONTROLLER_CATEGORIES)
	private CEntityController<Category, Integer> categoriesController;

	@Getter
	@Inject
	@Named(CONTROLLER_UNIT_LEVELS)
	private CEntityController<Level, Integer> unitLevelsController;

	@Getter
	@Inject
	@Named(CONTROLLER_LINK_TYPES)
	private CEntityController<LinkType, Integer> linkTypesController;

	@Getter
	@Inject
	private TagController tagsController;

	@Getter
	@Inject
	private SynGroupController synGroupsController;

	@Getter
	@Inject
	private SememeController sememeController;

	@Getter
	@Inject
	private LexemeController lexemesController;

	@Getter
	@Inject
	private LockController lockController;
}
