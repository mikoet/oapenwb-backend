// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.sememecreators;

import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;

import java.util.Set;

public interface ISememeCreator
{
	/**
	 * <p>Method to create the default sememe of a lexeme.</p>
	 *
	 * @param context the importer's context
	 * @param rowData data of the current row
	 * @param variantIDs IDs of all variants this sememe is created for
	 * @param dialectIDs all dialectIDs used in the variants this sememe is created for
	 * @return the created sememe
	 */
	Sememe create(CsvImporterContext context, RowData rowData, Set<Long> variantIDs, Set<Integer> dialectIDs);
}
