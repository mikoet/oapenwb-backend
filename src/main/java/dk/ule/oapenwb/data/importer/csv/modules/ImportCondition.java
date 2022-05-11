// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.modules;

import dk.ule.oapenwb.data.importer.csv.data.RowData;

/**
 * <p>An ImportCondition can be used optionally do check first if a row shall be imported.</p>
 */
@FunctionalInterface
public interface ImportCondition
{
	boolean meetsCondition(RowData row);
}
