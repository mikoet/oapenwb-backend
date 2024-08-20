// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity;

/**
 * Specifies an action done to an entity (for some entities) via the REST interface when the REST interface
 * is used rather like a remote procedure call (so done e.g. by the Lexemes).
 */
public enum ApiAction
{
	None,
	Insert,
	Update,
	Delete
}