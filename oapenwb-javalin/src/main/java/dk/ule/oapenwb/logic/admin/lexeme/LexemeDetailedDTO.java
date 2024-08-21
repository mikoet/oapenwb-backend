// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.persistency.entity.content.lexemes.Link;
import dk.ule.oapenwb.persistency.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;

/**
 * DTO to transport a lexeme with all it's attached data from and to frontend.
 */
@Data
@NoArgsConstructor
public class LexemeDetailedDTO
{
	@Valid
	private Lexeme lexeme;
	private List<@Valid Variant> variants;
	private List<@Valid Sememe> sememes;
	private List<@Valid Link> links;
	private List<@Valid Mapping> mappings;

	/**
	 * <p>Helper method turning a LexemeDetailedDTO into a string e.g. for logging of errors during an import.</p>>
	 *
	 * @param dto the dto to log
	 * @return the log string representation of the LexemeDetailedDTO
	 */
	public static String generatedLogStr(LexemeDetailedDTO dto)
	{
		StringBuilder out = new StringBuilder();

		if (dto.getVariants() != null) {
			boolean first = true;
			for (Variant variant : dto.getVariants()) {
				// Maybe add a colon
				if (first) {
					first = false;
				} else {
					out.append(", ");
				}

				// Add content
				Lemma lemma = variant.getLemma();
				if (variant.getLemma() != null && lemma.getMain() != null && !lemma.getMain().isBlank()) {
					out.append(lemma.getMain());
				} else if (variant.getLexemeForms() != null && variant.getLexemeForms().size() > 0 &&
					variant.getLexemeForms().get(0).getText() != null &&
					!variant.getLexemeForms().get(0).getText().isBlank())
				{
					out.append(variant.getLexemeForms().get(0).getText());
				} else {
					out.append("(empty)");
				}
			}
		} else {
			out.append("(empty)");
		}

		return out.toString();
	}
}