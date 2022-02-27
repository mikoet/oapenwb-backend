// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.statistics;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "SearchRuns")
@NoArgsConstructor
public class SearchRun
{
	@Id
	@Basic
	private Instant whenTS;

	@Column(nullable = false)
	private String searchText;

	/*
	 * Was the search a hit? That means, did it have a result?
	 */
	@Column(nullable = false)
	private boolean hit; // TODO numberOfResults?

	// TODO Add the measured time
	// private int millis;
}