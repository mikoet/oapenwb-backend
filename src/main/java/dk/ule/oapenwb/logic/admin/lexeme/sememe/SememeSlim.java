// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme.sememe;

import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Slim DTO representation of a {@link Sememe} that is by now used
 * in the {@link Mapping}s and the frontend functionality to create and handle
 * the mappings.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SememeSlim
{
	private long id;
	private String internalName;
	private boolean active;
	private String spec;
	private long lexemeID;

	// Lexeme data
	private long typeID;
	private int langID;
	private boolean lexActive;
	// Main variant data
	private String pre;
	private String main;
	private String post;
}