// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.data;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>Represents the data of a row that was read from the file.</p>
 * <p>Instances of these data objects are created and filled in the first step.</p>
 */
@Data
@AllArgsConstructor
public class RowData
{
	private int lineNumber;
	private String[] parts;
}
