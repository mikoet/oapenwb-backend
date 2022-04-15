// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search;

import lombok.Data;

/**
 * <p>The QueryObject contains all information that is transmitted for a search query from the dictionary's
 * frontend on thethe client to the backend.</p>
 */
@Data
public class QueryObject
{
	/*
	 * Keep it simple: we're gonna start with only three properties and will add those later on that are really
	 * needed
	 */
	private String pair;	// the LangPair
	private String term;	// the search term
	private Direction direction = Direction.Both;

	//private Integer occurrence; // 0 min one search term, 1 all search terms, 2 special syntax
	//private Integer dialectOne; // Check if set: Is this dialect a valid dialect of lang 1?
	//private Integer dialectTwo; // Check if set: Is this dialect a valid dialect of lang 2?

	// Maybe replace these properties with a reference to a configuration set, e.g.:
	// preset 'Nysassiske Skryvwyse', preset 'düütsk-baseerd sassisk', etc.
	/*
	private Integer[] orthosOne; // Check if set: Are these orthographies valid orthographies for dialect 1?
	private Integer[] orthosTwo; // Check if set: Are these orthographies valid orthographies for dialect 2?
	private Integer displayOrthoOne; // Check if set: Are these orthographies valid orthographies for dialect 1?
	private Integer displayOrthoTwo; // Check if set: Are these orthographies valid orthographies for dialect 1?
	 */
}