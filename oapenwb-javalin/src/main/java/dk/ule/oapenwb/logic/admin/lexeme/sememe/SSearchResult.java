// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme.sememe;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.entity.Views;
import lombok.Data;

import javax.validation.Valid;
import java.util.List;

/**
 * Sememe Search Result
 */
@Data
public class SSearchResult
{
	@JsonView(Views.REST.class)
	private List<@Valid LexemeSlimPlus> lexemes;
}