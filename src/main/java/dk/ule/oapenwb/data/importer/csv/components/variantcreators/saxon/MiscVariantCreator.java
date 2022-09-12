// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.CreatorUtils;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MiscVariantCreator extends AbstractVariantCreator
{
	private final int orthographyID;

	// FormTypes are the same for every language
	private LexemeFormType ftFirst; // form type depending on the LexemeType

	public MiscVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		int orthographyID,
		int columnIndex,
		int dialectsColumnIndex,
		Map<String, Language> dialectMap,
		Set<Integer> defaultDialectIDs)
	{
		super(adminControllers, partOfSpeech, columnIndex, dialectsColumnIndex, dialectMap, defaultDialectIDs);
		this.orthographyID = orthographyID;
	}

	@Override
	public AbstractVariantCreator initialise(CsvRowBasedImporter.TypeFormPair typeFormsPair) {
		super.initialise(typeFormsPair);

		// Trek den eyrsten formtypen ruut
		Optional<LexemeFormType> optFormType = typeFormsPair.getRight().values().stream().findFirst();
		if (optFormType.isPresent()) {
			this.ftFirst = optFormType.get();
		} else {
			throw new RuntimeException(String.format(
				"First (i.e. default) form type could not be found for PoS '%s'.", partOfSpeech));
		}

		return this;
	}

	/**
	 * Examples of input:
	 *
	 * ysig            ADJ
	 * öäver           ADP
	 * öävermorgen     ADV
	 * as              CCONJ
	 * bold ~ bolde    ADV
	 * dat givt        UTDR
	 *
	 * @param context the importer context instance
	 * @param rowData the current rowData instance
	 * @return a list of variants found in the rowData via columnIndex
	 */
	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData)
	{
		String text = rowData.getParts()[this.columnIndex - 1];

		List<Variant> result = new LinkedList<>();
		if (text.isBlank()) {
			return result;
		}

		// !! Processing of misc PoS with 1 part definition.
		// !! Text may also contain multiple variants separated via ~.

		// 1) Check if it has variants (char ~ will be part of the text)
		boolean hasMultipleVariants = text.contains("~");
		if (hasMultipleVariants) {
			// 2.a) It has multiple variants.
			// Find the number of variants within the text (find the largest number of variants within one part)
			int numberOfVariants = StringUtils.countMatches(text, "~") + 1;

			// 2.b) Create the variants
			boolean first = true;
			for (int i = 0; i < numberOfVariants; i++) {
				String content = CreatorUtils.getVariantForm(text, i);

				Variant variant = createVariant(context, rowData.getLineNumber(), content);
				if (first) {
					variant.setMainVariant(true);
					first = false;
				}
				result.add(variant);
			}
		} else {
			// 3.a) It only contains one variant
			String content = CreatorUtils.getVariantForm(text, 0);

			Variant variant = createVariant(context, rowData.getLineNumber(), content);
			variant.setMainVariant(true);
			result.add(variant);
		}

		CreatorUtils.readAndApplyDialects(result, rowData, getDialectsColumnIndex(), getDialectMap(),
			getDefaultDialectID());

		return result;
	}

	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData, String partText) {
		throw new RuntimeException("Not implemented!");
	}

	private Variant createVariant(CsvImporterContext context, int lineNumber, String sinNom)
	{
		// Create the LexemeForm
		LexemeForm lfSinNom = createLexemeForm(context, lineNumber, ftFirst.getId(), sinNom);

		List<LexemeForm> lexemeForms = List.of(lfSinNom);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}
}
