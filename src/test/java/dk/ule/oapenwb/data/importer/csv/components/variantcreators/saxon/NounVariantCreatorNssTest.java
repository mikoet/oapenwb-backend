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
public class NounVariantCreatorNssTest
{
	private final static int oNSS_ID = 1;
	private final static int COLUMN_INDEX = 1;
	private final static int DIALECT_COLUMN_INDEX = 2;

	// LexemeType NOUN
	private final LexemeType ltNoun = new LexemeType(1, null, "NOUN", 0, "ltNoun");

	// LexemeFormTypes for LexemeType NOUN
	private final LexemeFormType lftSinNom = new LexemeFormType(1, null, ltNoun.getId(), "sn", "nounSinNom",
		"Singular nominative", true, (short) 0);
	private final LexemeFormType lftPluNom = new LexemeFormType(2, null, ltNoun.getId(), "pn", "nounPluNom",
		"Plural nominative", false, (short) 1);

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
		AbstractVariantCreator creator = new NounVariantCreator(null, LexemeType.TYPE_NOUN, ImportMode.NSS, oNSS_ID,
			COLUMN_INDEX, DIALECT_COLUMN_INDEX).initialise(typeFormsPair);

		{
			// Check 1: one single form definition w/o multiple variants and w/o genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "dingsdag")
					),
					Map.of("genera", Set.of() /* Empty set since no genera were defined */),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"dingsdag", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-1");
		}

		{
			// Check 2: one single form definition w/o multiple variants and with one genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "byspil")
					),
					Map.of("genera", Set.of("n")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"byspil n", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-2");
		}

		{
			// Check 3: one single form definition w/o multiple variants and with two genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "hauptanklaagde")
					),
					Map.of("genera", Set.of("f", "m")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"hauptanklaagde (f, m)", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-3");
		}

		{
			// Check 4: one single form definition with multiple variants and w/o genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "geagendeyl")
					),
					Map.of("genera", Set.of() /* Empty set since no auxiliary verb was defined */),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "teagendeyl")
					),
					Map.of("genera", Set.of() /* Empty set since no auxiliary verb was defined */),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"geagendeyl ~ teagendeyl", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-4");
		}

		{
			// Check 5: one single form definition with multiple variants and with one genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "geagendeyl")
					),
					Map.of("genera", Set.of("n")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "teagendeyl")
					),
					Map.of("genera", Set.of("n")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"geagendeyl ~ teagendeyl n", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-5");
		}

		{
			// Check 6: one single form definition with multiple variants and with one genus
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "geagendeyl")
					),
					Map.of("genera", Set.of("f", "m", "n")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "teagendeyl")
					),
					Map.of("genera", Set.of("f", "m", "n")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"geagendeyl ~ teagendeyl ( f,  m,n)", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-6");
		}
	}

	@Test
	void testMultiFormDefinitions()
	{
		AbstractVariantCreator creator = new NounVariantCreator(null, LexemeType.TYPE_NOUN, ImportMode.NSS, oNSS_ID,
			COLUMN_INDEX, DIALECT_COLUMN_INDEX).initialise(typeFormsPair);

		{
			// Check 1: one multi form definition w/o multiple variants and w/o genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "deepde"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "deepden")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"deepde, deepden", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-1");
		}

		{
			// Check 2: multi form definition with multiple variants and w/o genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "deert"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "deerten")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "deert"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "deerter")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"deert, deerten ~ deerter", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-2");
		}

		{
			// Check 3: multi form definition with multiple variants and with two genera
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "minsk"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "minsken")
					),
					Map.of("genera", Set.of("m", "n")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "minske"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "minsken")
					),
					Map.of("genera", Set.of("m", "n")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {"minsk ~ minske, minsken (m, n)", "" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-3");
		}

		{
			// Check 4: temporary multi form definition test with multiple variants and w/o genera and parenthesisses
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "(ge)bakkene tüffel"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "(ge)bakkene tüffelen")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(/* TODO dialect */),
					List.of(
						createLexemeForm(lftSinNom.getId(), LexemeForm.STATE_TYPED, "(ge)bakkenen aerdappel"),
						createLexemeForm(lftPluNom.getId(), LexemeForm.STATE_TYPED, "(ge)bakkene aerdappelen")
					),
					Map.of("genera", Set.of()),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* TODO bruket wy den kontekst går nich? */,
				new RowData(1, new String[] {
					"(ge)bakkene tüffel ~ (ge)bakkenen aerdappel, (ge)bakkene tüffelen ~ (ge)bakkene aerdappelen",
					"" /* TODO dialect */}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-4");
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
