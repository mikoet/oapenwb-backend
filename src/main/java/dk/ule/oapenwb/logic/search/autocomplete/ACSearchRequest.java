// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.search.autocomplete;

import dk.ule.oapenwb.logic.search.Direction;
import lombok.Data;

@Data
public class ACSearchRequest
{
	private String pair;	// the LangPair
	private String term;	// the search term
	private Direction direction = Direction.Both;
}
