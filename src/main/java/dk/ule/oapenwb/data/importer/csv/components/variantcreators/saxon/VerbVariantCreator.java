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
import dk.ule.oapenwb.util.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

// TODO By de spleaden med meyr as eyn lekseem in en spleade mut eyn dat wul en beaten
//   anders maken. Dår bruukt de MultiLexemeProvider eygens neyn VerbVariantCreator,
//   oder de MultiLexemeProvider spalt dee enkelten leksemen al up un geavet se wyder.
//   Man ouk dän bruukt dat wul eygens neyne separaten Creators vöär Verb, Noun, unsouwyder.

/**
 * <p>The VerbVariantCreator creates verb variants (i.e. PoS being VERB) and was primarily put into place for
 * for Low Saxon, but can basically be used for any languages when the variants are specified by the same format
 * (see method create() in this class).</p>
 *
 * Oapene punkten:
 * TODO 102 De spleade med de dativ-saken mut importeerd warden
 */
public class VerbVariantCreator extends AbstractVariantCreator
{
	public static final String FT_INFINITIVE = LexemeFormType.FT_VERB_INFINITIVE;
	public static final String FT_INFINITIVE_DIV = "inf_div";
	public static final String FT_FORM1 = "s3ps"; // singular 3rd person present
	public static final String FT_FORM2 = "s3pt"; // singular 3rd person preterite
	public static final String FT_FORM3 = "ptc2"; // participle II

	private static final String AUX_HEVVEN = "hevven_v";
	private static final String AUX_WEASEN = "weasen_v";

	private final int orthographyID;

	// FormTypes are the same for every language
	private LexemeFormType ftInf;
	private LexemeFormType ftInfDiv;
	private LexemeFormType ftForm1; // 1st person singular, present time
	private LexemeFormType ftForm2; // 1st person singular, past time
	private LexemeFormType ftForm3; // particip perfect / II

	// Maps each auxiliary verb to a parserID
	private final Map<String, String> allowedAuxiliaries = new HashMap<>();

	public VerbVariantCreator(
		AdminControllers adminControllers,
		String partOfSpeech,
		int orthographyID,
		int columnIndex,
		int dialectsColumnIndex,
		Map<String, Language> dialectMap,
		Set<Integer> defaultDialectID)
	{
		super(adminControllers, partOfSpeech, columnIndex, dialectsColumnIndex, dialectMap, defaultDialectID);

		this.orthographyID = orthographyID;

		allowedAuxiliaries.put("hevven", AUX_HEVVEN);
		allowedAuxiliaries.put("hebben", AUX_HEVVEN);
		allowedAuxiliaries.put("weasen", AUX_WEASEN);
	}

	@Override
	public AbstractVariantCreator initialise(CsvRowBasedImporter.TypeFormPair typeFormsPair) {
		super.initialise(typeFormsPair);

		// Trek de formtypen ruut
		this.ftInf = typeFormsPair.getRight().get(FT_INFINITIVE);
		this.ftInfDiv = typeFormsPair.getRight().get(FT_INFINITIVE_DIV);
		this.ftForm1 = typeFormsPair.getRight().get(FT_FORM1);
		this.ftForm2 = typeFormsPair.getRight().get(FT_FORM2);
		this.ftForm3 = typeFormsPair.getRight().get(FT_FORM3);

		return this;
	}

