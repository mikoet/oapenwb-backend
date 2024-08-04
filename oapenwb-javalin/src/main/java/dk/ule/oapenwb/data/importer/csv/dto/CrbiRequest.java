// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.Set;

/**
 * <p>Request object for the {@link dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrbiRequest
{
	@Getter
	private String filename;
	@Getter
	private boolean simulate;
	@Getter
	private Set<String> tagNames;
	@Getter
	private Optional<Integer> transactionSkipCount = Optional.empty();
}
