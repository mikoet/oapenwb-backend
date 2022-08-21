// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MultiLexemeProviderTest
{
	// splitColumnContent
	@Test
	void testSplitColumnContent()
	{
		final String columnContent =
			"düt is en (seg ik eyns sou, woso ouk ni (nä, du?)) deyl; dat is ouk en deyl, un dat (wy weatet et al) is ouk eyn";
		String[] parts = MultiLexemeProvider.splitColumnContent(columnContent);

		assertEquals(3, parts.length);
		assertEquals("düt is en (seg ik eyns sou, woso ouk ni (nä, du?)) deyl", parts[0]);
		assertEquals("dat is ouk en deyl", parts[1]);
		assertEquals("un dat (wy weatet et al) is ouk eyn", parts[2]);
	}
}
