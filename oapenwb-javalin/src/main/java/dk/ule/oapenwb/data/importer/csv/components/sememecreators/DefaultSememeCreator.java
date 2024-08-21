// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.sememecreators;

import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;

import java.util.Set;

public class DefaultSememeCreator implements ISememeCreator
{
	@Override
	public Sememe create(CsvImporterContext context, RowData rowData, Set<Long> variantIDs, Set<Integer> dialectIDs) {
		Sememe sememe = new Sememe();
		sememe.setId(-1L);
		sememe.setInternalName("$default");
		sememe.setVariantIDs(variantIDs);
		sememe.setFillSpec(Sememe.FILL_SPEC_NONE);
		sememe.setDialectIDs(dialectIDs);

		sememe.setActive(true);
		sememe.setApiAction(ApiAction.Insert);
		sememe.setChanged(true);

		return sememe;
	}
}
