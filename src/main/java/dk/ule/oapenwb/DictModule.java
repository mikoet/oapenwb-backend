// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.entity.ui.*;
import dk.ule.oapenwb.faces.ConfigFace;
import dk.ule.oapenwb.faces.L10nFace;
import dk.ule.oapenwb.faces.SearchFace;
import dk.ule.oapenwb.faces.UsersFace;
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
import dk.ule.oapenwb.logic.config.ConfigController;
import dk.ule.oapenwb.logic.l10n.L10nController;
import dk.ule.oapenwb.logic.presentation.ControllerSet;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.logic.users.UserController;
import dk.ule.oapenwb.logic.users.ViolationController;

public class DictModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		configureDictClasses();
		configureAdminClasses();
	}

	private void configureDictClasses()
	{
		bind(AppConfig.class).asEagerSingleton();
		bind(DictJwtProvider.class);

		bind(ViolationController.class);
		bind(ConfigController.class);
		bind(L10nController.class);
		bind(SearchController.class);
		bind(UserController.class);
		bind(DictControllers.class);

		bind(ConfigFace.class);
		bind(L10nFace.class);
		bind(SearchFace.class);
		bind(UsersFace.class);
	}

	/**
	 * TODO ENHANCE: Are there indices on all columns of default ordering?
	 */
	private void configureAdminClasses()
	{
		/* !! UI data */

		// UiLanguages controller
		bind(new TypeLiteral<EntityController<UiLanguage, String>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_UI_LANGUAGES))
			.toInstance(new EntityController<>(UiLanguage::new, UiLanguage.class,
				ids -> ids[0], false) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.locale ASC";
				}
			});

		// UiScopes controller
		bind(new TypeLiteral<EntityController<UiTranslationScope, String>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_UI_SCOPES))
			.toInstance(new EntityController<>(UiTranslationScope::new, UiTranslationScope.class,
				ids -> ids[0], false) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.id ASC";
				}
			});

		// UiTranslationSets controller
		bind(UiTranslationSetController.class);

		// UiTranslations controller
		bind(new TypeLiteral<EntityController<UiTranslation, UiTranslationKey>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_UI_TRANSLATIONS))
			.toInstance(new EntityController<>(UiTranslation::new, UiTranslation.class,
				ids -> new UiTranslationKey(ids[0]/*uitID*/, ids[1]/*scope*/, ids[2]/*locale*/), false, false)
			{
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.uitKey.scopeID ASC, E.uitKey.id ASC";
				}
			});

		// UiResultCategories controller
		bind(new TypeLiteral<EntityController<UiResultCategory, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_UI_RESULT_CATEGORIES))
			.toInstance(new EntityController<>(
				UiResultCategory::new, UiResultCategory.class, ids -> Integer.parseInt(ids[0])
			) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.position ASC";
				}
			});


		/* !! Dictionary data */

		// Orthographies controller
		bind(new TypeLiteral<CEntityController<Orthography, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_ORTHOGRAPHIES))
			.toInstance(new CEntityController<>(Orthography::new, Orthography.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.abbreviation ASC";
				}
			});

		// LoMappings controller
		bind(new TypeLiteral<EntityController<LangOrthoMapping, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_LO_MAPPINGS))
			.toInstance(new EntityController<>(LangOrthoMapping::new, LangOrthoMapping.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.langID ASC, E.position ASC";
				}
			});

		// Languages controller
		bind(new TypeLiteral<CEntityController<Language, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_LANGUAGES))
			.toInstance(new CEntityController<>(Language::new, Language.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.locale ASC";
				}
			});

		// LangPairs controller
		bind(LangPairController.class);

		// LexemeTypes controller
		bind(new TypeLiteral<EntityController<LexemeType, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_LEXEME_TYPES))
			.toInstance(new EntityController<>(LexemeType::new, LexemeType.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.name ASC";
				}
			});

		// LexemeFormTypes controller
		bind(new TypeLiteral<CGEntityController<LexemeFormType, Integer, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_LEXEME_FORM_TYPES))
			.toInstance(new CGEntityController<>(LexemeFormType::new, LexemeFormType.class,
				ids -> Integer.parseInt(ids[0]), entity -> entity.getLexemeTypeID()) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.lexemeTypeID ASC, E.position ASC";
				}
			});

		// TypeLangConfigs controller
		bind(new TypeLiteral<CGEntityController<TypeLanguageConfig, Integer, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_TL_CONFIGS))
			.toInstance(new CGEntityController<>(TypeLanguageConfig::new, TypeLanguageConfig.class,
				ids -> Integer.parseInt(ids[0]), entity -> entity.getLexemeTypeID()) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.lexemeTypeID ASC, E.langID ASC";
				}
			});

		// LemmaTemplates controller
		bind(new TypeLiteral<CGEntityController<LemmaTemplate, Integer, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_LEMMA_TEMPLATES))
			.toInstance(new CGEntityController<>(LemmaTemplate::new, LemmaTemplate.class,
				ids -> Integer.parseInt(ids[0]), entity -> entity.getLexemeTypeID()) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.lexemeTypeID ASC, E.name ASC";
				}
			});

		// Categories controller
		bind(new TypeLiteral<CEntityController<Category, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_CATEGORIES))
			.toInstance(new CEntityController<>(Category::new, Category.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.description ASC";
				}
			});

		// UnitLevels controller
		bind(new TypeLiteral<CEntityController<Level, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_UNIT_LEVELS))
			.toInstance(new CEntityController<>(Level::new, Level.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.description ASC";
				}
			});

		// LinkTypes controller
		bind(new TypeLiteral<CEntityController<LinkType, Integer>>() {})
			.annotatedWith(Names.named(AdminControllers.CONTROLLER_LINK_TYPES))
			.toInstance(new CEntityController<>(LinkType::new, LinkType.class, ids -> Integer.parseInt(ids[0])) {
				@Override
				protected String getDefaultOrderClause() {
					return " order by E.target ASC, E.description ASC";
				}
			});


		/* !! Content data */

		// Tags controller
		bind(TagController.class);

		// SynGroups controller
		bind(SynGroupController.class);

		// Sememes controller
		bind(SememeController.class);

		// Lexemes controller
		bind(LexemeController.class);

		// Locks controller
		bind(LockController.class);

		// ControllerSet
		bind(ControllerSet.class);

		// The controllers class itself
		bind(AdminControllers.class);
	}
}
