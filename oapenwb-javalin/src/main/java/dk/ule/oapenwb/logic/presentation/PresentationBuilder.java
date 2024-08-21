// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.persistency.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.presentation.options.PresentationOptions;
import dk.ule.oapenwb.util.Pair;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * <p>The purpose of this class is to build the presentation of a {@link SynGroup}.</p>
 *
 * TODO Possible optimization: use a ThreadLocal store for these builders.
 */
public class PresentationBuilder
{
	private final WholeLemmaBuilder wholeLemmaBuilder = new WholeLemmaBuilder();

	public String build(final PresentationOptions options, final IControllerSet controllers,
		final List<Sememe> sememes, final List<Variant> allVariants) throws CodeException
	{
		LinkedList<Pair<Sememe, String>> sememePairsList = sememeListToPairList(sememes);
		HashMap<Long, Variant> allVariantsMap = variantListToHashMap(allVariants);

		for (Pair<Sememe, String> wholeLemma : sememePairsList)
		{
			Sememe sememe = wholeLemma.getLeft();
			if (sememe.getVariantIDs() != null && sememe.getVariantIDs().size() > 0)
			{
				wholeLemma.setRight(wholeLemmaBuilder.build(options, controllers, sememe, allVariantsMap));
			}
		}

		// Sorteren van de sememesMap (wholeLemmasMap)
		sememePairsList.sort(options.wholeLemmaComparator);

		return sememePairsListToString(options, sememePairsList);
	}

	private String sememePairsListToString(final PresentationOptions options, LinkedList<Pair<Sememe, String>> list)
	{
		StringBuilder sb = new StringBuilder();

		// Append the WholeLemmas
		boolean first = true;
		for (Pair<Sememe, String> pair : list)
		{
			if (!first)  {
				sb.append(options.wholeLemmaDivider);
				sb.append(' ');
			} else {
				first = false;
			}
			sb.append(pair.getRight());
		}

		return sb.toString();
	}

	public static LinkedList<Pair<Sememe, String>> sememeListToPairList(final List<Sememe> entities)
	{
		LinkedList<Pair<Sememe, String>> result = new LinkedList<>();
		for (Sememe entity : entities) {
			result.add(new Pair<>(entity, ""));
		}
		return result;
	}

	public static HashMap<Long, Variant> variantListToHashMap(final List<Variant> entities)
	{
		HashMap<Long, Variant> result = new LinkedHashMap<>();
		for (Variant entity : entities) {
			result.put(entity.getId(), entity);
		}
		return result;
	}
}
