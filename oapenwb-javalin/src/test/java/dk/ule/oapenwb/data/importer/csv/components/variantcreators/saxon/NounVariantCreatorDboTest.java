// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon;

import dk.ule.oapenwb.data.importer.VariantUtil;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.persistency.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class NounVariantCreatorDboTest
{
	private final static int oDBO_ID = 2;
	private final static int COLUMN_INDEX = 1;
	private final static int DIALECT_COLUMN_INDEX = 2;

	// LexemeType NOUN
	private final LexemeType ltNoun = new LexemeType(1, null, "NOUN", 0, "ltNoun");

	// LexemeFormTypes for LexemeType NOUN
	private final LexemeFormType lftSinNom = new LexemeFormType(1, null, ltNoun.getId(), "sn", "nounSinNom",
		"Singular nominative", true, (short) 0);
	private final LexemeFormType lftPluNom = new LexemeFormType(2, null, ltNoun.getId(), "pn", "nounPluNom",
		"Plural nominative", false, (short) 1);

	// Dialects
	private final Language lLowSaxon = new Language(1, null, "nds", "", "", "", oDBO_ID, "nds");
	private final Language lNorthernLowSaxon = new Language(2, lLowSaxon.getId(), "nds_DE@dns", "", "", "", oDBO_ID, "dns");
	private final Language lDitmarsk = new Language(3, lNorthernLowSaxon.getId(), "nds_DE@dns-dit", "", "", "", oDBO_ID, "dit");
	private final Language lNoordhannoversk = new Language(4, lNorthernLowSaxon.getId(), "nds_DE@dns-nhn", "", "", "", oDBO_ID, "nhn");
	private final Language lWestphalian = new Language(10, lLowSaxon.getId(), "nds_DE@dwf", "", "", "", oDBO_ID, "dwf");
	private final Language lEastphalian = new Language(20, lLowSaxon.getId(), "nds_DE@of", "", "", "", oDBO_ID, "of");

	private final Map<String, Language> dialectMap = Map.of(
		lLowSaxon.getImportAbbreviation(), lLowSaxon,
		lNorthernLowSaxon.getImportAbbreviation(), lNorthernLowSaxon,
		lDitmarsk.getImportAbbreviation(), lDitmarsk,
		lNoordhannoversk.getImportAbbreviation(), lNoordhannoversk,
		lWestphalian.getImportAbbreviation(), lWestphalian,
		lEastphalian.getImportAbbreviation(), lEastphalian
	);

	private final Set<Integer> defaultDialectID = Set.of(lNorthernLowSaxon.getId());

	//
	private final CsvRowBasedImporter.TypeFormPair typeFormsPair = new CsvRowBasedImporter.TypeFormPair(ltNoun,
		new LinkedHashMap<>());

	@BeforeAll
	public void initMembers()
	{
		// Setup the TypeFormPair
		LinkedHashMap<String, LexemeFormType> linkedMap = typeFormsPair.getRight();
		linkedMap.put(lftSinNom.getName(), lftSinNom);
		linkedMap.put(lftPluNom.getName(), lftPluNom);
	}

	@Test
	void testSingleFormDefinitions()
	{
		AbstractVariantCreator creator = new NounVariantCreator(null, LexemeType.TYPE_NOUN, ImportMode.DBO, oDBO_ID,
			COLUMN_INDEX, DIALECT_COLUMN_INDEX, dialectMap, defaultDialectID).initialise(typeFormsPair);

		{
			// Check 1: one single form definition w/o multiple variants and w/o genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, defaultDialectID,
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Kegelrubb")
					),
					Map.of("genera", Set.of() /* Empty set since no genera were defined */),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"Kegelrubb", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-1");
		}

		{
			// Check 2: one single form definition w/o multiple variants and with feminine genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(lNorthernLowSaxon.getId(), lWestphalian.getId()),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Dogg(e)")
					),
					Map.of("genera", Set.of("f")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"de Dogg(e)", "dns, dwf"}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-2");
		}

		{
			// Check 3: one single form definition w/o multiple variants and with masculine genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, defaultDialectID,
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "kastangappel")
					),
					Map.of("genera", Set.of("m")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"de(n) kastangappel", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-3");
		}

		{
			// Check 4: one single form definition w/o multiple variants and neuter genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, defaultDialectID,
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Binnenland")
					),
					Map.of("genera", Set.of("n")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"dat Binnenland", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-4");
		}

		{
			// Check 5: one single form definition with multiple variants and with feminine genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(lNorthernLowSaxon.getId(), lWestphalian.getId()),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Kärmse")
					),
					Map.of("genera", Set.of("f")),
					ApiAction.Insert),
				VariantUtil.createVariant(oDBO_ID, false, true, Set.of(lEastphalian.getId()),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Karmse")
					),
					Map.of("genera", Set.of("f")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"de Kärmse~Karmse", "dns, dwf ~ of"}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-5");
		}
	}

	@Test
	void testMultiFormDefinitions()
	{
		AbstractVariantCreator creator = new NounVariantCreator(null, LexemeType.TYPE_NOUN, ImportMode.DBO, oDBO_ID,
			COLUMN_INDEX, DIALECT_COLUMN_INDEX, new HashMap<>(), Set.of()).initialise(typeFormsPair);

		{
			// Check 1: one multi form definition w/o multiple variants and w/o genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Disch"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Dischen")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"Disch, Dischen", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-1");
		}

		{
			// Check 2: one multi form definition w/o multiple variants and w/o genus but usage of '-' in plural form
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Bootshall"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Bootshallen")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"Bootshall, -en", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-2");
		}

		{
			// Check 3: one multi form definition w/o multiple variants and with genus but usage of '-' in plural form
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Dänne"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Dännen")
					),
					Map.of("genera", Set.of("f")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"de Dänne, -n", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-3");
		}

		{
			// Check 4: one multi form definition w/o multiple variants and with genus but usage of '-' in plural form
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Gordencenter"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Gordencenters")
					),
					Map.of("genera", Set.of("n")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"dat Gordencenter, -s", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-4");
		}

		{
			// Check 5: multi form definition with multiple variants and with genera and usuage of / instead of ~
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Deef"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Deev")
					),
					Map.of("genera", Set.of("m")),
					ApiAction.Insert),
				VariantUtil.createVariant(oDBO_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Deef"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Deven")
					),
					Map.of("genera", Set.of("m")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"de(n) Deef, Deev/Deven", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-5");
		}

		{
			// Check 6: multi form definition with multiple variants and with genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oDBO_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Deef"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Deev")
					),
					Map.of("genera", Set.of("m")),
					ApiAction.Insert),
				VariantUtil.createVariant(oDBO_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "Deef"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "Deven")
					),
					Map.of("genera", Set.of("m")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"de(n) Deef, Deev ~ Deven", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-5");
		}
	}

	private LexemeForm createLexemeForm(int formTypeID, byte state, String text)
	{
		LexemeForm lf = new LexemeForm();

		lf.setFormTypeID(formTypeID);
		lf.setState(state);
		lf.setText(text);

		return lf;
	}
}