	/**
	 * Examples of input:
	 *
	 * acht|geaven, givt acht, gaev acht, het achtgeaven
	 * af|helpen ~ af|hölpen, helpt af ~ hölpt af, holp af, het afholpen
	 * an wat liggen, ligt an wat, laeg an wat, het an wat leagen
	 *
	 * krupen, krüpt ~ krupt, kroup, kroapen (hevven)
	 * loupen, löpt, leyp, loupen (hevven, weasen)
	 *
	 * nikkoppen~nikköppen
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

		// !! To see if a text is multi-part a possible auxiliary verb definition has to be excluded because both
		// !! can contain commas (`,`)
		boolean isMultiPart = false;
		boolean mpHasAuxiliaryDef = false;
		int mpNumberOfParts = 0;
		if (text.contains(",")) {
			if (text.endsWith(")")) {
				// If the text contains auxiliary verb definition only split at the commas left of the first open bracket
				mpHasAuxiliaryDef = true;
				int mpOpenBracket = text.lastIndexOf(" (");
				mpNumberOfParts = StringUtils.countMatches(text.substring(0, mpOpenBracket), ',') + 1;
				isMultiPart = mpNumberOfParts > 1;
			} else {
				isMultiPart = true;
			}
		}

		if (isMultiPart) {
			// !! Processing of verbs with 4 parts definition + optional auxiliary verb(s) in one of the two specification forms.
			// !! Verbs in this format may also contain multiple variants separated via ~.

			// 1) Check for the 4 parts
			String[] parts;
			if (mpHasAuxiliaryDef) {
				// If the text contains auxiliary verb definition only split at the commas left of the first open bracket
				parts = text.split(",", mpNumberOfParts);
			} else {
				parts = text.split(",");
			}
			if (parts.length != 4) {
				throw new RuntimeException(String.format("Verb '%s' in column %d does not consist of propper 4 parts",
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
					String infinitiveDiv = CreatorUtils.getVariantForm(parts[0], i);
					String form1 = CreatorUtils.getVariantForm(parts[1], i);
					String form2 = CreatorUtils.getVariantForm(parts[2], i);

					// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
					//  Anders kun eyn de evtl. öäver den kontekst torüg geaven.
					// the participle II is special in that it might contain one or more auxiliary verbs
					String partFour = parts[3].trim();
					Pair<String, Set<String>> auxiliaryPair = extractAuxiliaries(partFour);

					String form3 = CreatorUtils.getVariantForm(auxiliaryPair.getLeft(), i);

					Variant variant = createVariant(context, rowData.getLineNumber(), infinitiveDiv, form1, form2, form3);
					if (first) {
						variant.setMainVariant(true);
						first = false;
					}
					// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
					variant.getProperties().put("auxiliaries", auxiliaryPair.getRight());
					result.add(variant);
				}
			} else {
				// 4.a) It only contains one variant
				// Create it.
				String infinitiveDiv = CreatorUtils.getVariantForm(parts[0], 0);
				String form1 = CreatorUtils.getVariantForm(parts[1], 0);
				String form2 = CreatorUtils.getVariantForm(parts[2], 0);

				// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
				//  Anders kun eyn de evtl. öäver den kontekst torüg geaven.
				// the participle II is special in that it might contain one or more auxiliary verbs
				String partFour = parts[3].trim();
				Pair<String, Set<String>> auxiliaryPair = extractAuxiliaries(partFour);
				partFour = auxiliaryPair.getLeft();

				String form3 = CreatorUtils.getVariantForm(partFour, 0);

				Variant variant = createVariant(context, rowData.getLineNumber(), infinitiveDiv, form1, form2, form3);
				variant.setMainVariant(true);
				// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
				variant.getProperties().put("auxiliaries", auxiliaryPair.getRight());
				result.add(variant);
			}
		} else {
			// !! Processing of verbs with 1 part definition only + optional auxiliary verb(s).
			// !! Verbs in this format may also contain multiple variants separated via ~.

			// 1) Check if it has variants (char ~ will be part of the text)
			boolean hasMultipleVariants = text.contains("~");
			if (hasMultipleVariants) {
				// 2.a) It has multiple variants.
				// Find the number of variants within the text (find the largest number of variants within one part)
				int numberOfVariants = StringUtils.countMatches(text, "~") + 1;

				// 2.b) Create the variants
				boolean first = true;
				for (int i = 0; i < numberOfVariants; i++) {
					// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
					//  Anders kun eyn de evtl. öäver den kontekst torüg geaven.
					// the participle II is special in that it might contain one or more auxiliary verbs
					Pair<String, Set<String>> auxiliaryPair = extractAuxiliaries(text);

					String infinitiveDiv = CreatorUtils.getVariantForm(auxiliaryPair.getLeft(), i);
					Variant variant = createVariant(context, rowData.getLineNumber(), infinitiveDiv);
					if (first) {
						variant.setMainVariant(true);
						first = false;
					}
					// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
					variant.getProperties().put("auxiliaries", auxiliaryPair.getRight());
					result.add(variant);
				}
			} else {
				// 3.a) It only contains one variant
				// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
				//  Anders kun eyn de evtl. öäver den kontekst torüg geaven.
				// The participle II is special in that it might contain one or more auxiliary verbs
				Pair<String, Set<String>> auxiliaryPair = extractAuxiliaries(text);

				// Create the variant
				String infinitiveDiv = CreatorUtils.getVariantForm(auxiliaryPair.getLeft(), 0);
				Variant variant = createVariant(context, rowData.getLineNumber(), infinitiveDiv);
				variant.setMainVariant(true);
				// TODO 101 Sint de auxiliaries nu eygens deyl van'e variante oder van't semeem?
				variant.getProperties().put("auxiliaries", auxiliaryPair.getRight());
				result.add(variant);
			}
		}

		CreatorUtils.readAndApplyDialects(result, rowData, getDialectsColumnIndex(), getDialectMap(),
			getDefaultDialectID());

		return result;
	}

	@Override
	public List<Variant> create(CsvImporterContext context, RowData rowData, String partText) {
		throw new RuntimeException("Not implemented!");
	}

	/**
	 * @param partFour string containing the lexeme form with optionally multiple variants and optional specification
	 *   of auxiliary verbs
	 * @return a pair containg the partFour string reduced by the auxiliary specification in the left and a set of
	 *   auxiliary verbs in the right
	 */
	private Pair<String, Set<String>> extractAuxiliaries(String partFour)
	{
		Set<String> auxiliaries = new HashSet<>();

		if (partFour.startsWith("het ")) {
			partFour = partFour.substring(4);
			auxiliaries.add("hevven");
		} else if (partFour.startsWith("is ")) {
			partFour = partFour.substring(3);
			auxiliaries.add("weasen");
		} else if (partFour.endsWith(")") && partFour.contains(" (")) {
			int openBracket = partFour.lastIndexOf(" (");
			int closeBracket = partFour.length() - 1;

			String auxiliaryStr = partFour.substring(openBracket + 2, closeBracket).replace(" ", "");
			String[] auxiliariesArr = auxiliaryStr.contains(",") ? auxiliaryStr.split(",")
										 : new String[] { auxiliaryStr };
			Collections.addAll(auxiliaries, auxiliariesArr);

			// Remove the auxiliary verb definition from partFour
			partFour = partFour.substring(0, openBracket).trim();
		}

		Set<String> parserIDs = new HashSet<>();
		for (var auxiliary : auxiliaries) {
			if (!allowedAuxiliaries.containsKey(auxiliary)) {
				throw new RuntimeException(String.format("Specified auxiliary verb '%s' is unknown.", auxiliary));
			}
			parserIDs.add(allowedAuxiliaries.get(auxiliary));
		}

		if (parserIDs.size() == 0) {
			// Add auxiliary verb 'hevven' as default
			parserIDs.add(AUX_HEVVEN);
		}

		return new Pair<>(partFour, parserIDs);
	}

