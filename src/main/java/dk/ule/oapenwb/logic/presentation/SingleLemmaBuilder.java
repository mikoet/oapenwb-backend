// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.Orthography;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.presentation.options.SingleLemmaOptions;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The purpose of this class is to build a single lemma, i.e. one for one variant.
 * Such a lemma will include the dialects it is valid for. In order to do that this
 * builder takes the dialectIDs of the variant but reduces them by the dialectIDs
 * that are not part of the sememe's dialectIDs.
 */
public class SingleLemmaBuilder
{
	public String build(final SingleLemmaOptions options, final ControllerSet controllers, final Variant variant,
		final Set<Integer> sememeDialects) throws CodeException
	{
		if (options.activeDataOnly && !variant.isActive())
		{
			return "";
		}

		StringBuilder sb = new StringBuilder();

		/**
		 * Examples of SingleLemmas:
		 * eaten^[o:nss] ((l:nds-nw))
		 * etten^[o:nss] ((l:nds-wf))
		 * eten^[o:db] ((l:nds-nw))
		 */

		Lemma lemma = variant.getLemma();
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

		// Add the orthography if set
		if (options.includeOrthography)
		{
			Orthography o = controllers.getOrthographiesController().get(variant.getOrthographyID());
			if (o != null) {
				sb.append("^[");
				sb.append(o.getAbbreviation()); // TODO This could be switched to the ID instead of the abbreviation - could make sense if the client already knows all abbrebiations
				sb.append("]");
			}
		}

		// Add the dialects if set and available on the sememe
		if (options.includeDialects)
		{
			Set<Integer> d = variant.getDialectIDs();
			// Are there any dialects set on the variant?
			if (d != null && d.size() > 0 && sememeDialects != null && sememeDialects.size() > 0)
			{
				// Filter out the dialects so that only those remain that are both, part of the variant and the sememe
				List<Integer> dialectIDs = d.stream().filter(id -> sememeDialects.contains(id)).collect(Collectors.toList());
				// Are there dialects left that were set on the sememe?
				if (dialectIDs.size() > 0)
				{
					// Now add the dialects
					sb.append(" ((");
					boolean first = true;
					CEntityController<Language, Integer> langController = controllers.getLanguagesController();
					for (Integer id : d)
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
}