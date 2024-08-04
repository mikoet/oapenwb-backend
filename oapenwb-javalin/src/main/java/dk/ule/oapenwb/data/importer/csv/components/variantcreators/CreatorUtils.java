// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators;

import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreatorUtils
{
	public static String getVariantForm(String part, int index)
	{
		if (part.contains("~")) {
			String[] parts = part.split("~");
			if (index >= parts.length) {
				throw new RuntimeException(String.format("Part '%s' has no variant# %d", part, index + 1));
			}
			return part.split("~")[index].trim();
		}
		return part.trim();
	}

	public static void readAndApplyDialects(List<Variant> variants, RowData rowData, int dialectsColumnIndex,
		Map<String, Language> dialectMap, Set<Integer> defaultDialectID)
	{
		if (variants == null || variants.size() == 0) {
			return;
		} else {
			if (dialectsColumnIndex > rowData.getParts().length) {
				throw new RuntimeException(
					String.format("Dialect(s) column with index %d does not exist for this row", dialectsColumnIndex));
			}

			String dialectData = rowData.getParts()[dialectsColumnIndex - 1];
			// Remove all spaces
			dialectData = dialectData.replace(" ", "");

			// TODO 125 What to do about the sememes? How to set the dialects there?

			if (dialectData.isBlank()) {
				// Set the standard dialects for each variant
				for (var variant : variants) {
					variant.setDialectIDs(defaultDialectID);
				}
			} else {
				// Dialects are specified and we're going to read them
				if (variants.size() == 1) {
					// Only one variant is there
					if (dialectData.contains("~")) {
						throw new RuntimeException(String.format(
							"Dialect column %d contains variant separator characters ('~'), but should contain only one dialect",
							dialectsColumnIndex));
					}
					Set<Integer> dialectIDs = defaultDialectID;
					if (!dialectData.isBlank()) {
						dialectIDs = parseDialects(dialectMap, dialectData, dialectsColumnIndex);
					}
					variants.get(0).setDialectIDs(dialectIDs);
				} else {
					if (StringUtils.countMatches(dialectData, '~') + 1 != variants.size()) {
						throw new RuntimeException(String.format(
							"Specified dialects don't match the number of specified variants (dialects column = %d)",
							dialectsColumnIndex));
					}
					String[] dialectsParts = dialectData.split("~");
					for (int i = 0; i < variants.size(); i++) {
						String dialectsPart = dialectsParts[i];
						Set<Integer> dialectIDs = defaultDialectID;
						if (!dialectsPart.isBlank()) {
							dialectIDs = parseDialects(dialectMap, dialectsPart, dialectsColumnIndex);
						}
						variants.get(i).setDialectIDs(dialectIDs);
					}
				}
			}
		}
	}

	// dialectData can contain a single dialect or multiple ones separated by a ','
	private static Set<Integer> parseDialects(Map<String, Language> dialectMap, String dialectData,
		int dialectsColumnIndex)
	{
		if (dialectData.contains(",")) {
			// Several dialects are given
			Set<Integer> dialectIDs = new HashSet<>();
			String[] dialectParts = dialectData.split(",");
			for (String dialectPart : dialectParts) {
				dialectIDs.add(getDialectID(dialectMap, dialectPart, dialectsColumnIndex));
			}
			return dialectIDs;
		} else {
			// Simple case: only one dialect is specified
			return Set.of(getDialectID(dialectMap, dialectData, dialectsColumnIndex));
		}
	}

	private static int getDialectID(Map<String, Language> dialectMap, String dialectStr, int dialectsColumnIndex) {
		Language dialect = dialectMap.get(dialectStr);
		if (dialect == null) {
			throw new RuntimeException(String.format(
				"Specified dialect is unknown (dialects column = %d)",
				dialectsColumnIndex));
		}
		return dialect.getId();
	}
}
