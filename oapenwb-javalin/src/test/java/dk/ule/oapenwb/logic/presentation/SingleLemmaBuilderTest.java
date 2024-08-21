// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.admin.LanguagesController;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememesController;
import dk.ule.oapenwb.logic.presentation.options.PresentationOptions;
import dk.ule.oapenwb.logic.presentation.options.SingleLemmaOptions;
import dk.ule.oapenwb.persistency.entity.content.basedata.Category;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.basedata.Level;
import dk.ule.oapenwb.persistency.entity.content.basedata.Orthography;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Tests for class {@link SingleLemmaBuilder}.</p>
 * TODO
 *   - Write tests for non-default configurational options
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class SingleLemmaBuilderTest
{
	// Orthographies
	private final Orthography oNSS = new Orthography(1, null, "o:nss", Orthography.ABBR_SAXON_NYSASSISKE_SKRYVWYSE,
		"Nysassiske Skryvwyse", true);
	private final Orthography oDBO = new Orthography(2, null, "o:dbo", Orthography.ABBR_SAXON_GERMAN_BASED,
		"Düütschbaseerde Schriefwies", true);
	private final Orthography oNBO = new Orthography(3, null, "o:nbo", Orthography.ABBR_SAXON_DUTCH_BASED,
		"Nederlands gebaseerde orthografie", true);

	// Saxon language and some dialects for it (two subdialects, each having one further subdialect)
	private final Language lSaxon = new Language(1, null, "nds", "Neddersassisk",
		"l:nds", "l:nds", oNSS.getId());
	private final Language lWestphalian = new Language(2, lSaxon.getId(), "nds-wf", "Westföälsk",
		"l:nds-wf", "l:nds-wf", oNSS.getId());
	private final Language lMoensterlaendsk = new Language(3, lWestphalian.getId(), "nds-wf-ml", "Mönsterländsk",
		"l:nds-wf-ml", "l:nds-wf-ml", oNSS.getId());
	private final Language lNorthernLowSaxon = new Language(4, lSaxon.getId(), "nds-nns", "Noordneddersassisk",
		"l:nds-nns", "l:nds-nns", oNSS.getId());
	private final Language lDitmarsk = new Language(5, lNorthernLowSaxon.getId(), "nds-nns-dm", "Ditmarsk",
		"l:nds-nns-dm", "l:nds-nns-dm", oNSS.getId());


	private IControllerSet controllerSet;

	@BeforeAll
	public void initControllers(
		@Mock CEntityController<Orthography, Integer> orthographiesController,
		@Mock LanguagesController languagesController,
		@Mock CEntityController<Category, Integer> categoriesController,
		@Mock CEntityController<Level, Integer> unitLevelsController,
		@Mock SememesController sememesController
	) throws CodeException
	{
		// Setting up mocking for orthographies controller
		Mockito.lenient().when(orthographiesController.get(oNSS.getId())).thenReturn(oNSS);
		Mockito.lenient().when(orthographiesController.get(oDBO.getId())).thenReturn(oDBO);
		Mockito.lenient().when(orthographiesController.get(oNBO.getId())).thenReturn(oNBO);

		// Setting up mocking for languages controller
		Mockito.lenient().when(languagesController.get(lSaxon.getId())).thenReturn(lSaxon);
		Mockito.lenient().when(languagesController.get(lWestphalian.getId())).thenReturn(lWestphalian);
		Mockito.lenient().when(languagesController.get(lMoensterlaendsk.getId())).thenReturn(lMoensterlaendsk);
		Mockito.lenient().when(languagesController.get(lNorthernLowSaxon.getId())).thenReturn(lNorthernLowSaxon);
		Mockito.lenient().when(languagesController.get(lDitmarsk.getId())).thenReturn(lDitmarsk);

		ControllerSet controllerSet = new ControllerSet();
		controllerSet.setControllers(orthographiesController, languagesController, categoriesController,
			unitLevelsController, sememesController);
		this.controllerSet = controllerSet;
	}

	@Test
	void testDefaultOptionsOnActiveVariantsOnly() throws CodeException
	{
		SingleLemmaBuilder builder = new SingleLemmaBuilder();

		{
			// Check 1: one single dialect is in use at both the variant and the sememe
			final String checkResult = String.format(
				"{eaten}^[%s] ((%s))", oNSS.getAbbreviation(), lMoensterlaendsk.getUitID_abbr()
			);
			assertEquals(checkResult, builder.build(
				PresentationOptions.DEFAULT_PRESENTATION_OPTIONS,
				this.controllerSet,
				EntitiesUtil.createVariant(oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
					EntitiesUtil.createLemma("eaten"), true),
				Set.of(lMoensterlaendsk.getId())
			), String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 2: two dialects on the variant, but only one of them is used at the sememe
			final String checkResult = String.format(
				"{eaten}^[%s] ((%s))", oNSS.getAbbreviation(), lMoensterlaendsk.getUitID_abbr()
			);
			assertEquals(checkResult, builder.build(
				PresentationOptions.DEFAULT_PRESENTATION_OPTIONS,
				this.controllerSet,
				EntitiesUtil.createVariant(oNSS.getId(), Set.of(lMoensterlaendsk.getId(), lDitmarsk.getId()),
					EntitiesUtil.createLemma("eaten"), true),
				Set.of(lMoensterlaendsk.getId())
			), String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 3: one dialect on the variant, but two are used at the sememe
			final String checkResult = String.format(
				"{eaten}^[%s] ((%s))", oNSS.getAbbreviation(), lMoensterlaendsk.getUitID_abbr()
			);
			assertEquals(checkResult, builder.build(
				PresentationOptions.DEFAULT_PRESENTATION_OPTIONS,
				this.controllerSet,
				EntitiesUtil.createVariant(oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
					EntitiesUtil.createLemma("eaten"), true),
				Set.of(lMoensterlaendsk.getId(), lDitmarsk.getId())
			), String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 4: two dialects on the variant, and those two are used on the sememe
			final String checkResult = String.format(
				"{eaten}^[%s] ((%s, %s))",
				oNSS.getAbbreviation(), lMoensterlaendsk.getUitID_abbr(), lDitmarsk.getUitID_abbr()
			);
			assertEquals(checkResult, builder.build(
				PresentationOptions.DEFAULT_PRESENTATION_OPTIONS,
				this.controllerSet,
				// Note that lMoensterlaendsk and lDitmarsk are twisted in the dialectIDs set of th variant
				EntitiesUtil.createVariant(oNSS.getId(), Set.of(lDitmarsk.getId(), lMoensterlaendsk.getId()),
					EntitiesUtil.createLemma("eaten"), true),
				Set.of(lMoensterlaendsk.getId(), lDitmarsk.getId())
			), String.format("Lemma should have been '%s'", checkResult));
		}
	}

	@Test
	void testInactiveVariant() throws CodeException {
		SingleLemmaBuilder builder = new SingleLemmaBuilder();
		SingleLemmaOptions options = new SingleLemmaOptions(true, true, true);

		{
			// Check 1: one single dialect is in use at both the variant and the sememe, but the variant is inactive
			final String checkResult = "";
			assertEquals(checkResult, builder.build(
				options,
				this.controllerSet,
				EntitiesUtil.createVariant(oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
					EntitiesUtil.createLemma("eaten"), false),
				Set.of(lMoensterlaendsk.getId())
			), String.format("Lemma should have been '%s'", checkResult));
		}
	}
}
