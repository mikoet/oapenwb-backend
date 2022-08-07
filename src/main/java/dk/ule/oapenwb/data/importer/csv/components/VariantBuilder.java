// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components;

import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>The VariantBuilder is an abstraction layer above the actual {@link AbstractVariantCreator} instances:
 * since the construction of a variant is PoS specific it does contain a map that maps PoS tags to concrete
 * implementations of the AbstractVariantCreator. It also has a otherCaseCreator that is just for all PoS
 * cases that are not included in the map.</p>
 */
public class VariantBuilder
{
	@Getter
	private final Map<String, AbstractVariantCreator> posToCreator = new HashMap<>();
	//private AbstractVariantCreator otherCaseCreator;

	/**
	 * <p>This method has to be called before the first call of method build() in order to initialise the creators
	 * with their necessary type information.</p>
	 *
	 * @param typeFormMap the TypeFormMap containing the LexemeType definitions for each LexemeType (PoS)
	 */
	public void initialise(CsvRowBasedImporter.TypeFormMap typeFormMap)
	{
		for (var creator : posToCreator.values()) {
			creator.initialise(typeFormMap.get(creator.getPartOfSpeech()));
		}
	}

	/**
	 * <p>Calls one of its {@link AbstractVariantCreator} instances to build 0..n variants
	 * and returns them in a list.</p>
	 * <p>This method is the right one for the MultiLexemeProvider since it utilises
	 * the partText parameter that is necessary to build the variants there.</p>
	 *
	 * @param context the importer's context
	 * @param typeFormPair the instance containing the vital type information fitting the current
	 *   Part of Speech
	 * @param rowData data of the current row
	 * @param partText string supplied only by the MultiLexemeProvider for those columns that
	 *   contain multiple lexemes in one column, and null is supllied in context of LexemeProvider
	 * @return a list containing 0..n variants
	 */
	public List<Variant> build(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		RowData rowData,
		String partText)
	{
		final String pos = typeFormPair.getLeft().getName();
		final AbstractVariantCreator variantCreator = posToCreator.get(pos);
		if (variantCreator == null) {
			throw new RuntimeException(String.format("No variant creator configured for PoS '%s'", pos));
		}
		if (partText == null) {
			return variantCreator.create(context, rowData);
		}
		return variantCreator.create(context, rowData, partText);
	}

	/**
	 * <p>Calls one of its {@link AbstractVariantCreator} instances to build 0..n variants
	 * and returns them in a list.</p>
	 * <p>This method is the right one for the LexemeProvider since it doesn't utilise
	 * the partText parameter.</p>
	 *
	 * @param context the importer's context
	 * @param typeFormPair the instance containing the vital type information fitting the current
	 *   Part of Speech
	 * @param rowData data of the current row
	 * @return a list containing 0..n variants
	 */
	public List<Variant> build(
		CsvImporterContext context,
		CsvRowBasedImporter.TypeFormPair typeFormPair,
		RowData rowData)
	{
		return build(context, typeFormPair, rowData, null);
	}
}
