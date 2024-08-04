// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;

import java.util.Set;

@FunctionalInterface
public interface DuplicateCheckKeyBuilder
{
	/**
	 * <p>Builds the keys for a given {@link LexemeDetailedDTO} instance. However, if a set is really returned depends
	 * on the implemetation of the concrete builder. E.g. it might only build keys for specific languages.</p>
	 *
	 * @param dto the {@link LexemeDetailedDTO} instance to build for
	 * @return a set of keys or null
	 */
	Set<String> buildKeys(LexemeDetailedDTO dto);
}