	private Variant createVariant(CsvImporterContext context, int lineNumber, String infinitiveDivText, String form1Text,
		String form2Text, String form3Text)
	{
		// Create the LexemeForms
		LexemeForm infinitive = createLexemeForm(context, lineNumber, ftInf.getId(),
			infinitiveDivText.replace("|", ""));
		LexemeForm infinitiveDiv = infinitiveDivText.contains("|") ? createLexemeForm(context, lineNumber,
			ftInfDiv.getId(),
			infinitiveDivText) : null;
		LexemeForm form1 = createLexemeForm(context, lineNumber, ftForm1.getId(), form1Text);
		LexemeForm form2 = createLexemeForm(context, lineNumber, ftForm2.getId(), form2Text);
		LexemeForm form3 = createLexemeForm(context, lineNumber, ftForm3.getId(), form3Text);

		List<LexemeForm> lexemeForms = infinitiveDiv == null ? List.of(infinitive, form1, form2, form3)
			: List.of(infinitive, infinitiveDiv, form1, form2, form3);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}

	private Variant createVariant(CsvImporterContext context, int lineNumber, String infinitiveDivText)
	{
		// Create the LexemeForm
		LexemeForm infinitive = createLexemeForm(context, lineNumber, ftInf.getId(),
			infinitiveDivText.replace("|", ""));
		LexemeForm infinitiveDiv = infinitiveDivText.contains("|")
			? createLexemeForm(context, lineNumber, ftInfDiv.getId(), infinitiveDivText)
			: null;

		List<LexemeForm> lexemeForms = infinitiveDiv == null ? List.of(infinitive)
										   : List.of(infinitive, infinitiveDiv);

		// Create the variant
		return createVariant(context, lexemeForms, this.orthographyID);
	}
}
