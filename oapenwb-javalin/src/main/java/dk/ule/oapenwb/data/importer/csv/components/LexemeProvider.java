// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>Provides one {@link LexemeDetailedDTO} that will be build by 0..n {@link VariantBuilder}s,
 * or loaded from the database if one of the built variants already exists.</p>
 */
public class LexemeProvider extends AbstractLexemeProvider
{
	public LexemeProvider(AdminControllers adminControllers, String lang, boolean mustProvide)
	{
		super(adminControllers, lang, mustProvide, String.format("Lexeme Provider '%s'", lang));
	}

	public LexemeDetailedDTO provide(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		RowData rowData)
	{
		LexemeDetailedDTO detailedDTO = createDTO(context, typeFormPair,
			buildVariants(context, typeFormPair, rowData), rowData);

		if (detailedDTO.getVariants().size() == 0) {
			// No variants, no lexeme provided.
			return null;
		}

		LexemeDetailedDTO otherDetailedDTO;
		if ((otherDetailedDTO = lookup(context, rowData.getLineNumber(), detailedDTO)) !=  null) {
			// At least one variant already exists
			context.getMessages().add(messageContext, MessageType.Debug,
				"Lexeme from database is being used", rowData.getLineNumber(), -1);
			detailedDTO = otherDetailedDTO;
		}

		return detailedDTO;
	}

	private List<Variant> buildVariants(CsvImporterContext context, CsvRowBasedImporter.TypeFormPair typeFormPair, RowData rowData)
	{
		List<Variant> variantList = new LinkedList<>();
		for (var builder : variantBuilders) {
			// TODO Probably the first variant should be taken here, and be set as the main variant.
			//  Besides this, the LexemeController must make sure that only one variant has mainVariant==true set!
			variantList.addAll(builder.build(context, typeFormPair, rowData));
		}
		return variantList;
	}
}
