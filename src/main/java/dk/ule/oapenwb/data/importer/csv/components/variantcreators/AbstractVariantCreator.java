// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.basis.ApiAction;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import lombok.Getter;

import java.util.List;

/**
 * <p>Abstract class for all concrete variant creators. Each concrete creator is then valid for at least one
 * Part of Speech.</p>
 */
public abstract class AbstractVariantCreator
{
	private static long NEXT_VARIANT_ID = -1L;

	protected final AdminControllers adminControllers;

	@Getter
	protected final String partOfSpeech;

	@Getter
	protected final int columnIndex;

	protected CsvRowBasedImporter.TypeFormPair typeFormsPair;

	/**
	 * <p>Method create for the LexemeProvider environment.</p>
	 *
	 * @param context the importer's context
	 * @param rowData data of the current row
	 * @return a list containing 0..n variants
	 */
	public abstract List<Variant> create(CsvImporterContext context, RowData rowData);

	/**
	 * <p>Method create for the MultiLexemeProvider environment.</p>
	 *
	 * @param context the importer's context
	 * @param rowData data of the current row
	 * @param partText string supplied only by the MultiLexemeProvider for those columns that
	 *   contain multiple lexemes in one column
	 * @return a list containing 0..n variants
	 */
	public abstract List<Variant> create(CsvImporterContext context, RowData rowData, String partText);

	public AbstractVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		int columnIndex)
	{
		this.adminControllers = adminControllers;
		this.partOfSpeech = partOfSpeech;
		this.columnIndex = columnIndex;
	}

	/**
	 * <p>Unfortunately we do not really have the typeFormsPair when creating the creator in a setting. That is why
	 * initialise has to be called to a later time and before the method create(…) will be called(!).</p>
	 *
	 * @param typeFormsPair
	 * @return itself
	 */
	public AbstractVariantCreator initialise(CsvRowBasedImporter.TypeFormPair typeFormsPair)
	{
		if (typeFormsPair == null) {
			throw new RuntimeException("Something went wrong as the type-form pair is null. Maybe the PoS is unknown?");
		}
		this.typeFormsPair = typeFormsPair;

		return this;
	}

	protected Variant createVariant(CsvImporterContext context, List<LexemeForm> lexemeForms,
		int orthographyID)
	{
		// Create the lemma
		Lemma lemma = new Lemma();
		lemma.setFillLemma(Lemma.FILL_LEMMA_AUTOMATICALLY);

		long variantID;
		synchronized (AbstractVariantCreator.class) {
			variantID = NEXT_VARIANT_ID--;
		}

		// Finally create the variant
		Variant variant = new Variant();
		variant.setId(variantID);
		variant.setOrthographyID(orthographyID);
		variant.setLexemeForms(lexemeForms);
		variant.setLemma(lemma);

		variant.setActive(true);
		variant.setApiAction(ApiAction.Insert);
		variant.setChanged(true);

		// Set the variantID on the lexemeForms
		if (lexemeForms != null) {
			for (var lexemeForm : lexemeForms) {
				lexemeForm.setVariantID(variant.getId());
			}
		}

		return variant;
	}

	protected LexemeForm createLexemeForm(int formTypeID, String text)
	{
		LexemeForm form = new LexemeForm();
		form.setState(LexemeForm.STATE_TYPED);
		form.setFormTypeID(formTypeID);
		form.setText(text.trim());

		return form;
	}
}
