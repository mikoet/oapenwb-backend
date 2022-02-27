// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Lexeme Search Request from the administration interface.
 */
@Data
public class SearchRequest
{
	@NotNull
	@JsonView(Views.REST.class)
	private String filter;

	@JsonView(Views.REST.class)
	private Optional<TextSearchType> textSearchType = Optional.of(TextSearchType.PostgreWeb);

	@JsonView(Views.REST.class)
	private Integer offset;

	@JsonView(Views.REST.class)
	private Integer limit;

	@JsonView(Views.REST.class)
	private FilterOptions options;
}