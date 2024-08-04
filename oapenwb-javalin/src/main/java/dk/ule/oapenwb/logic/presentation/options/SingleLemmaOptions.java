// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation.options;

import lombok.AllArgsConstructor;

/**
 * <p>Options that are used only for building a single lemma for a variant of a sememe. Such a lemma may
 * include an orthography and the dialects it's valid for.</p>
 */
@AllArgsConstructor
public class SingleLemmaOptions
{
	public final boolean activeDataOnly;
	public final boolean includeOrthography;
	public final boolean includeDialects;
}