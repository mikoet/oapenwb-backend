// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation.options;

import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.util.Pair;

import java.util.Comparator;

/**
 * Options for a whole presentation (of a SynGroup?).
 */
public class PresentationOptions extends WholeLemmaOptions
{
	public static final Comparator<Pair<Sememe, String>> ALPHABETIC_WHOLE_LEMMA_COMPARATOR =
		(t1, t2) -> t1.getRight().compareTo(t2.getRight());

	public static final String DEFAULT_WHOLE_LEMMA_DIVIDER = ";";
	public static final PresentationOptions DEFAULT_PRESENTATION_OPTIONS = new PresentationOptions(
		false, true, true, true, true, ALPHABETIC_SINGLE_LEMMA_COMPARATOR, DEFAULT_SINGLE_LEMMA_DIVIDER,
		ALPHABETIC_WHOLE_LEMMA_COMPARATOR, DEFAULT_WHOLE_LEMMA_DIVIDER
	);

	// Each SingleLemma represents a Variant, and thus this comparator determines the
	// order of the SingleLemmas within a WholeLemma.
	// <Sememe, List of Variants, String representation of WholeLemma>
	public final Comparator<Pair<Sememe, String>> wholeLemmaComparator;
	public final String wholeLemmaDivider;


	public PresentationOptions(boolean activeDataOnly, boolean includeOrthographies, boolean includeDialects,
		boolean includeCategories, boolean includeLevels, Comparator singleLemmaComparator, String singleLemmaDivider,
		Comparator<Pair<Sememe, String>> wholeLemmaComparator, String wholeLemmaDivider)
	{
		super(activeDataOnly, includeOrthographies, includeDialects, includeCategories, includeLevels,
			singleLemmaComparator, singleLemmaDivider);
		this.wholeLemmaComparator = wholeLemmaComparator;
		this.wholeLemmaDivider = wholeLemmaDivider;
	}
}