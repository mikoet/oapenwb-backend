// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.syngroup;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.LexemeSlimPlus;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * SynGroup Search Result
 */
@Data
public class SGSearchResult
{
	@JsonView(Views.REST.class)
	private List<@Valid SynGroupItem> synGroups;

	@JsonView(Views.REST.class)
	private List<@Valid LexemeSlimPlus> lexemes;
}