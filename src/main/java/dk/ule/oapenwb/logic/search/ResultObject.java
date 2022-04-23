// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import lombok.Data;

import java.util.List;

/**
 * TODO REFACT
 */
@Data
public class ResultObject
{
	@Data
	static class SememeEntry
	{
		//long id; // only needed later once details for sememes can be viewed
		String lemma;
	}

	@Data
	static class ResultEntry
	{
		SememeEntry sememeOne;
		SememeEntry sememeTwo;
		int typeID;
		short weight;
	}

	private List<ResultEntry> entries;
}
