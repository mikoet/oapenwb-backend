// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the data of a row that was read from the file.
 */
@Data
@AllArgsConstructor
public class RowData
{
	private int lineNumber;
	private String[] parts;
}
