// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.sememecreators.saxon;

import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.components.sememecreators.DefaultSememeCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.csv.setting.SaxonFirstImportSetting;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;

import java.util.Set;

public class SaxonSememeCreator extends DefaultSememeCreator
{
	@Override
	public Sememe create(CsvImporterContext context, RowData rowData, Set<Long> variantIDs, Set<Integer> dialectIDs) {
		Sememe sememe = super.create(context, rowData, variantIDs, dialectIDs);

		String text = rowData.getParts()[SaxonFirstImportSetting.COL_VASTE_VORBINDING - 1];
		if (text != null && !text.isBlank()) {
			// token PTAPIE
			sememe.getProperties().put("vaste-vorbinding", text);
		}

		return sememe;
	}
}
