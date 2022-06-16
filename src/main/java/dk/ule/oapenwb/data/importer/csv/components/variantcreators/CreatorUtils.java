// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators;

import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;

import java.util.List;
import java.util.Set;

public class CreatorUtils
{
	public static String getVariantForm(String part, int index)
	{
		if (part.contains("~")) {
			return part.split("~")[index].trim();
		}
		return part.trim();
	}

	public static void readAndApplyDialects(List<Variant> variants, RowData rowData, int dialectsColumnIndex)
	{
		// TODO 100 As wy de dialekten hebbet mut dat ruut
		if (1 == 1) {
			return;
		}

		// TODO Read the dialects via property dialectsColumnIndex and apply them to the variants.
		//  Throw an exception if the number of variants and dialects doesn't match.
		String dialectsStr = rowData.getParts()[dialectsColumnIndex - 1];
		// Remove all spaces
		dialectsStr = dialectsStr.replace(" ", "");
		String[] dialects = dialectsStr.split(",");

		if (dialects.length != variants.size()) {
			throw new RuntimeException("Number of variants and dialects doesn't match.");
		}

		int index = 0;
		for (var variant : variants) {
			String dialect = dialects[index];
			variant.setDialectIDs(Set.of()); // TODO map from dialect to dialectID
			index++;
		}
	}
}
