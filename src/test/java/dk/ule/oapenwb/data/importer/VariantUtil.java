// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer;

import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VariantUtil
{
	private static long NEXT_VARIANT_ID = -1L;

	public static void compareVariantLists(List<Variant> checkResult, List<Variant> result, String runText)
	{
		assertEquals(checkResult.size(), result.size(), String.format(
			"Result lists do not have the same size! checkResult.size = %d and result.size = %d",
			checkResult.size(), result.size()));

		for (int i = 0; i < checkResult.size(); i++) {
			String variantText = String.format("variant no. %d", i+1);
			Variant checkVariant = checkResult.get(i);
			Variant variant = result.get(i);

			assertEquals(checkVariant.getOrthographyID(), variant.getOrthographyID(),
				String.format("Property '%s' does not equal (%s) in run '%s'", "orthographyID", variantText, runText));
			assertEquals(checkVariant.isMainVariant(), variant.isMainVariant(),
				String.format("Property '%s' does not equal (%s) in run '%s'", "mainVariant", variantText, runText));
			assertEquals(checkVariant.isActive(), variant.isActive(),
				String.format("Property '%s' does not equal (%s) in run '%s'", "active", variantText, runText));
			assertEquals(checkVariant.getDialectIDs(), variant.getDialectIDs(),
				String.format("Property '%s' does not equal (%s) in run '%s'", "dialectIDs", variantText, runText));
			assertEquals(checkVariant.getApiAction(), variant.getApiAction(),
				String.format("Property '%s' does not equal (%s) in run '%s'", "apiAction", variantText, runText));

			// Not sure (Hash)Map's equals method is well enough for comparing the depth the properties can have.
			assertEquals(checkVariant.getProperties(), variant.getProperties(),
				String.format("Property '%s' does not equal (%s) in run '%s'", "properties", variantText, runText));

			// The two lists are compared for equality without looking at the order. Class LexemeForm must and does
			// implements equals method well.
			assertTrue(listsEqualIgnoringOrder(checkVariant.getLexemeForms(), variant.getLexemeForms()),
				String.format("Property '%s' does not equal (%s) in run '%s'", "lexemeForms", variantText, runText));
		}
	}

	public static <T> boolean listsEqualIgnoringOrder(List<T> listA, List<T> listB)
	{
		if (listA == null && listB == null) {
			return false;
		}
		if (listA == null || listB == null) {
			return false;
		}
		return listA.containsAll(listB) && listB.containsAll(listA);
	}

	public static Variant createVariant(
		int orthographyID,
		boolean mainVariant,
		boolean active,
		Set<Integer> dialectIDs,
		List<LexemeForm> lexemeForms,
		Map<String, Object> properties,
		ApiAction apiAction
	)
	{
		Variant v = new Variant();

		v.setId(VariantUtil.NEXT_VARIANT_ID--);
		v.setOrthographyID(orthographyID);
		v.setMainVariant(mainVariant);
		v.setActive(active);
		v.setDialectIDs(dialectIDs);
		v.setLexemeForms(lexemeForms);
		v.setProperties(properties);
		v.setApiAction(apiAction);
		// Lemma is not necessary since its created in a later stage in the LexemeController's persisting, thus not part
		// of the VerbVariantCreator.

		// Set the variantID on the lexemeForms
		for (var lexemeForm : lexemeForms) {
			lexemeForm.setVariantID(v.getId());
		}

		return v;
	}
}
