// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dk.ule.oapenwb.persistency.entity.content.basedata.*;
import dk.ule.oapenwb.persistency.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.persistency.entity.ui.UiLanguage;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslationScope;
import dk.ule.oapenwb.faces.UiTranslationSetsFace;
import dk.ule.oapenwb.faces.admin.*;
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

	public static final String FACE_ORTHOGRAPHIES = "Face_Orthographies";
	public static final String FACE_LO_MAPPINGS = "Face_LoMappings";
	public static final String FACE_LEXEME_FORM_TYPES = "Face_LexemeFormTypes";
	public static final String FACE_TL_CONFIGS = "Face_TlConfigs";
	public static final String FACE_LEMMA_TEMPLATES = "Face_LemmaTemplates";
	public static final String FACE_CATEGORIES = "Face_Categories";
	public static final String FACE_LEVELS = "Face_Levels";


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
	private UiTranslationSetsFace uiTranslationsFace;

	@Getter
	@Inject
	private UiResultCategoriesFace uiResultCategoriesFace;

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
	private LanguagesFace languagesFace;

	@Getter
	@Inject
	private LangPairsFace langPairsFace;

	@Getter
	@Inject
	private LexemeTypesFace lexemeTypesFace;

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
	private LinkTypesFace linkTypesFace;

	@Getter
	@Inject
	private TagsFace tagsFace;

	@Getter
	@Inject
	private SynGroupsFace synGroupsFace;

	@Getter
	@Inject
	private SememesFace sememesFace;

	@Getter
	@Inject
	private LexemesFace lexemesFace;
}
