// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation.options;

import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.util.Pair;

import java.util.Comparator;

/**
 * <p>Options for a whole lemma, extending those of a single lemma. A whole lemma consists of one to many
 * single lemmas as optional categories and levels.</p>
 */
public class WholeLemmaOptions extends SingleLemmaOptions
{
	public static final Comparator<Pair<Variant, String>> ALPHABETIC_SINGLE_LEMMA_COMPARATOR =
		Comparator.comparing(Pair::getRight);

	public static final String DEFAULT_SINGLE_LEMMA_DIVIDER = ",";

	public final boolean includeCategories;
	public final boolean includeLevels;
	// Each SingleLemma represents a Variant, and thus this comparator determines the
	// order of the SingleLemmas within a WholeLemma.
	public final Comparator<Pair<Variant, String>> singleLemmaComparator;
	public final String singleLemmaDivider;

	public WholeLemmaOptions(boolean activeDataOnly, boolean includeOrthographies, boolean includeDialects,
		boolean includeCategories, boolean includeLevels, Comparator<Pair<Variant, String>> singleLemmaComparator,
		String singleLemmaDivider)
	{
		super(activeDataOnly, includeOrthographies, includeDialects);
		this.includeCategories = includeCategories;
		this.includeLevels = includeLevels;
		this.singleLemmaComparator = singleLemmaComparator;
		this.singleLemmaDivider = singleLemmaDivider;
	}
}