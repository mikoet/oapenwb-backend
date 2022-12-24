// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search.autocomplete;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>The ACSearchResult delivers the result for an auto-completion query.</p>
 */
@Data
public class ACSearchResult
{
	@Data
	static class VariantEntry
	{
		//long id; // only needed later once details for sememes can be viewed
		int typeID;
		String lemma;
		String searchWord; // = Lemma.main

		// optional locale of the lexeme's language
		String locale;
	}

	private List<ACSearchResult.VariantEntry> entries = new LinkedList<>();
}
