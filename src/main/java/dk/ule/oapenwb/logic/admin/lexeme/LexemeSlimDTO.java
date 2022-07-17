// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Slim DTO to transfer an overview of a lexeme.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LexemeSlimDTO
{
	private long id;
	private String parserID;
	private long typeID;
	private int langID;
	private String pre;
	private String main;
	private String post;
	private boolean active;
	private int condition;
	private Set<String> tags;
	private long firstSememeID; // ID of the first sememe (first means lowest ID)

	public LexemeSlimDTO(
		@NotNull Lexeme lexeme,
		@NotNull Variant mainVariant,
		@NotNull Sememe firstSememe)
	{
		this.id = lexeme.getId();
		this.parserID = lexeme.getParserID();
		this.typeID = lexeme.getTypeID();
		this.langID = lexeme.getLangID();
		this.pre = mainVariant.getLemma().getPre();
		this.main = mainVariant.getLemma().getMain();
		this.post = mainVariant.getLemma().getPost();
		this.active = lexeme.isActive();
		this.condition = 5;
		this.tags = lexeme.getTags();
		this.firstSememeID = firstSememe.getId();
	}
}
