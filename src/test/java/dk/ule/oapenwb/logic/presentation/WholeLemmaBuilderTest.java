// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.Category;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.Level;
import dk.ule.oapenwb.entity.content.basedata.Orthography;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.generic.ICEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememeController;
import dk.ule.oapenwb.logic.presentation.options.SingleLemmaOptions;
import dk.ule.oapenwb.logic.presentation.options.WholeLemmaOptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * <p>Tests for class {@link WholeLemmaBuilder}.</p>
 * TODO
 *   - Write tests for non-default configurational options
 *   - Write more tests
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class WholeLemmaBuilderTest
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
		"l:nds", "l:nds_a", oNSS.getId());
	private final Language lWestphalian = new Language(2, lSaxon.getId(), "nds-wf", "Westföälsk",
		"l:nds-wf", "l:nds-wf_a", oNSS.getId());
	private final Language lMoensterlaendsk = new Language(3, lWestphalian.getId(), "nds-wf-ml", "Mönsterländsk",
		"l:nds-wf-ml", "l:nds-wf-ml_a", oNSS.getId());
	private final Language lNorthernLowSaxon = new Language(4, lSaxon.getId(), "nds-nns", "Noordneddersassisk",
		"l:nds-nns", "l:nds-nns_a", oNSS.getId());
	private final Language lDitmarsk = new Language(5, lNorthernLowSaxon.getId(), "nds-nns-dm", "Ditmarsk",
		"l:nds-nns-dm", "l:nds-nns-dm_a", oNSS.getId());

	// Categories
	private final Category cFlora = new Category(1, null, null, "c:flora", "c:flora_a", "Botanic lexemes");
	private final Category cFauna = new Category(2, null, null, "c:fauna", "c:fauna_a", "Lexemes of the animal kingdom");

	// Levels
	private final Level slColloquial = new Level(1, null, "sl:colloq", "sl:colloq_a", "Colloquial words");
	private final Level slExalted = new Level(2, null, "sl:exalted", "sl:exalted_a", "Exalted words");


	private IControllerSet controllerSet;

	@BeforeAll
	public void initControllers(
		@Mock ICEntityController<Orthography, Integer> orthographiesController,
		@Mock ICEntityController<Language, Integer> languagesController,
		@Mock ICEntityController<Category, Integer> categoriesController,
		@Mock ICEntityController<Level, Integer> levelsController,
		@Mock SememeController sememeController
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

		// Setting up mocking for categories controller
		Mockito.lenient().when(categoriesController.get(cFlora.getId())).thenReturn(cFlora);
		Mockito.lenient().when(categoriesController.get(cFauna.getId())).thenReturn(cFauna);

		// Setting up mocking for levels controller
		Mockito.lenient().when(levelsController.get(slColloquial.getId())).thenReturn(slColloquial);
		Mockito.lenient().when(levelsController.get(slExalted.getId())).thenReturn(slExalted);

		ControllerSet controllerSet = new ControllerSet();
		controllerSet.setControllers(orthographiesController, languagesController, categoriesController,
			levelsController, sememeController);
		this.controllerSet = controllerSet;
	}

	@Test
	void testWithActiveDataOnlyFlag() throws CodeException
	{
		SingleLemmaBuilder sBuilder = new SingleLemmaBuilder();
		WholeLemmaBuilder wBuilder = new WholeLemmaBuilder();
		WholeLemmaOptions options = new WholeLemmaOptions(true, false, false, true, true,
			WholeLemmaOptions.ALPHABETIC_SINGLE_LEMMA_COMPARATOR, WholeLemmaOptions.DEFAULT_SINGLE_LEMMA_DIVIDER);

		{
			// Check 1: a sememe with two active variants, two categories and two levels
			Variant vOne = EntitiesUtil.createVariant(1L, oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
				EntitiesUtil.createLemma("eaten"), true);
			Variant vTwo = EntitiesUtil.createVariant(2L, oNSS.getId(), Set.of(lDitmarsk.getId()),
				EntitiesUtil.createLemma("etten"), true);

			Map<Long, Variant> allVariantsMap = Map.of(
				vOne.getId(), vOne,
				vTwo.getId(), vTwo
			);

			Sememe sememe = EntitiesUtil.createSememe(
				Set.of(vOne.getId(), vTwo.getId()),
				Set.of(cFlora.getId(), cFauna.getId()),
				Set.of(slColloquial.getId(), slExalted.getId())
			);

			String checkResult = String.format("%s, %s [[%s, %s]] [/%s, %s/]",
				sBuilder.build(options, this.controllerSet, vOne, sememe.getDialectIDs()),
				sBuilder.build(options, this.controllerSet, vTwo, sememe.getDialectIDs()),
				this.cFlora.getUitID_abbr(),
				this.cFauna.getUitID_abbr(),
				this.slColloquial.getUitID_abbr(),
				this.slExalted.getUitID_abbr()
			);
			String wholeLemma = wBuilder.build(options, this.controllerSet, sememe, allVariantsMap);

			assertEquals(checkResult, wholeLemma, String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 2: a sememe with one active and one inactive variant, two categories and two levels
			Variant vOne = EntitiesUtil.createVariant(1L, oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
				EntitiesUtil.createLemma("eaten"), true);
			Variant vTwo = EntitiesUtil.createVariant(2L, oNSS.getId(), Set.of(lDitmarsk.getId()),
				EntitiesUtil.createLemma("etten"), false);

			Map<Long, Variant> allVariantsMap = Map.of(
				vOne.getId(), vOne,
				vTwo.getId(), vTwo
			);

			Sememe sememe = EntitiesUtil.createSememe(
				Set.of(vOne.getId(), vTwo.getId()),
				Set.of(cFlora.getId(), cFauna.getId()),
				Set.of(slColloquial.getId(), slExalted.getId())
			);

			String checkResult = String.format("%s [[%s, %s]] [/%s, %s/]",
				sBuilder.build(options, this.controllerSet, vOne, sememe.getDialectIDs()),
				this.cFlora.getUitID_abbr(),
				this.cFauna.getUitID_abbr(),
				this.slColloquial.getUitID_abbr(),
				this.slExalted.getUitID_abbr()
			);
			String wholeLemma = wBuilder.build(options, this.controllerSet, sememe, allVariantsMap);

			assertEquals(checkResult, wholeLemma, String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 3: a sememe with two inactive variants, two categories and two levels
			Variant vOne = EntitiesUtil.createVariant(1L, oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
				EntitiesUtil.createLemma("eaten"), false);
			Variant vTwo = EntitiesUtil.createVariant(2L, oNSS.getId(), Set.of(lDitmarsk.getId()),
				EntitiesUtil.createLemma("etten"), false);

			Map<Long, Variant> allVariantsMap = Map.of(
				vOne.getId(), vOne,
				vTwo.getId(), vTwo
			);

			Sememe sememe = EntitiesUtil.createSememe(
				Set.of(vOne.getId(), vTwo.getId()),
				Set.of(cFlora.getId(), cFauna.getId()),
				Set.of(slColloquial.getId(), slExalted.getId())
			);

			String checkResult = ""; // Empty result
			String wholeLemma = wBuilder.build(options, this.controllerSet, sememe, allVariantsMap);

			assertEquals(checkResult, wholeLemma, String.format("Lemma should have been '%s'", checkResult));
		}
	}

	/**
	 * In this test the full lemma including all variants is always expected as their inactivity doesn't play a role.
	 *
	 * @throws CodeException Can be thrown by controllers by declaration (but not by the mocked ones, though)
	 */
	@Test
	void testWithoutActiveDataOnlyFlag() throws CodeException
	{
		SingleLemmaBuilder sBuilder = new SingleLemmaBuilder();
		WholeLemmaBuilder wBuilder = new WholeLemmaBuilder();
		WholeLemmaOptions options = new WholeLemmaOptions(false, false, false, true, true,
			WholeLemmaOptions.ALPHABETIC_SINGLE_LEMMA_COMPARATOR, WholeLemmaOptions.DEFAULT_SINGLE_LEMMA_DIVIDER);

		{
			// Check 1: a sememe with two active variants, two categories and two levels
			Variant vOne = EntitiesUtil.createVariant(1L, oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
				EntitiesUtil.createLemma("eaten"), true);
			Variant vTwo = EntitiesUtil.createVariant(2L, oNSS.getId(), Set.of(lDitmarsk.getId()),
				EntitiesUtil.createLemma("etten"), true);

			Map<Long, Variant> allVariantsMap = Map.of(
				vOne.getId(), vOne,
				vTwo.getId(), vTwo
			);

			Sememe sememe = EntitiesUtil.createSememe(
				Set.of(vOne.getId(), vTwo.getId()),
				Set.of(cFlora.getId(), cFauna.getId()),
				Set.of(slColloquial.getId(), slExalted.getId())
			);

			String checkResult = String.format("%s, %s [[%s, %s]] [/%s, %s/]",
				sBuilder.build(options, this.controllerSet, vOne, sememe.getDialectIDs()),
				sBuilder.build(options, this.controllerSet, vTwo, sememe.getDialectIDs()),
				this.cFlora.getUitID_abbr(),
				this.cFauna.getUitID_abbr(),
				this.slColloquial.getUitID_abbr(),
				this.slExalted.getUitID_abbr()
			);
			String wholeLemma = wBuilder.build(options, this.controllerSet, sememe, allVariantsMap);

			assertEquals(checkResult, wholeLemma, String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 2: a sememe with one active and one inactive variant, two categories and two levels
			Variant vOne = EntitiesUtil.createVariant(1L, oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
				EntitiesUtil.createLemma("eaten"), true);
			Variant vTwo = EntitiesUtil.createVariant(2L, oNSS.getId(), Set.of(lDitmarsk.getId()),
				EntitiesUtil.createLemma("etten"), false);

			Map<Long, Variant> allVariantsMap = Map.of(
				vOne.getId(), vOne,
				vTwo.getId(), vTwo
			);

			Sememe sememe = EntitiesUtil.createSememe(
				Set.of(vOne.getId(), vTwo.getId()),
				Set.of(cFlora.getId(), cFauna.getId()),
				Set.of(slColloquial.getId(), slExalted.getId())
			);

			String checkResult = String.format("%s, %s [[%s, %s]] [/%s, %s/]",
				sBuilder.build(options, this.controllerSet, vOne, sememe.getDialectIDs()),
				sBuilder.build(options, this.controllerSet, vTwo, sememe.getDialectIDs()),
				this.cFlora.getUitID_abbr(),
				this.cFauna.getUitID_abbr(),
				this.slColloquial.getUitID_abbr(),
				this.slExalted.getUitID_abbr()
			);
			String wholeLemma = wBuilder.build(options, this.controllerSet, sememe, allVariantsMap);

			assertEquals(checkResult, wholeLemma, String.format("Lemma should have been '%s'", checkResult));
		}

		{
			// Check 3: a sememe with two inactive variants, two categories and two levels
			Variant vOne = EntitiesUtil.createVariant(1L, oNSS.getId(), Set.of(lMoensterlaendsk.getId()),
				EntitiesUtil.createLemma("eaten"), false);
			Variant vTwo = EntitiesUtil.createVariant(2L, oNSS.getId(), Set.of(lDitmarsk.getId()),
				EntitiesUtil.createLemma("etten"), false);

			Map<Long, Variant> allVariantsMap = Map.of(
				vOne.getId(), vOne,
				vTwo.getId(), vTwo
			);

			Sememe sememe = EntitiesUtil.createSememe(
				Set.of(vOne.getId(), vTwo.getId()),
				Set.of(cFlora.getId(), cFauna.getId()),
				Set.of(slColloquial.getId(), slExalted.getId())
			);

			String checkResult = String.format("%s, %s [[%s, %s]] [/%s, %s/]",
				sBuilder.build(options, this.controllerSet, vOne, sememe.getDialectIDs()),
				sBuilder.build(options, this.controllerSet, vTwo, sememe.getDialectIDs()),
				this.cFlora.getUitID_abbr(),
				this.cFauna.getUitID_abbr(),
				this.slColloquial.getUitID_abbr(),
				this.slExalted.getUitID_abbr()
			);
			String wholeLemma = wBuilder.build(options, this.controllerSet, sememe, allVariantsMap);

			assertEquals(checkResult, wholeLemma, String.format("Lemma should have been '%s'", checkResult));
		}
	}
}
