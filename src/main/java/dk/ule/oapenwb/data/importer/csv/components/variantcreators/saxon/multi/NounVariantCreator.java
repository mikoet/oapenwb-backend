// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.multi;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.CreatorUtils;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class NounVariantCreator extends AbstractVariantCreator
{
	public static final String FT_SINGULAR_NOMINATIVE = "sn"; // singular nominative
	public static final String FT_PLURAL_NOMINATIVE = "pn"; // plural nominative

	private final int orthographyID;

	// FormTypes are the same for every language
	private final LexemeFormType ftSn; // singular nominative
	private final LexemeFormType ftPn; // plural nominative

	private final int dialectsColumnIndex;

	private final Set<String> allowedGenera = new HashSet<>();

	public NounVariantCreator(
		AdminControllers adminControllers,
		CsvRowBasedImporter.TypeFormPair typeFormsPair,
		int orthographyID,
		int columnIndex,
		int dialectsColumnIndex)
	{
		super(adminControllers, typeFormsPair, columnIndex);

		this.orthographyID = orthographyID;

		// Trek de formtypen ruut
		this.ftSn = typeFormsPair.getRight().get(FT_SINGULAR_NOMINATIVE);
		this.ftPn = typeFormsPair.getRight().get(FT_PLURAL_NOMINATIVE);

		this.dialectsColumnIndex = dialectsColumnIndex;

		allowedGenera.add("f");
		allowedGenera.add("m");
		allowedGenera.add("n");
	}

	/**
	 * Examples of input:
	 *
	 * deepde, deepden f
	 * deert, deerten ~ deerter
	 * deev, deve ~ deven m
	 * dingsdag
	 * geagendeyl ~ teagendeyl, geagendeyl ~ teagendeyl n
	 * hauptanklaagde (f, m)
	 * minsk ~ minske, minsken (m, n)
	 * byspil (n)
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

		// !! To see if a text is multi-part a possible genera definition has to be excluded because both
		// !! can contain commas (`,`)
		boolean isMultiPart = false;
		boolean mpHasGeneraDef = false;
		int mpNumberOfParts = 0;
		if (text.contains(",")) {
			if (text.contains("(")) {
				// If the text contains genera definition only split at the commas left of the first open bracket
				mpHasGeneraDef = true;
				int mpOpenBracket = text.indexOf("(");
				mpNumberOfParts = StringUtils.countMatches(text.substring(0, mpOpenBracket), ',') + 1;
				isMultiPart = mpNumberOfParts > 1;
			} else {
				isMultiPart = true;
			}
		}

		if (isMultiPart) {
			// !! Processing of nouns with 2 parts definition + optional genera in one of the two specification forms.
			// !! Nouns in this format may also contain multiple variants separated via ~.

			// 1) Check for the 2 parts
			String[] parts;
			if (mpHasGeneraDef) {
				// If the text contains genera definition only split at the commas left of the first open bracket
				parts = text.split(",", mpNumberOfParts);
			} else {
				parts = text.split(",");
			}
			if (parts.length != 2) {
				throw new RuntimeException("Noun '" + text + "' in text " + rowData.getLineNumber() + " does not consist of 2 parts");
			}

			// 2) Check if it has variants (char ~ will be part of the text)
			boolean hasMultipleVariants = text.contains("~");
			if (hasMultipleVariants) {
				// 3.a) It has multiple variants.
				// Find the number of variants within the text (find the largest number of variants within one part)
				int numberOfVariants = 1;
				for (String part : parts) {
					if (part.contains("~")) {
						int variantsOfPart = StringUtils.countMatches(part, "~") + 1;
						if (variantsOfPart > numberOfVariants) {
							numberOfVariants = variantsOfPart;
						}
					}
				}

				// 3.b) Create the variants
				boolean first = true;
				for (int i = 0; i < numberOfVariants; i++) {
					String sinNom = CreatorUtils.getVariantForm(parts[0], i);

					// the second part is special in that it might contain one or more genera
					String partTwo = parts[1].trim();
					Pair<String, Set<String>> generaPair = extractGenera(partTwo);

					String pluNom = CreatorUtils.getVariantForm(generaPair.getLeft(), i);

					Variant variant = createVariant(context, sinNom, pluNom);
					if (first) {
						variant.setMainVariant(true);
						first = false;
					}
					variant.getProperties().put("genera", generaPair.getRight());
					result.add(variant);
				}
			} else {
				// 4.a) It only contains one variant
				// Create it.
				String sinNom = CreatorUtils.getVariantForm(parts[0], 0);

				// the second part is special in that it might contain one or more genera
				String partTwo = parts[1].trim();
				Pair<String, Set<String>> generaPair = extractGenera(partTwo);

				String pluNom = CreatorUtils.getVariantForm(generaPair.getLeft(), 0);

				Variant variant = createVariant(context, sinNom, pluNom);
				variant.setMainVariant(true);
				variant.getProperties().put("genera", generaPair.getRight());
				result.add(variant);
			}
		} else {
			// !! Processing of nouns with 1 part definition only + optional genera.
			// !! Nouns in this format may also contain multiple variants separated via ~.

			// 1) Check if it has variants (char ~ will be part of the text)
			boolean hasMultipleVariants = text.contains("~");
			if (hasMultipleVariants) {
				// 2.a) It has multiple variants.
				// Find the number of variants within the text (find the largest number of variants within one part)
				int numberOfVariants = StringUtils.countMatches(text, "~") + 1;

				// 2.b) Create the variants
				boolean first = true;
				for (int i = 0; i < numberOfVariants; i++) {
					// the second part is special in that it might contain one or more genera
					Pair<String, Set<String>> generaPair = extractGenera(text);
					String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), i);

					Variant variant = createVariant(context, sinNom);
					if (first) {
						variant.setMainVariant(true);
						first = false;
					}
					variant.getProperties().put("genera", generaPair.getRight());
					result.add(variant);
				}
			} else {
				// 3.a) It only contains one variant
				// the second part is special in that it might contain one or more genera
				Pair<String, Set<String>> generaPair = extractGenera(text);
				String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), 0);

				Variant variant = createVariant(context, sinNom);
				variant.setMainVariant(true);
				variant.getProperties().put("genera", generaPair.getRight());
				result.add(variant);
			}
		}

		CreatorUtils.readAndApplyDialects(result, rowData, this.dialectsColumnIndex);

		return result;
	}

	private Pair<String, Set<String>> extractGenera(String partTwo)
	{
		Set<String> genera = new HashSet<>();
		partTwo = partTwo.trim();

		if (partTwo.endsWith(" f")) {
			partTwo = partTwo.substring(0, partTwo.length() - 2);
			genera.add("f");
		} else if (partTwo.endsWith(" m")) {
			partTwo = partTwo.substring(0, partTwo.length() - 2);
			genera.add("m");
		} else if (partTwo.endsWith(" n")) {
			partTwo = partTwo.substring(0, partTwo.length() - 2);
			genera.add("n");
		} else if (partTwo.contains("(")) {
			int openBracket = partTwo.indexOf("(");
			int closeBracket = partTwo.indexOf(")");

			if (openBracket == -1 || closeBracket == -1) {
				throw new RuntimeException("Wrong specification of genera: brackets don't match.");
			}

			String generaStr = partTwo.substring(openBracket + 1, closeBracket).replace(" ", "");
			String[] generaArr = generaStr.contains(",") ? generaStr.split(",")
										 : new String[] { generaStr };
			Collections.addAll(genera, generaArr);

			// Remove the genera definition from partTwo
			partTwo = partTwo.substring(0, openBracket - 1).trim();
		}

		for (var genus : genera) {
			if (!allowedGenera.contains(genus)) {
				throw new RuntimeException(String.format("Specified genus '%s' is unknown.", genus));
			}
		}

		return new Pair<>(partTwo, genera);
	}

	private Variant createVariant(CsvImporterContext context, String sinNom, String pluNom)
	{
		// Create the LexemeForms
		LexemeForm lfSinNom = createLexemeForm(ftSn.getId(), sinNom);
		LexemeForm lfPluNom = createLexemeForm(ftPn.getId(), pluNom);

		List<LexemeForm> lexemeForms = lfPluNom == null ? List.of(lfSinNom)
										   : List.of(lfSinNom, lfPluNom);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}

	private Variant createVariant(CsvImporterContext context, String sinNom)
	{
		// Create the LexemeForm
		LexemeForm lfSinNom = createLexemeForm(ftSn.getId(), sinNom);

		List<LexemeForm> lexemeForms = List.of(lfSinNom);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}
}
