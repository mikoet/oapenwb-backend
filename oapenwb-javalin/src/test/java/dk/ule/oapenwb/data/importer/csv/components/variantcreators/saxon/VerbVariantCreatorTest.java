// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon;

import dk.ule.oapenwb.data.importer.VariantUtil;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

/**
 * <ul>
 *     <li>No (full) dialect reading is performed in these tests. That means only empty default dialect sets are
 *     given to the variants.</li>
 * </ul>
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(MockitoExtension.class)
public class VerbVariantCreatorTest
{
	private final static int oNSS_ID = 1;
	private final static int COLUMN_INDEX = 1;
	private final static int DIALECT_COLUMN_INDEX = 2;

	// LexemeType VERB
	private final LexemeType ltVerb = new LexemeType(1, null, "VERB", 0, "ltVerb");

	// LexemeFormTypes for LexemeType VERB
	private final LexemeFormType lftVerbInf = new LexemeFormType(1, null, ltVerb.getId(), "inf", "verbInf",
		"Infinitive", true, (short) 0);
	private final LexemeFormType lftVerbInfDiv = new LexemeFormType(2, null, ltVerb.getId(), "inf_div", "verbInfDiv",
		"Infinitive with divider", false, (short) 1);
	private final LexemeFormType lftVerbS3ps = new LexemeFormType(3, null, ltVerb.getId(), "s3ps", "verbS3ps",
		"Singular 3rd person present", false, (short) 2);
	private final LexemeFormType lftVerbS3pt = new LexemeFormType(4, null, ltVerb.getId(), "s3pt", "verbS3pt",
		"Singular 3rd person past tense", false, (short) 3);
	private final LexemeFormType lftVerbPtc2 = new LexemeFormType(5, null, ltVerb.getId(), "ptc2", "verbPtc2",
		"Participle II", false, (short) 4);

	//
	private final CsvRowBasedImporter.TypeFormPair typeFormsPair = new CsvRowBasedImporter.TypeFormPair(ltVerb,
		new LinkedHashMap<>());

	@BeforeAll
	public void initMembers()
	{
		// Setup the TypeFormPair
		LinkedHashMap<String, LexemeFormType> linkedMap = typeFormsPair.getRight();
		linkedMap.put(lftVerbInf.getName(), lftVerbInf);
		linkedMap.put(lftVerbInfDiv.getName(), lftVerbInfDiv);
		linkedMap.put(lftVerbS3ps.getName(), lftVerbS3ps);
		linkedMap.put(lftVerbS3pt.getName(), lftVerbS3pt);
		linkedMap.put(lftVerbPtc2.getName(), lftVerbPtc2);
	}

	@Test
	void testSingleFormDefinitions()
	{
		AbstractVariantCreator creator = new VerbVariantCreator(null, LexemeType.TYPE_VERB, oNSS_ID, COLUMN_INDEX,
			DIALECT_COLUMN_INDEX, new HashMap<>(), Set.of()).initialise(typeFormsPair);

		{
			// Check 1: one single form definition w/o divider, w/o multiple variants and w/o auxiliary verbs
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikkoppen")
					),
					Map.of("auxiliaries", Set.of() /* Empty set since no auxiliary verb was defined */),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"nikkoppen", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-1");
		}

		{
			// Check 2: one single form definition with divider, w/o multiple variants and w/o auxiliary verbs
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikkoppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|koppen")
					),
					Map.of("auxiliaries", Set.of() /* Empty set since no auxiliary verb was defined */),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"nik|koppen", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-2");
		}

		{
			// Check 3: one single form definition with divider, with multiple variants and w/o auxiliary verbs
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikkoppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|koppen")
					),
					Map.of("auxiliaries", Set.of() /* Empty set since no auxiliary verb was defined */),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikköppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|köppen")
					),
					Map.of("auxiliaries", Set.of() /* Empty set since no auxiliary verb was defined */),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"nik|koppen ~ nik|köppen", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-3");
		}

		{
			// Check 4: one single form definition with divider, with multiple variants and with one auxiliary verb
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikkoppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|koppen")
					),
					Map.of("auxiliaries", Set.of("hevven_v")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikköppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|köppen")
					),
					Map.of("auxiliaries", Set.of("hevven_v")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"nik|koppen ~ nik|köppen (hevven)", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-4");
		}

		{
			// Check 5: one single form definition with divider, with multiple variants and with two auxiliary verbs
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikkoppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|koppen")
					),
					Map.of("auxiliaries", Set.of("hevven_v", "weasen_v")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "nikköppen")
						,createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "nik|köppen")
					),
					Map.of("auxiliaries", Set.of("hevven_v", "weasen_v")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"nik|koppen ~ nik|köppen (hevven, weasen)", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testSingleFormDefinitions-5");
		}
	}

	@Test
	void testMultiFormDefinitions()
	{
		AbstractVariantCreator creator = new VerbVariantCreator(null, LexemeType.TYPE_VERB, oNSS_ID, COLUMN_INDEX,
			DIALECT_COLUMN_INDEX, new HashMap<>(), Set.of()).initialise(typeFormsPair);

		{
			// Check 1: one multi form definition w/o divider, w/o multiple variants and with auxiliary verb
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "afhelpen"),
						createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "af|helpen"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "helpt af"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "holp af"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "afholpen")
					),
					Map.of("auxiliaries", Set.of("hevven_v")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "afhölpen"),
						createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "af|hölpen"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "hölpt af"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "holp af"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "afholpen")
					),
					Map.of("auxiliaries", Set.of("hevven_v")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"af|helpen ~ af|hölpen, helpt af ~ hölpt af, holp af, het afholpen", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-1");
		}

		{
			// Check 2: one multi form definition w/o divider, w/o multiple variants and with auxiliary verbs
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "loupen"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "löpt"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "leyp"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "loupen")
					),
					Map.of("auxiliaries", Set.of("hevven_v", "weasen_v")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"loupen, löpt, leyp, loupen (hevven, weasen)", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-2");
		}

		{
			// Check 3: one multi form definition w/o divider, w/o multiple variants and w/o auxiliary verbs
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "winnen"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "wint"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "wun"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "wunnen")
					),
					Map.of("auxiliaries", Set.of()),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"winnen, wint, wun, wunnen", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-3");
		}

		{
			// Check 4: one multi form definition with divider, w/o multiple variants and with auxiliary verb
			// !! Case that made a problem in import
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "lousgån"),
						createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "lous|gån"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "geit lous"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "gung lous"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "lousgån")
					),
					Map.of("auxiliaries", Set.of("weasen_v")),
					ApiAction.Insert)
			);
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"lous|gån, geit lous, gung lous, is lousgån", ""}));
			VariantUtil.compareVariantLists(checkResult, result, "testMultiFormDefinitions-4");
		}

		{
			// Check 5: one multi form definition with divider, w/o multiple variants and with auxiliary verb
			// !! Case that made a problem in import
			List<Variant> checkResult = List.of(
				VariantUtil.createVariant(oNSS_ID, true, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "anslån"),
						createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "an|slån"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "sleit an"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "sloug an"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "anslån")
					),
					Map.of("auxiliaries", Set.of("hevven_v")),
					ApiAction.Insert),
				VariantUtil.createVariant(oNSS_ID, false, true, Set.of(),
					List.of(
						createLexemeForm(lftVerbInf.getId(), LexemeForm.STATE_TYPED, "anslån"),
						createLexemeForm(lftVerbInfDiv.getId(), LexemeForm.STATE_TYPED, "an|slån"),
						createLexemeForm(lftVerbS3ps.getId(), LexemeForm.STATE_TYPED, "sleit an"),
						createLexemeForm(lftVerbS3pt.getId(), LexemeForm.STATE_TYPED, "slöyg an"),
						createLexemeForm(lftVerbPtc2.getId(), LexemeForm.STATE_TYPED, "anslån")
					),
					Map.of("auxiliaries", Set.of("hevven_v")),
					ApiAction.Insert)
			);
			// HINT (change) Changed behaviour in parsing made adaptation of this test necessary.
			List<Variant> result = creator.create(null /* no context for now */,
				new RowData(1, new String[] {"an|slån, sleit an, sloug an ~ slöyg an, het anslån", ""}));
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
