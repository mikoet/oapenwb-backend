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

	// Since RowData instances are only compared within one import run
	// comparison via the lineNumber is sufficient.
	@Override
	public boolean equals(Object other) {
		if (other == null) {
			return false;
		}
		if (other instanceof RowData) {
			return this.lineNumber == ((RowData) other).getLineNumber();
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.lineNumber;
	}
}
