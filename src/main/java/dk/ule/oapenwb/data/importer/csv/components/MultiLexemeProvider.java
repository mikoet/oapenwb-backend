// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;

import java.util.LinkedList;
import java.util.List;

public class MultiLexemeProvider extends AbstractLexemeProvider
{
	protected final int columnIndex;

	public MultiLexemeProvider(
		AdminControllers adminControllers,
		String lang,
		boolean mustProvide,
		int columnIndex)
	{
		super(adminControllers, lang, mustProvide, String.format("Multi Lexeme Provider '%s'", lang));
		this.columnIndex = columnIndex;
	}

	public List<LexemeDetailedDTO> provide(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		RowData rowData)
	{
		String columnContent = rowData.getParts()[columnIndex - 1].trim();
		if (columnContent.isEmpty()) {
			return null;
		}

		// Split the column content into parts each representing a possible lexeme
		String[] textParts;
		if (columnContent.contains(",") || columnContent.contains(";")) {
			textParts = columnContent.replace(";", ",").split(",");
		} else {
			textParts = new String[] { columnContent };
		}

		List<LexemeDetailedDTO> result = new LinkedList<>();
		for (int i = 0; i < textParts.length; i++) {
			LexemeDetailedDTO detailedDTO = createDTO(context, typeFormPair,
				buildVariants(context, typeFormPair, rowData, textParts[i]));

			if (detailedDTO.getVariants().size() == 0) {
				// No variants, no lexeme provided.
				continue;
			}

			LexemeDetailedDTO otherDetailedDTO;
			if ((otherDetailedDTO = lookup(context, rowData.getLineNumber(), detailedDTO)) !=  null) {
				// At least one variant already exists
				context.getMessages().add(messageContext, MessageType.Info,
					"Lexeme from database is being used", rowData.getLineNumber(), -1);
				detailedDTO = otherDetailedDTO;
			}

			result.add(detailedDTO);
		}

		if (result.size() == 0) {
			result = null;
		}

		return result;
	}

	private List<Variant> buildVariants(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		RowData rowData,
		String textPart)
	{
		List<Variant> variantList = new LinkedList<>();
		for (var builder : variantBuilders) {
			variantList.addAll(builder.build(context, typeFormPair, rowData, textPart));
		}
		return variantList;
	}
}
