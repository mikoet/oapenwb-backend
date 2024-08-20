// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.persistency.entity.Views;
import lombok.Data;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Filter options for search of lexemes in the administration interface.
 */
@Data
public class FilterOptions
{
	@JsonView(Views.REST.class)
	private Set<Integer> langIDs = new LinkedHashSet<>();

	@JsonView(Views.REST.class)
	private Set<Integer> typeIDs = new LinkedHashSet<>();

	@JsonView(Views.REST.class)
	private Set<String> tags = new LinkedHashSet<>();

	@JsonView(Views.REST.class)
	private State state = State.Both;
}