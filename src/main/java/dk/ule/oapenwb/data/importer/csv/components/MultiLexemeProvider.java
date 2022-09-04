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
import java.util.ListIterator;

public class MultiLexemeProvider extends AbstractLexemeProvider
{
	protected final int columnIndex;

	static String[] splitColumnContent(String columnContent) {
		int openBraces = 0;
		List<Integer> fracturePositions = new LinkedList<>();
		for (int i = 0; i < columnContent.length(); i++) {
			char c = columnContent.charAt(i);
			if (c == '(') {
				openBraces++;
			} else if (c == ')') {
				openBraces--;
			} else if ((c == ',' || c == ';') && openBraces == 0) {
				fracturePositions.add(i);
			}
		}

		if (fracturePositions.size() == 0) {
			return new String[] { columnContent };
		} else {
			String[] textParts = new String[fracturePositions.size() + 1];

			int index = 0, startPosition = 0;
			ListIterator<Integer> iterator = fracturePositions.listIterator();
			while (iterator.hasNext()) {
				int fracturePosition = iterator.next();
				textParts[index] = columnContent.substring(startPosition, fracturePosition).trim();
				startPosition = fracturePosition + 1;
				index++;
			}
			// Also set the remaining last part
			textParts[index] = columnContent.substring(startPosition).trim();
			return textParts;
		}
	}

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
		String columnContent = rowData.getParts()[columnIndex - 1];
		if (columnContent.isEmpty()) {
			return null;
		}

		// Split the column content into parts each representing a possible lexeme
		String[] textParts = splitColumnContent(columnContent);

		List<LexemeDetailedDTO> result = new LinkedList<>();
		for (String textPart : textParts) {
			LexemeDetailedDTO detailedDTO = createDTO(context, typeFormPair,
				buildVariants(context, typeFormPair, rowData, textPart));

			if (detailedDTO.getVariants().size() == 0) {
				// No variants, no lexeme provided.
				continue;
			}

			LexemeDetailedDTO otherDetailedDTO;
			if ((otherDetailedDTO = lookup(context, rowData.getLineNumber(), detailedDTO)) != null) {
				// At least one variant already exists
				context.getMessages().add(messageContext, MessageType.Debug,
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
