// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dk.ule.oapenwb.logic.admin.*;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.generic.CGEntityController;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememesController;
import dk.ule.oapenwb.logic.admin.locking.LockController;
import dk.ule.oapenwb.logic.admin.syngroup.SynGroupsController;
import dk.ule.oapenwb.persistency.entity.content.basedata.*;
import dk.ule.oapenwb.persistency.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.persistency.entity.ui.UiLanguage;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslationScope;
import lombok.Getter;

/**
 * <p>This class contains the controllers necessary to manage the entities of the administration interface.</p>
 */
@Singleton
public class AdminControllers
{
	public static final String CONTROLLER_UI_LANGUAGES = "Controller_UiLanguages";
	public static final String CONTROLLER_UI_SCOPES = "Controller_UiScopes";
	// Only for injection into UiTranslationSetsController
	public static final String CONTROLLER_UI_TRANSLATIONS = "Controller_UiTranslations";

	public static final String CONTROLLER_ORTHOGRAPHIES = "Controller_Orthographies";
	public static final String CONTROLLER_LO_MAPPINGS = "Controller_LoMappings";
	public static final String CONTROLLER_LEXEME_FORM_TYPES = "Controller_LexemeFormTypes";
	public static final String CONTROLLER_TL_CONFIGS = "Controller_TlConfigs";
	public static final String CONTROLLER_LEMMA_TEMPLATES = "Controller_LemmaTemplates";
	public static final String CONTROLLER_CATEGORIES = "Controller_Categories";
	public static final String CONTROLLER_UNIT_LEVELS = "Controller_UnitLevels";


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
	private UiTranslationSetsController uiTranslationsController;

	@Getter
	@Inject
	private UiResultCategoriesController uiResultCategoriesController;


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
	private LanguagesController languagesController;

	@Getter
	@Inject
	private LangPairsController langPairsController;

	@Getter
	@Inject
	private LexemeTypesController lexemeTypesController;

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
	private LinkTypesController linkTypesController;

	@Getter
	@Inject
	private TagsController tagsController;

	@Getter
	@Inject
	private SynGroupsController synGroupsController;

	@Getter
	@Inject
	private SememesController sememesController;

	@Getter
	@Inject
	private LexemesController lexemesController;

	@Getter
	@Inject
	private LockController lockController;
}
