// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.sheet;

import lombok.Data;

@Data
public class SheetResult
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