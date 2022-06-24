// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.multi;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class MultiVariantCreator extends AbstractVariantCreator
{
	public static final String WARNING_PARANTHESES = "parantheses used";
	public static final String WARNING_MULTI_WORD = "multi-word";

	private final int orthographyID;

	// FormTypes are the same for every language
	private LexemeFormType ftFirst; // form type depending on the LexemeType

	private final OperationMode mode;
	private final boolean multiWord;

	public MultiVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		int orthographyID,
		int columnIndex,
		OperationMode mode,
		boolean multiWord)
	{
		super(adminControllers, partOfSpeech, columnIndex);

		this.orthographyID = orthographyID;
		this.mode = mode;
		this.multiWord = multiWord;
	}

	public MultiVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		int orthographyID,
		int columnIndex)
	{
		this(adminControllers, partOfSpeech, orthographyID, columnIndex, OperationMode.Default, false);
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

	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData)
	{
		throw new RuntimeException("Not implemented!");
	}

	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData, String partText)
	{
		List<Variant> result = new LinkedList<>();
		if (partText.isBlank()) {
			return result;
		}

		switch (mode) {
			case Danish -> {
				if (partText.startsWith("at ")) {
					partText = partText.substring(3);
				}
			}
			case English -> {
				if (partText.startsWith("to ")) {
					partText = partText.substring(3);
				}
			}
			case Swedish -> {
				if (partText.startsWith("att ")) {
					partText = partText.substring(4);
				}
			}
		}

		// Create the variant with the partText
		Variant variant = createVariant(context, partText.trim());
		variant.setMainVariant(true);
		result.add(variant);

		// Do some checks and create warning messages if necessary
		if (StringUtils.containsAny(partText, '(', ')')) {
			// Parts with parantheses are not optimal
			context.getMessages().add(CsvRowBasedImporter.CONTEXT_BUILD_STRUCTURES, MessageType.Error,
				"Content contains parantheses which should ideally be avoided",
				rowData.getLineNumber(), columnIndex);

			@SuppressWarnings("unchecked cast")
			Set<String> warnings = (Set<String>) variant.getProperties().computeIfAbsent(
				"warnings", k -> new HashSet<String>());
			warnings.add(WARNING_PARANTHESES);
		}
		if (!multiWord && partText.contains(" ")) {
			// Looks like an UTDR
			context.getMessages().add(CsvRowBasedImporter.CONTEXT_BUILD_STRUCTURES, MessageType.Error,
				"Content contains spaces which suggests it is an UTDR",
				rowData.getLineNumber(), columnIndex);

			@SuppressWarnings("unchecked cast")
			Set<String> warnings = (Set<String>) variant.getProperties().computeIfAbsent(
				"warnings", k -> new HashSet<String>());
			warnings.add(WARNING_MULTI_WORD);
		}


		return result;
	}

	private Variant createVariant(CsvImporterContext context, String sinNom)
	{
		// Create the LexemeForm
		LexemeForm lfSinNom = createLexemeForm(ftFirst.getId(), sinNom);

		List<LexemeForm> lexemeForms = List.of(lfSinNom);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}
}
