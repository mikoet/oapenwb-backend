// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.entity.ui.*;
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
import dk.ule.oapenwb.logic.presentation.ControllerSet;
import lombok.Getter;

/**
 * <p>This class creates the controllers necessary to manage the entities of the administration interface and connects
 * them to each other if necessary.</p>
 */
public class AdminControllers
{
	@Getter
	private final EntityController<UiLanguage, String> uiLanguagesController;

	@Getter
	private final EntityController<UiTranslationScope, String> uiScopesController;

	@Getter
	private final UiTranslationSetController uiTranslationsController;

	@Getter
	private final EntityController<UiResultCategory, Integer> uiResultCategoriesController;

	@Getter
	private final CEntityController<Orthography, Integer> orthographiesController;

	@Getter
	private final EntityController<LangOrthoMapping, Integer> loMappingsController;

	@Getter
	private final CEntityController<Language, Integer> languagesController;

	@Getter
	private final LangPairController langPairsController;

	@Getter
	private final EntityController<LexemeType, Integer> lexemeTypesController;

	@Getter
	private final CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesController;

	@Getter
	private final CGEntityController<TypeLanguageConfig, Integer, Integer> tlConfigsController;

	// Groups by lexeme type ID
	// <LemmaTemplate, LemmaTemplate ID, LexemeType ID>
	@Getter
	private final CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesController;

	@Getter
	private final CEntityController<Category, Integer> categoriesController;

	@Getter
	private final CEntityController<Level, Integer> unitLevelsController;

	@Getter
	private final CEntityController<LinkType, Integer> linkTypesController;

	@Getter
	private final TagController tagsController;

	@Getter
	private final SynGroupController synGroupsController;

	@Getter
	private final SememeController sememeController;

	@Getter
	private final LexemeController lexemesController;

	@Getter
	private final LockController lockController;

	// TODO ENHANCE: Are there indices on the columns of default ordering?
	AdminControllers() {
		// Interface data uitKey. scope id
		this.uiLanguagesController = new EntityController<>(UiLanguage::new, UiLanguage.class,
				ids -> ids[0], false) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.locale ASC";
			}
		};
		this.uiScopesController = new EntityController<>(UiTranslationScope::new, UiTranslationScope.class,
				ids -> ids[0], false) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.id ASC";
			}
		};
		this.uiTranslationsController = new UiTranslationSetController(
			new EntityController<>(UiTranslation::new, UiTranslation.class,
				ids -> new UiTranslationKey(ids[0]/*uitID*/, ids[1]/*scope*/, ids[2]/*locale*/), false, false)
			{
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.uitKey.scopeID ASC, E.uitKey.id ASC";
				}
			}
		);
		this.uiResultCategoriesController = new EntityController<>(
			UiResultCategory::new, UiResultCategory.class, ids -> Integer.parseInt(ids[0])
		) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.position ASC";
			}
		};

		// Dictionary data
		this.orthographiesController = new CEntityController<>(
			Orthography::new, Orthography.class, ids -> Integer.parseInt(ids[0])
		) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.abbreviation ASC";
			}
		};
		this.loMappingsController = new EntityController<>(LangOrthoMapping::new, LangOrthoMapping.class, ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.langID ASC, E.position ASC";
			}
		};
		this.languagesController = new CEntityController<>(Language::new, Language.class, ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.locale ASC";
			}
		};
		this.langPairsController = new LangPairController(languagesController);
		this.lexemeTypesController = new EntityController<>(LexemeType::new, LexemeType.class, ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.name ASC";
			}
		};
		this.lexemeFormTypesController = new CGEntityController<>(LexemeFormType::new, LexemeFormType.class,
			ids -> Integer.parseInt(ids[0]), entity -> entity.getLexemeTypeID()) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.lexemeTypeID ASC, E.position ASC";
			}
		};
		this.tlConfigsController = new CGEntityController<>(TypeLanguageConfig::new, TypeLanguageConfig.class,
			ids -> Integer.parseInt(ids[0]), entity -> entity.getLexemeTypeID()) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.lexemeTypeID ASC, E.langID ASC";
			}
		};
		this.lemmaTemplatesController = new CGEntityController<>(LemmaTemplate::new, LemmaTemplate.class,
			ids -> Integer.parseInt(ids[0]), entity -> entity.getLexemeTypeID()) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.lexemeTypeID ASC, E.name ASC";
			}
		};
		this.categoriesController = new CEntityController<>(Category::new, Category.class, ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.description ASC";
			}
		};
		this.unitLevelsController = new CEntityController<>(Level::new, Level.class, ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.description ASC";
			}
		};
		this.linkTypesController = new CEntityController<>(LinkType::new, LinkType.class, ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.target ASC, E.description ASC";
			}
		};
		this.tagsController = new TagController();

		// Controller Set
		final ControllerSet controllerSet = new ControllerSet();
		this.synGroupsController = new SynGroupController(controllerSet);

		// Content data
		this.sememeController = new SememeController();
		this.lexemesController = new LexemeController(lexemeFormTypesController, lemmaTemplatesController,
			tagsController, synGroupsController, langPairsController, sememeController);

		// Set controllers now (this is where auto wiring comes in helpfull)
		controllerSet.setControllers(orthographiesController, languagesController, categoriesController,
			unitLevelsController, linkTypesController, sememeController);

		// Locking
		this.lockController = new LockController();
	}
}