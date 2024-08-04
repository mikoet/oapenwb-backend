// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.Category;
import dk.ule.oapenwb.entity.content.basedata.Level;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.generic.ICEntityController;
import dk.ule.oapenwb.logic.presentation.options.WholeLemmaOptions;
import dk.ule.oapenwb.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * <p>This class will stick together the single lemmata built by the SingleLemmaBuilder, do some
 * kind of ordering / sorting of the single lemmata, and add additional information such as the
 * catgories and the levels. It could also be called SememeLemmaBuilder as it builds the lemma
 * for a sememe with all its assigned variants.</p>
 * <p>Examples of WholeLemmata:
 * <ul>
 *   <li>eaten^NSS, etten^NSS</li>
 *   <li>eaten, etten [[c:flora_a, c:fauna_a]] [/sl:colloq_a, sl:exalted_a/]</li>
 * </ul>
 * </p>
 */
public class WholeLemmaBuilder
{
	private static final Logger LOG = LoggerFactory.getLogger(WholeLemmaBuilder.class);

	private final SingleLemmaBuilder singleLemmaBuilder = new SingleLemmaBuilder();

	private Variant merge(Variant v1, Variant v2) throws CloneNotSupportedException {
		Variant result = (Variant) v1.clone();
		if (result.getDialectIDs() != null && v2.getDialectIDs() != null) {
			result.getDialectIDs().addAll(v2.getDialectIDs());
		} else if (result.getDialectIDs() == null && v2.getDialectIDs() != null) {
			result.setDialectIDs(new HashSet<>());
			result.getDialectIDs().addAll(v2.getDialectIDs());
		}
		return result;
	}

	public String build(final WholeLemmaOptions options, final IControllerSet controllers, final Sememe sememe,
		Map<Long, Variant> allVariantsMap) throws CodeException
	{
		final List<Pair<Variant, String>> variantList = new LinkedList<>();
		final LinkedHashMap<String, Variant> variantMap = new LinkedHashMap<>(
			sememe.getVariantIDs() != null ? sememe.getVariantIDs().size() : 0);

		try {
			Set<Long> variantIDs = sememe.getVariantIDs();
			if (variantIDs != null && variantIDs.size() > 0) {
				// First loop: Merge variants that have the same lemma
				for (Long variantID : variantIDs) {
					Variant variant = allVariantsMap.get(variantID);
					if (variant != null && ((options.activeDataOnly && variant.isActive()) ^ !options.activeDataOnly)) {
						final String lemma = singleLemmaBuilder.buildLemmaWithOrthographyOnly(options, controllers, variant);
						if (variantMap.containsKey(lemma)) {
							variantMap.put(lemma, merge(variantMap.get(lemma), variant));
						} else {
							variantMap.put(lemma, variant);
						}
					}
				}
				// Second loop: Build the single lemmata for the remaining variants
				for (Map.Entry<String, Variant> entry : variantMap.entrySet()) {
					Variant variant = entry.getValue();
					variantList.add(new Pair<>(
						variant, singleLemmaBuilder.build(options, controllers, variant, sememe.getDialectIDs())));
				}
			}
		} catch (CloneNotSupportedException e) {
			LOG.error("Cloning a variant in merge() failed", e);
			throw new CodeException(ErrorCode.Other_CloningFailed,
				Arrays.asList(new Pair<>("type", "Variant"), new Pair<>("scope", "whole-lemma-building")));
		}

		if (variantList.size() > 0) {
			// Sort the variants
			variantList.sort(options.singleLemmaComparator);

			// Return them in string form
			return dataToString(options, controllers, sememe, variantList);
		} else {
			return "";
		}
	}

	private String dataToString(final WholeLemmaOptions options, final IControllerSet controllers, final Sememe sememe,
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

			// Sort the IDs (i.e. the catetegories will for now only be sorted via their ID)
			List<Integer> categoryIDsSorted = new ArrayList<>(categoryIDs);
			Collections.sort(categoryIDsSorted);

			ICEntityController<Category, Integer> cc = controllers.getCategoriesController();
			first = true;
			for (Integer categoryID : categoryIDsSorted)
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

			// Sort the IDs (i.e. the levels will for now only be sorted via their ID)
			List<Integer> levelIDsSorted = new ArrayList<>(levelIDs);
			Collections.sort(levelIDsSorted);

			ICEntityController<Level, Integer> lc = controllers.getUnitLevelsController();
			first = true;
			for (Integer levelID : levelIDsSorted)
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