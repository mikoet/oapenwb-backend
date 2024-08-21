// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb.persistency.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * <p>Slim DTO representation of a <b>Sememe</b> that is by now used
 * in the <b>Mapping</b>s and the frontend functionality to create and handle
 * the mappings.</p>
 */
@Getter
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