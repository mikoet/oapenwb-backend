// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.Orthography;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.LanguagesController;
import dk.ule.oapenwb.logic.presentation.options.SingleLemmaOptions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>The purpose of this class is to build a 'single lemma', i.e. one for a variant that is
 * assigned to a sememe (i.e. could also be named VariantLemmaBuilder).</p>
 * <p>Such a lemma will include the dialects it is valid for. In order to do that this builder takes
 * the dialectIDs of the variant but reduces them by the dialectIDs that are not part of the sememe's
 * dialectIDs.</p>
 */
public class SingleLemmaBuilder
{
	/**
	 * <p>Builds a lemma for a single variant that is assigned to a sememe (both being part of the same lexeme, of
	 * course).</p>
	 *
	 * <p>Examples of SingleLemmas:
	 * <ul>
	 *   <li>eaten^[o:nss] ((l:nds-nw))</li>
	 *   <li>etten^[o:nss] ((l:nds-nw, l:nds-wf))</li>
	 *   <li>eten^[o:db] ((l:nds-nw))</li>
	 * </ul>
	 * </p>
	 *
	 * @param options The options object (use
	 *     {@link dk.ule.oapenwb.logic.presentation.options.PresentationOptions}.DEFAULT_PRESENTATION_OPTIONS for defauls)
	 * @param controllers Set of controllers necessary for building the lemmata
	 * @param variant The variant of a sememe for which the lemma is to be built
	 * @param sememeDialects Set of IDs of dialects that are used in the sememe
	 * @return the built lemma string
	 * @throws CodeException Can be thrown by controllers of the IControllerSet controllers
	 */
	public String build(final SingleLemmaOptions options, final IControllerSet controllers, final Variant variant,
		final Set<Integer> sememeDialects) throws CodeException
	{
		if (options.activeDataOnly && !variant.isActive())
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();
		sb.append(this.buildLemmaWithOrthographyOnly(options, controllers, variant));

		// Add the dialects if set and available on the sememe
		if (options.includeDialects)
		{
			Set<Integer> variantDialectIDs = variant.getDialectIDs();
			// Are there any dialects set on the variant?
			if (variantDialectIDs != null && variantDialectIDs.size() > 0 && sememeDialects != null && sememeDialects.size() > 0)
			{
				// Filter out the dialects so that only those remain that are both, part of the variant and the sememe
				List<Integer> dialectIDs = variantDialectIDs.stream().filter(sememeDialects::contains).collect(Collectors.toList());
				// Are there dialects left that were set on the sememe?
				if (dialectIDs.size() > 0)
				{
					// Sort the IDs (i.e. the dialects will for now only be sorted via their ID)
					Collections.sort(dialectIDs);

					// Now add the dialects
					sb.append(" ((");
					boolean first = true;
					LanguagesController langController = controllers.getLanguagesController();
					for (Integer id : dialectIDs)
					{
						// Only take those dialectIDs into account that are also set on the sememe!
						if (sememeDialects.contains(id))
						{
							if (!first) {
								sb.append(", ");
							} else {
								first = false;
							}
							Language l = langController.get(id);
							sb.append(l.getUitID_abbr());
						}
					}
					sb.append("))");
				}
			}
		}

		return sb.toString();
	}

	public String buildLemmaWithOrthographyOnly(final SingleLemmaOptions options, final IControllerSet controllers,
		final Variant variant) throws CodeException
	{
		if (options.activeDataOnly && !variant.isActive())
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();

		/*
		 * Examples of SingleLemmas:
		 * eaten^[o:nss] ((l:nds-nw))
		 * etten^[o:nss] ((l:nds-wf))
		 * eten^[o:db] ((l:nds-nw))
		 */

		Lemma lemma = variant.getLemma();

		sb.append('{');
		// Add the pre text is there is one
		String pre = lemma.getPre();
		if (pre != null && !pre.isEmpty())
		{
			sb.append(pre);
			sb.append(' ');
		}

		// Add the main text
		sb.append(lemma.getMain());

		// Add the post text if there is one
		String post = lemma.getPost();
		if (post != null && !post.isEmpty())
		{
			sb.append(' ');
			sb.append(post);
		}

		sb.append('}');

		// Add the orthography if set
		if (options.includeOrthography)
		{
			Orthography o = controllers.getOrthographiesController().get(variant.getOrthographyID());
			if (o != null) {
				sb.append("^[");
				sb.append(o.getAbbreviation()); // TODO This could be switched to the ID instead of the abbreviation
				//   - could make sense if the client already knows all abbrebiations
				sb.append("]");
			}
		}

		return sb.toString();
	}
}