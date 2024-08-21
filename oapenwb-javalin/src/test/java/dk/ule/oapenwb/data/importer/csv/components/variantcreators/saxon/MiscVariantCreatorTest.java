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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class MiscVariantCreatorTest
{
	private final static int oNSS_ID = 1;
	private final static int COLUMN_INDEX = 1;
	private final static int DIALECT_COLUMN_INDEX = 2;

	// LexemeTypes
	private final LexemeType ltAdv = new LexemeType(1, null, "NOUN", 0, "ltNoun");

	// LexemeFormTypes for ADV
	private final LexemeFormType lftAdvBaseForm = new LexemeFormType(1, null, ltAdv.getId(), "bf", "baseForm", null,
		true, (short) 0);

	// Dialects
	private final Language lLowSaxon = new Language(1, null, "nds", "", "", "", oNSS_ID, "nds");
	private final Language lNorthernLowSaxon = new Language(2, lLowSaxon.getId(), "nds_DE@dns", "", "", "", oNSS_ID, "dns");
	private final Language lDitmarsk = new Language(3, lNorthernLowSaxon.getId(), "nds_DE@dns-dit", "", "", "", oNSS_ID, "dit");
	private final Language lNoordhannoversk = new Language(4, lNorthernLowSaxon.getId(), "nds_DE@dns-nhn", "", "", "", oNSS_ID, "nhn");
	private final Language lWestphalian = new Language(10, lLowSaxon.getId(), "nds_DE@dwf", "", "", "", oNSS_ID, "dwf");
	private final Language lEastphalian = new Language(20, lLowSaxon.getId(), "nds_DE@of", "", "", "", oNSS_ID, "of");

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
	private final CsvRowBasedImporter.TypeFormPair typeFormsPair = new CsvRowBasedImporter.TypeFormPair(ltAdv,
		new LinkedHashMap<>());

	@BeforeAll
	public void initMembers()
	{
		// Setup the TypeFormPair
		LinkedHashMap<String, LexemeFormType> linkedMap = typeFormsPair.getRight();
		linkedMap.put(lftAdvBaseForm.getName(), lftAdvBaseForm);
	}

	@Test
	void testVariousAdjectives()
	{
		AbstractVariantCreator creator = new MiscVariantCreator(null, LexemeType.TYPE_ADJ, oNSS_ID, COLUMN_INDEX,
			DIALECT_COLUMN_INDEX, dialectMap, defaultDialectID).initialise(typeFormsPair);

		{
			// Check 1: definition w/o multiple variants and with dialect specification
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(lNorthernLowSaxon.getId(), lWestphalian.getId()),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "ysig")
					),
					Map.of(),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"ysig", "dns, dwf"}));
			VariantUtil.compareVariantLists(checkResult, result, "testVariousAdjectives-1");
		}

		{
			// Check 2: definition w/o multiple variants but an actual UTDR (several words) and with default dialect
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, defaultDialectID,
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "dat givt")
					),
					Map.of(),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"dat givt", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testVariousAdjectives-2");
		}

		{
			// Check 3: definition with multiple variants and dialect specification
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(lNorthernLowSaxon.getId()),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "bold")
					),
					Map.of(),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(lEastphalian.getId(), lWestphalian.getId()),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "bolde")
					),
					Map.of(),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"bold ~ bolde", "dns ~ dwf, of"}));
			VariantUtil.compareVariantLists(checkResult, result, "testVariousAdjectives-3");
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
