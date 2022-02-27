// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.syngroup;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Part of the {@link SGSearchResult}
 */
@Data
@AllArgsConstructor
public class SynGroupItem
{
	@JsonView(Views.REST.class)
	private int id;

	@JsonView(Views.REST.class)
	private String description;

	@JsonView(Views.REST.class)
	private String presentation;
}