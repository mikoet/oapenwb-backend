// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon;

import dk.ule.oapenwb.data.importer.VariantUtil;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
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
			DIALECT_COLUMN_INDEX).initialise(typeFormsPair);

		{
			// Check 1: definition w/o multiple variants
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "ysig")
					),
					Map.of(),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"ysig", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testVariousAdjectives-1");
		}

		{
			// Check 2: definition w/o multiple variants but an actual UTDR (several words)
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "dat givt")
					),
					Map.of(),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"dat givt", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testVariousAdjectives-2");
		}

		{
			// Check 3: definition with multiple variants
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "bold")
					),
					Map.of(),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftAdvBaseForm.getId(), LexemeForm.STATE_TYPED, "bolde")
					),
					Map.of(),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"bold ~ bolde", "" /* TODO dialect */}));
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
