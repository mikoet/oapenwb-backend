// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme.sememe;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeSlimDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;

/**
 * Vorwydert de klas LexemeSlimDTO üm en list van sememen.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LexemeSlimPlus extends LexemeSlimDTO
{
	@JsonView(Views.REST.class)
	private List<@Valid Sememe> sememes;

	public LexemeSlimPlus(long id, String parserID, long typeID, int langID, String pre, String main, String post,
		boolean active, int condition, Set<String> tags, Long firstSememeID)
	{
		setId(id);
		setParserID(parserID);
		setTypeID(typeID);
		setLangID(langID);
		setPre(pre);
		setMain(main);
		setPost(post);
		setActive(active);
		setCondition(condition);
		setTags(tags);
		setFirstSememeID(firstSememeID);
	}
}
