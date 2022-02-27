// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.entity.content.lexemes.Link;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
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
}