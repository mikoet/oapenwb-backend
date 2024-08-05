// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity.statistics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class CondensedData
{
	/*
	 * An ID contains the year and month and a "-" in between.
	 * Example: 2020-07
	 */
	@Id
	@Column(length = 7)
	private String id;

	private CondenseType conType;

	@Column(nullable = false)
	private int numberOfSearches;

	@Column(nullable = false)
	private int numberOfUsers;

	/*
	 * The top ten searches will be a string made up of 10 rows with
	 * each row containing something like:
	 * [search string]:[search count]
	 */
	@Column(nullable = false)
	private String topTenSearches;

	// TODO Add: millisMin, millisMax, millisAvg, millisMedian
}