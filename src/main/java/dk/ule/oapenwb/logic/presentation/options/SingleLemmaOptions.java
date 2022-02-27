// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation.options;

import lombok.AllArgsConstructor;

/**
 * <p>Options that are used only for building a single lemma.</p>
 *
 * TODO What's a single lemma? A SingleLemma probably is the lemma built for only one sememe or one variant.
 *   Debug and dodcument well.
 */
@AllArgsConstructor
public class SingleLemmaOptions
{
	public final boolean activeDataOnly;
	public final boolean includeOrthography;
	public final boolean includeDialects;
}