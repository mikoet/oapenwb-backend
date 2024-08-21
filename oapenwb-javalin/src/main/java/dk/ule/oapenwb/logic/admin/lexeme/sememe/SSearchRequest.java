// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme.sememe;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.logic.admin.lexeme.TextSearchType;
import dk.ule.oapenwb.persistency.entity.Views;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Sememe Search Request
 */
@Data
public class SSearchRequest
{
	@NotNull
	@JsonView(Views.REST.class)
	private String filter; // search text

	@JsonView(Views.REST.class)
	private Optional<TextSearchType> textSearchType = Optional.of(TextSearchType.Prefixed);

	@NotNull
	@JsonView(Views.REST.class)
	private Integer langID;

	@JsonView(Views.REST.class)
	private Optional<Integer> typeID = Optional.empty();
}