// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.dto;

import lombok.Data;

/**
 * Contains the result of an import done by an {@link dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter} instance.
 */
@Data
public class CrbiResult
{
	private long readCount = 0;
	private long saveCount = 0;
	private long skipCount = 0;
	private boolean successful = false;

	public void incReadCount()
	{
		this.readCount++;
	}

	public void incSaveCount()
	{
		this.saveCount++;
	}

	public void incSkipCount()
	{
		this.skipCount++;
	}
}
