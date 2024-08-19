// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators;

import com.google.common.collect.ImmutableSet;
import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.ApiAction;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

	@Getter
	private final int dialectsColumnIndex;

	@Getter
	private final Map<String, Language> dialectMap;

	@Getter
	private final Set<Integer> defaultDialectID;

	@Getter
	private CsvRowBasedImporter.TypeFormPair typeFormsPair;

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
		int columnIndex,
		int dialectsColumnIndex,
		Map<String, Language> dialectMap,
		Set<Integer> defaultDialectID)
	{
		this.adminControllers = adminControllers;
		this.partOfSpeech = partOfSpeech;
		this.columnIndex = columnIndex;
		this.dialectsColumnIndex = dialectsColumnIndex;
		this.dialectMap = dialectMap;
		// !!
		this.defaultDialectID = defaultDialectID == null ? null : ImmutableSet.copyOf(defaultDialectID);
	}

	// only for the MultiVariantController which does not utilize the CreatorUtils
	public AbstractVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		int columnIndex,
		int dialectsColumnIndex)
	{
		this(adminControllers, partOfSpeech, columnIndex, dialectsColumnIndex, null, null);
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

	protected LexemeForm createLexemeForm(CsvImporterContext context, int lineNumber, int formTypeID, String text)
	{
		// TEXT_WARN_LENGTH
		if (text.length() > LexemeForm.TEXT_MAX_LENGTH) {
			throw new RuntimeException(String.format(
				"Lexeme form '%s' exceeds maximum length of %d chars", text, LexemeForm.TEXT_MAX_LENGTH));
		}
		if (text.length() > LexemeForm.TEXT_WARN_LENGTH) {
			context.getMessages().add(String.format("Variant creator '%s'", this.partOfSpeech), MessageType.Warning,
				String.format("Lexeme form '%s' contains more than %d characters", text, LexemeForm.TEXT_WARN_LENGTH),
				lineNumber, this.columnIndex);
		}

		LexemeForm form = new LexemeForm();
		form.setState(LexemeForm.STATE_TYPED);
		form.setFormTypeID(formTypeID);
		form.setText(text.trim());

		return form;
	}
}
