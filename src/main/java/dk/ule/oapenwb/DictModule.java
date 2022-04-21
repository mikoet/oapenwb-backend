// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.util.Types;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.entity.ui.*;
import dk.ule.oapenwb.faces.*;
import dk.ule.oapenwb.faces.admin.*;
import dk.ule.oapenwb.logic.admin.LangPairsController;
import dk.ule.oapenwb.logic.admin.TagsController;
import dk.ule.oapenwb.logic.admin.UiTranslationSetsController;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.generic.CGEntityController;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememesController;
import dk.ule.oapenwb.logic.admin.locking.LockController;
import dk.ule.oapenwb.logic.admin.syngroup.SynGroupsController;
import dk.ule.oapenwb.logic.config.ConfigController;
import dk.ule.oapenwb.logic.l10n.L10nController;
import dk.ule.oapenwb.logic.presentation.ControllerSet;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.logic.users.UserController;
import dk.ule.oapenwb.logic.users.ViolationController;

import java.lang.reflect.ParameterizedType;

/**
 * <p>Configures all controllers, faces and necessary additional classes of the dictionary including the admin
 * API in one module to function in Guice.</p>
 */
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
	 * TODO ENHANCE: Are there indices on all columns of default ordering of the controllers' tables?
	 */
	private void configureAdminClasses()
	{
		/* !!!! Controllers !!!! */
		/* !! UI data !! */

		// UiLanguages controller
		EntityController<UiLanguage, String> uiLanguageCtrl = new EntityController<>(
			UiLanguage::new, UiLanguage.class,
			ids -> ids[0], false) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.locale ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_UI_LANGUAGES, uiLanguageCtrl,
			EntityController.class, UiLanguage.class, String.class);

		// UiScopes controller
		EntityController<UiTranslationScope, String> uiScopeCtrl = new EntityController<>(
			UiTranslationScope::new, UiTranslationScope.class,
			ids -> ids[0], false) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.id ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_UI_SCOPES, uiScopeCtrl,
			EntityController.class, UiTranslationScope.class, String.class);

		// UiTranslationSets controller
		bind(UiTranslationSetsController.class);

		// UiTranslations controller
		EntityController<UiTranslation, UiTranslationKey> uiTranslationCtrl = new EntityController<>(
			UiTranslation::new, UiTranslation.class,
			ids -> new UiTranslationKey(ids[0]/*uitID*/, ids[1]/*scope*/, ids[2]/*locale*/), false, false) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.uitKey.scopeID ASC, E.uitKey.id ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_UI_TRANSLATIONS, uiTranslationCtrl,
			EntityController.class, UiTranslation.class, UiTranslationKey.class);

		// UiResultCategories controller
		EntityController<UiResultCategory, Integer> uiResultCategoryCtrl = new EntityController<>(
			UiResultCategory::new, UiResultCategory.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.position ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_UI_RESULT_CATEGORIES, uiResultCategoryCtrl,
			EntityController.class, UiResultCategory.class, Integer.class);


		/* !! Dictionary data !! */

		// Orthographies controller
		CEntityController<Orthography, Integer> orthographiesCtrl = new CEntityController<>(
			Orthography::new, Orthography.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.abbreviation ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_ORTHOGRAPHIES, orthographiesCtrl,
			CEntityController.class, Orthography.class, Integer.class);

		// LoMappings controller
		EntityController<LangOrthoMapping, Integer> loMappingsCtrl = new EntityController<>(
			LangOrthoMapping::new, LangOrthoMapping.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.langID ASC, E.position ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_LO_MAPPINGS, loMappingsCtrl,
			EntityController.class, LangOrthoMapping.class, Integer.class);

		// Languages controller
		CEntityController<Language, Integer> languagesCtrl = new CEntityController<>(
			Language::new, Language.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.locale ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_LANGUAGES, languagesCtrl,
			CEntityController.class, Language.class, Integer.class);

		// LangPairs controller
		bind(LangPairsController.class);

		// LexemeTypes controller
		EntityController<LexemeType, Integer> lexemeTypesCtrl = new EntityController<>(
			LexemeType::new, LexemeType.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.name ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_LEXEME_TYPES, lexemeTypesCtrl,
			EntityController.class, LexemeType.class, Integer.class);

		// LexemeFormTypes controller
		CGEntityController<LexemeFormType, Integer, Integer> lexemeFormTypesCtrl = new CGEntityController<>(
			LexemeFormType::new,
			LexemeFormType.class,
			ids -> Integer.parseInt(ids[0]), LexemeFormType::getLexemeTypeID) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.lexemeTypeID ASC, E.position ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_LEXEME_FORM_TYPES, lexemeFormTypesCtrl,
			CGEntityController.class, LexemeFormType.class, Integer.class, Integer.class);

		// TypeLangConfigs controller
		CGEntityController<TypeLanguageConfig, Integer, Integer> tlConfigsCtrl = new CGEntityController<>(
			TypeLanguageConfig::new, TypeLanguageConfig.class,
			ids -> Integer.parseInt(ids[0]), TypeLanguageConfig::getLexemeTypeID) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.lexemeTypeID ASC, E.langID ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_TL_CONFIGS, tlConfigsCtrl,
			CGEntityController.class, TypeLanguageConfig.class, Integer.class, Integer.class);

		// LemmaTemplates controller
		CGEntityController<LemmaTemplate, Integer, Integer> lemmaTemplatesCtrl = new CGEntityController<>(
			LemmaTemplate::new,
			LemmaTemplate.class,
			ids -> Integer.parseInt(ids[0]), LemmaTemplate::getLexemeTypeID) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.lexemeTypeID ASC, E.name ASC";
			}
		};
		bindAnnotatedTypeWithInstance(
			AdminControllers.CONTROLLER_LEMMA_TEMPLATES, lemmaTemplatesCtrl,
			CGEntityController.class, LemmaTemplate.class, Integer.class, Integer.class);

		// Categories controller
		CEntityController<Category, Integer> categoriesCtrl = new CEntityController<>(
			Category::new, Category.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.description ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_CATEGORIES, categoriesCtrl,
			CEntityController.class, Category.class, Integer.class);

		// UnitLevels controller
		CEntityController<Level, Integer> levelsCtrl = new CEntityController<>(
			Level::new, Level.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.description ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_UNIT_LEVELS, levelsCtrl,
			CEntityController.class, Level.class, Integer.class);

		// LinkTypes controller
		CEntityController<LinkType, Integer> linkTypesCtrl = new CEntityController<>(
			LinkType::new, LinkType.class,
			ids -> Integer.parseInt(ids[0])) {
			@Override
			protected String getDefaultOrderClause() {
				return " order by E.target ASC, E.description ASC";
			}
		};
		bindAnnotatedTypeWithInstance(AdminControllers.CONTROLLER_LINK_TYPES, linkTypesCtrl,
			CEntityController.class, LinkType.class, Integer.class);


		/* !! Content data !! */

		// Tags controller
		bind(TagsController.class);
		bind(SynGroupsController.class);
		bind(SememesController.class);
		bind(LexemesController.class);
		bind(LockController.class);
		bind(ControllerSet.class);

		// The controllers class itself
		bind(AdminControllers.class);


		/* !!!! Faces !!!! */
		/* !! UI data !! */

		bindAnnotatedTypeWithInstance(AdminFaces.FACE_UI_LANGUAGES, new EntityFace<>(uiLanguageCtrl),
			EntityFace.class, UiLanguage.class, String.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_UI_SCOPES, new EntityFace<>(uiScopeCtrl),
			EntityFace.class, UiTranslationScope.class, String.class);

		bind(UiTranslationSetsFace.class);

		bindAnnotatedTypeWithInstance(AdminFaces.FACE_UI_RESULT_CATEGORIES, new EntityFace<>(uiResultCategoryCtrl),
			EntityFace.class, UiResultCategory.class, Integer.class);


		/* !! Dictionary data !! */

		bindAnnotatedTypeWithInstance(AdminFaces.FACE_ORTHOGRAPHIES, new EntityFace<>(orthographiesCtrl),
			EntityFace.class, Orthography.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LO_MAPPINGS, new EntityFace<>(loMappingsCtrl),
			EntityFace.class, LangOrthoMapping.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LANGUAGES, new EntityFace<>(languagesCtrl),
			EntityFace.class, Language.class, Integer.class);

		bind(LangPairsFace.class);

		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LEXEME_TYPES, new EntityFace<>(lexemeTypesCtrl),
			EntityFace.class, LexemeType.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LEXEME_FORM_TYPES, new EntityFace<>(lexemeFormTypesCtrl),
			EntityFace.class, LexemeFormType.class, Integer.class);

		bindAnnotatedTypeWithInstance(AdminFaces.FACE_TL_CONFIGS, new EntityFace<>(tlConfigsCtrl),
			EntityFace.class, TypeLanguageConfig.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LEMMA_TEMPLATES, new EntityFace<>(lemmaTemplatesCtrl),
			EntityFace.class, LemmaTemplate.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_CATEGORIES, new EntityFace<>(categoriesCtrl),
			EntityFace.class, Category.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LEVELS, new EntityFace<>(levelsCtrl),
			EntityFace.class, Level.class, Integer.class);
		bindAnnotatedTypeWithInstance(AdminFaces.FACE_LINK_TYPES, new EntityFace<>(linkTypesCtrl),
			EntityFace.class, LinkType.class, Integer.class);


		/* !! Content data !! */

		bind(TagsFace.class);
		bind(SynGroupsFace.class);
		bind(SememesFace.class);
		bind(LexemesFace.class);
	}

	/**
	 * <p>Binds the type T consisting of a raw type A with B..Z parameter types to an instance that will be used as a
	 * singleton (as its always with bind(...).toInstance(...)).</p>
	 * <p>Type example: `A&lt;B, C, D&gt;` or concrete `EntityController&lt;SomeEntity, Integer&gt;`<br>
	 * I.e. the instance given into this method must be of that type.</p>
	 *
	 * @param annotatedWith String used in the Named annotation
	 * @param instance this method will always bind to one instance (singleton)
	 * @param rawType the raw type of the instance
	 * @param paramClasses the parameter types or classes of the instance
	 * @param <T> the raw type with its parameter types of the instance
	 */
	private <T> void bindAnnotatedTypeWithInstance(String annotatedWith, T instance, Class<?> rawType, Class<?>... paramClasses)
	{
		ParameterizedType parameterizedButler = Types.newParameterizedType(rawType, paramClasses);
		@SuppressWarnings("unchecked")
		Key<T> key = (Key<T>) Key.get(parameterizedButler, Names.named(annotatedWith));
		bind(key).toInstance(instance);
	}
}
