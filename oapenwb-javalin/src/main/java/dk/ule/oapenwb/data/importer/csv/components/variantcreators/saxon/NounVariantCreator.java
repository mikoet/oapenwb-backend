// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.data.importer.csv.CsvImporterContext;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.AbstractVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.CreatorUtils;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.persistency.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class NounVariantCreator extends AbstractVariantCreator
{
	public static final String FT_SINGULAR_NOMINATIVE = "sn"; // singular nominative
	public static final String FT_PLURAL_NOMINATIVE = "pn"; // plural nominative

	private final ImportMode mode;
	private final int orthographyID;

	// FormTypes are the same for every language
	private LexemeFormType ftSn; // singular nominative
	private LexemeFormType ftPn; // plural nominative

	private final Set<String> allowedGenera = new HashSet<>();

	public NounVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		ImportMode mode,
		int orthographyID,
		int columnIndex,
		int dialectsColumnIndex,
		Map<String, Language> dialectMap,
		Set<Integer> defaultDialectID)
	{
		super(adminControllers, partOfSpeech, columnIndex, dialectsColumnIndex, dialectMap, defaultDialectID);

		this.mode = mode;
		this.orthographyID = orthographyID;

		allowedGenera.add("f");
		allowedGenera.add("m");
		allowedGenera.add("n");
	}

	@Override
	public AbstractVariantCreator initialise(CsvRowBasedImporter.TypeFormPair typeFormsPair) {
		super.initialise(typeFormsPair);

		// Trek de formtypen ruut
		this.ftSn = typeFormsPair.getRight().get(FT_SINGULAR_NOMINATIVE);
		this.ftPn = typeFormsPair.getRight().get(FT_PLURAL_NOMINATIVE);

		return this;
	}

	/**
	 * @param context the importer context instance
	 * @param rowData the current rowData instance
	 * @return a list of variants found in the rowData via columnIndex
	 */
	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData)
	{
		List<Variant> result;
		switch (this.mode) {
			case NSS -> result = createNSS(context, rowData);
			case DBO -> result = createDBO(context, rowData);
			default -> throw new RuntimeException(String.format("Unsupported mode '%s'", this.mode));
		}
		return result;
	}

	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData, String partText) {
		throw new RuntimeException("Not implemented!");
	}

	/**
	 * Examples of input for NSS mode:
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
	private List<Variant> createNSS(CsvImporterContext context, RowData rowData)
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
			if (text.endsWith(")")) {
				// If the text contains genera definition only split at the commas left of the first open bracket
				mpHasGeneraDef = true;
				int mpOpenBracket = text.lastIndexOf(" (");
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
				throw new RuntimeException(String.format("Noun '%s' in column %d does not consist of propper 2 parts",
					text, columnIndex));
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
					Pair<String, Set<String>> generaPair = extractGeneraNSS(partTwo);

					String pluNom = CreatorUtils.getVariantForm(generaPair.getLeft(), i);

					Variant variant = createVariant(context, rowData.getLineNumber(), sinNom, pluNom);
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
				Pair<String, Set<String>> generaPair = extractGeneraNSS(partTwo);

				String pluNom = CreatorUtils.getVariantForm(generaPair.getLeft(), 0);

				Variant variant = createVariant(context, rowData.getLineNumber(), sinNom, pluNom);
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
					Pair<String, Set<String>> generaPair = extractGeneraNSS(text);
					String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), i);

					Variant variant = createVariant(context, rowData.getLineNumber(), sinNom);
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
				Pair<String, Set<String>> generaPair = extractGeneraNSS(text);
				String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), 0);

				Variant variant = createVariant(context, rowData.getLineNumber(), sinNom);
				variant.setMainVariant(true);
				variant.getProperties().put("genera", generaPair.getRight());
				result.add(variant);
			}
		}

		CreatorUtils.readAndApplyDialects(result, rowData, getDialectsColumnIndex(), getDialectMap(),
			getDefaultDialectID());

		return result;
	}

	/**
	 * Examples of input for DBO mode:
	 *
	 * Kegelrubb
	 * de Dogg(e)
	 * de(n) kastangappel
	 * dat Binnenland
	 * de Kärmse~Karmse
	 *
	 * Disch, Dischen
	 * Bootshall, -en
	 * de Dänne, -n
	 * dat Gordencenter, -s
	 * de(n) Deef, Deev/Deven    <--- sügt uut nå formaatfeyler? (FIX)
	 *
	 * @param context the importer context instance
	 * @param rowData the current rowData instance
	 * @return a list of variants found in the rowData via columnIndex
	 */
	private List<Variant> createDBO(CsvImporterContext context, RowData rowData)
	{
		String text = rowData.getParts()[this.columnIndex - 1];

		List<Variant> result = new LinkedList<>();
		if (text.isBlank()) {
			return result;
		}

		if (text.contains(",")) {
			// !! Processing of nouns with 2 parts definition + optional genera in one of the two specification forms.
			// !! Nouns in this format may also contain multiple variants separated via ~.

			// FIX Some variants seem to be described via / instead of ~.
			text = text.replace("/", "~");

			// 1) Check for the 2 parts
			String[] parts = text.split(",");
			if (parts.length != 2) {
				throw new RuntimeException(String.format("Noun '%s' in column %d does not consist of propper 2 parts",
					text, columnIndex));
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
					// the first part is special in that it might contain the genus
					String partOne = parts[0];
					Pair<String, Set<String>> generaPair = extractGeneraDBO(partOne);
					String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), i);

					String pluNom = CreatorUtils.getVariantForm(parts[1], i);
					if (pluNom.startsWith("-") && StringUtils.countMatches(pluNom, '-') == 1) {
						pluNom = pluNom.replace("-", sinNom);
					} else if (StringUtils.countMatches(pluNom, '-') > 1) {
						throw new RuntimeException(String.format("Plural definition not understandable: '%s'", pluNom));
					}

					Variant variant = createVariant(context, rowData.getLineNumber(), sinNom, pluNom);
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
				// the first part is special in that it might contain the genus
				String partOne = parts[0];
				Pair<String, Set<String>> generaPair = extractGeneraDBO(partOne);
				String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), 0);

				String pluNom = CreatorUtils.getVariantForm(parts[1], 0);
				if (pluNom.startsWith("-") && StringUtils.countMatches(pluNom, '-') == 1) {
					pluNom = pluNom.replace("-", sinNom);
				} else if (StringUtils.countMatches(pluNom, '-') > 1) {
					throw new RuntimeException(String.format("Plural definition not understandable: '%s'", pluNom));
				}

				Variant variant = createVariant(context, rowData.getLineNumber(), sinNom, pluNom);
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
					// the first part is special in that it might contain the genus
					Pair<String, Set<String>> generaPair = extractGeneraDBO(text);
					String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), i);

					Variant variant = createVariant(context, rowData.getLineNumber(), sinNom);
					if (first) {
						variant.setMainVariant(true);
						first = false;
					}
					variant.getProperties().put("genera", generaPair.getRight());
					result.add(variant);
				}
			} else {
				// 3.a) It only contains one variant
				// the first part is special in that it might contain the genus
				Pair<String, Set<String>> generaPair = extractGeneraDBO(text);
				String sinNom = CreatorUtils.getVariantForm(generaPair.getLeft(), 0);

				Variant variant = createVariant(context, rowData.getLineNumber(), sinNom);
				variant.setMainVariant(true);
				variant.getProperties().put("genera", generaPair.getRight());
				result.add(variant);
			}
		}

		CreatorUtils.readAndApplyDialects(result, rowData, getDialectsColumnIndex(), getDialectMap(),
			getDefaultDialectID());

		return result;
	}

	// String partTwo should already be trimmed for this function to work propperly.
	private Pair<String, Set<String>> extractGeneraNSS(String partTwo)
	{
		Set<String> genera = new HashSet<>();

		if (partTwo.endsWith(" f")) {
			partTwo = partTwo.substring(0, partTwo.length() - 2);
			genera.add("f");
		} else if (partTwo.endsWith(" m")) {
			partTwo = partTwo.substring(0, partTwo.length() - 2);
			genera.add("m");
		} else if (partTwo.endsWith(" n")) {
			partTwo = partTwo.substring(0, partTwo.length() - 2);
			genera.add("n");
		} else if (partTwo.endsWith(")") && partTwo.contains(" (")) {
			int openBracket = partTwo.lastIndexOf(" (");
			int closeBracket = partTwo.length() - 1;

			String generaStr = partTwo.substring(openBracket + 2, closeBracket).replace(" ", "");
			String[] generaArr = generaStr.contains(",") ? generaStr.split(",")
										 : new String[] { generaStr };
			Collections.addAll(genera, generaArr);

			// Remove the genera definition from partTwo
			partTwo = partTwo.substring(0, openBracket).trim();
		}

		for (var genus : genera) {
			if (!allowedGenera.contains(genus)) {
				throw new RuntimeException(String.format("Specified genus '%s' is unknown.", genus));
			}
		}

		return new Pair<>(partTwo, genera);
	}

	private Pair<String, Set<String>> extractGeneraDBO(String partOne)
	{
		Set<String> genera = new HashSet<>();
		partOne = partOne.trim();

		if (partOne.startsWith("de ")) {
			partOne = partOne.substring(3);
			genera.add("f");
		} else if (partOne.startsWith("de(n) ")) {
			partOne = partOne.substring(6);
			genera.add("m");
		} else if (partOne.startsWith("dat ")) {
			partOne = partOne.substring(4);
			genera.add("n");
		}

		for (var genus : genera) {
			if (!allowedGenera.contains(genus)) {
				throw new RuntimeException(String.format("Specified genus '%s' is unknown.", genus));
			}
		}

		return new Pair<>(partOne, genera);
	}

	private Variant createVariant(CsvImporterContext context, int lineNumber, String sinNom, String pluNom)
	{
		// Create the LexemeForms
		LexemeForm lfSinNom = createLexemeForm(context, lineNumber, ftSn.getId(), sinNom);
		LexemeForm lfPluNom = createLexemeForm(context, lineNumber, ftPn.getId(), pluNom);

		List<LexemeForm> lexemeForms = lfPluNom == null ? List.of(lfSinNom)
			: List.of(lfSinNom, lfPluNom);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}

	private Variant createVariant(CsvImporterContext context, int lineNumber, String sinNom)
	{
		// Create the LexemeForm
		LexemeForm lfSinNom = createLexemeForm(context, lineNumber, ftSn.getId(), sinNom);

		List<LexemeForm> lexemeForms = List.of(lfSinNom);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}
}
