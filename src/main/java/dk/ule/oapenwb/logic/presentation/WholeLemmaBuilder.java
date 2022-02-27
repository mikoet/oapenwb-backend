// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.Category;
import dk.ule.oapenwb.entity.content.basedata.Level;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.presentation.options.WholeLemmaOptions;
import dk.ule.oapenwb.util.Pair;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * <p>This class will stick together the single lemmata built by the SingleLemmaBuilder, do some
 * kind of ordering / sorting of the single lemmata, and add additional information such as the
 * catgories and the levels.</p>
 */
public class WholeLemmaBuilder
{
	private final SingleLemmaBuilder singleLemmaBuilder = new SingleLemmaBuilder();

	public String build(final WholeLemmaOptions options, final ControllerSet controllers, final Sememe sememe,
		HashMap<Long, Variant> allVariantsMap) throws CodeException
	{
		final List<Pair<Variant, String>> variantList = new LinkedList<>();

		Set<Long> variantIDs = sememe.getVariantIDs();
		if (variantIDs != null && variantIDs.size() > 0)
		{
			for (Long variantID : variantIDs)
			{
				Variant variant = allVariantsMap.get(variantID);
				if (variant != null)
				{
					variantList.add(new Pair<>(
						variant, singleLemmaBuilder.build(options, controllers, variant, sememe.getDialectIDs())));
				}
			}
		}

		// Sort the variants
		variantList.sort(options.singleLemmaComparator);

		// Return them in string form
		return dataToString(options, controllers, sememe, variantList);
	}

	private String dataToString(final WholeLemmaOptions options, final ControllerSet controllers, final Sememe sememe,
		final List<Pair<Variant, String>> variantList) throws CodeException
	{
		StringBuilder sb = new StringBuilder();

		// Append the SingleLemmas
		boolean first = true;
		for (Pair<Variant, String> pair : variantList)
		{
			if (!first)  {
				sb.append(options.singleLemmaDivider);
				sb.append(' ');
			} else {
				first = false;
			}
			sb.append(pair.getRight());
		}

		// Append the categories - if set and if there are any
		Set<Integer> categoryIDs = sememe.getCategoryIDs();
		if (options.includeCategories && categoryIDs != null && categoryIDs.size() > 0)
		{
			sb.append(' ');
			sb.append("[[");

			CEntityController<Category, Integer> cc = controllers.getCategoriesController();
			first = true;
			for (Integer categoryID : categoryIDs)
			{
				Category c = cc.get(categoryID);
				if (c != null)
				{
					if (!first)  {
						sb.append(", ");
					} else {
						first = false;
					}
					sb.append(c.getUitID_abbr());
				}
			}

			sb.append("]]");
		}

		// Append the unit levels - if set and if there are any
		Set<Integer> levelIDs = sememe.getLevelIDs();
		if (options.includeLevels && levelIDs != null && levelIDs.size() > 0)
		{
			sb.append(' ');
			sb.append("[/");

			CEntityController<Level, Integer> lc = controllers.getUnitLevelsController();
			first = true;
			for (Integer levelID : levelIDs)
			{
				Level l = lc.get(levelID);
				if (l != null)
				{
					if (!first)  {
						sb.append(", ");
					} else {
						first = false;
					}
					sb.append(l.getUitID_abbr());
				}
			}

			sb.append("/]");
		}

		return sb.toString();
	}
}