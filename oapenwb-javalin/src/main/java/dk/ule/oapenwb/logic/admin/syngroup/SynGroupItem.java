// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.syngroup;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Part of the {@link SGSearchResult}
 */
@Getter
@RequiredArgsConstructor
public class SynGroupItem
{
	@JsonView(Views.REST.class)
	private final int id;

	@JsonView(Views.REST.class)
	private final String description;

	@JsonView(Views.REST.class)
	private final String presentation;
}