// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.syngroup;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.logic.admin.lexeme.TextSearchType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * SynGroup Search Request
 */
@Data
public class SGSearchRequest
{
	@NotNull
	@JsonView(Views.REST.class)
	private String filter;

	@JsonView(Views.REST.class)
	private Optional<TextSearchType> textSearchType = Optional.of(TextSearchType.Prefixed);

	@NotNull
	@JsonView(Views.REST.class)
	private Integer langID;

	@JsonView(Views.REST.class)
	private Optional<Integer> typeID = Optional.empty();

	/*
	@JsonView(Views.REST.class)
	private Integer offset;

	@JsonView(Views.REST.class)
	private Integer limit;
	 */
}