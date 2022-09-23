// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import lombok.Data;

import java.util.List;
import java.util.Optional;

/**
 * <p>The SearchResult for a search request in the dictionary's frontend.</p>
 */
@Data
public class SearchResult
{
	@Data
	static class SememeEntry
	{
		//long id; // only needed later once details for sememes can be viewed
		int typeID;
		String lemma;

		// optional locale of the lexeme's language
		Optional<String> locale = Optional.empty();
	}

	@Data
	static class ResultEntry
	{
		SememeEntry sememeOne;
		SememeEntry sememeTwo;
		short weight;
	}

	private List<ResultEntry> entries;
}
