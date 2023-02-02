// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.statistics;

import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.logic.search.Direction;
import dk.ule.oapenwb.logic.search.DirectionConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Data
@Entity
@Table(name = "SearchRuns")
@NoArgsConstructor
@AllArgsConstructor
public class SearchRun
{
	@Id
	@Basic
	private Instant whenTS;

	@Column(nullable = false, length = 255)
	private String searchText;

	@Column(nullable = false, length = LangPair.ID_LENGTH)
	private String langPair;

	@Column(columnDefinition = "character", length = 1, nullable = false)
	@Convert(converter = DirectionConverter.class)
	private Direction direction;

	/*
	 * resultCount higher than 0 means it was a hit
	 */
	@Column(nullable = false)
	private int resultCount;

	@Column(nullable = false)
	private int millis;

	// Add a JSON field?
}
