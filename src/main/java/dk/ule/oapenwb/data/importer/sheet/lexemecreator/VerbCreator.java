// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.sheet.lexemecreator;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.sheet.SheetFileImporter;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.Orthography;
import dk.ule.oapenwb.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class VerbCreator extends AbstractCreator
{
	public static final String FT_INFINITIVE = LexemeFormType.FT_VERB_INFINITIVE;
	public static final String FT_FORM1 = "form1"; // TODO
	public static final String FT_FORM2 = "form2"; // TODO
	public static final String FT_FORM3 = "form3"; // TODO

	// FormTypes are the same for every language
	private final LexemeFormType ftInf;
	private final LexemeFormType ftForm1; // 1st person singular, present time
	private final LexemeFormType ftForm2; // 1st person singular, past time
	private final LexemeFormType ftForm3; // partizip perfect / II

	public VerbCreator(AdminControllers adminControllers, SheetFileImporter.TypeFormPair typeFormsPair)
	{
		super(adminControllers, typeFormsPair);

		// Trek de formtypen ruut
		this.ftInf = typeFormsPair.getRight().get(FT_INFINITIVE);
		this.ftForm1 = typeFormsPair.getRight().get(FT_FORM1);
		this.ftForm2 = typeFormsPair.getRight().get(FT_FORM2);
		this.ftForm3 = typeFormsPair.getRight().get(FT_FORM3);
	}

	public LexemeDetailedDTO createDTO_Saxon(String newSpelling, String germanBased, CreationConfig config)
		throws CodeException
	{
		List<Variant> variants = new LinkedList<>();
		variants.addAll(createVerbVariants_NSS(config, newSpelling));
		variants.addAll(createVerbVariants_SaxonGermanBased(config, germanBased));

		return createDTO(config, variants);
	}

	/**
	 * Examples of input:
	 *
	 * acht|geaven, givt acht, gaev acht, het achtgeaven
	 * af|helpen ~ af|hölpen, helpt af ~hölpt af, holp af, het afholpen
	 * an wat liggen, ligt an wat, laeg an wat, het an wat leagen
	 *
	 * open:
	 * nikkoppen~nikköppen
	 *
	 * @param line
	 * @param config
	 */
	private List<Variant> createVerbVariants_NSS(CreationConfig config, String line)
	{
		List<Variant> result = new LinkedList<>();
		if (line.isBlank()) {
			return result;
		}

		// 1) Check for the 4 parts
		String[] parts = line.split(",");
		if (parts.length != 4) {
			throw new RuntimeException(
				"Verb '" + line + "' in line " + config.getLineNo() + " does not consist of 4 parts");
		}

		// 2) Check if it has variants (char ~ will be part of the line)
		boolean hasMultipleVariants = line.contains("~");
		if (hasMultipleVariants) {
			// 3.a) It has multiple variants.
			// Find the number of variants within the line
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
				String infinitive = getVariantForm(parts[0], i);
				String form1 = getVariantForm(parts[1], i);
				String form2 = getVariantForm(parts[2], i);
				String form3 = getVariantForm(parts[3], i);

				Variant variant = createLexemeFormsAndVariant(config, infinitive, form1, form2, form3);
				if (first) {
					variant.setMainVariant(true);
					first = false;
				}
				result.add(variant);
			}
		} else {
			// 4.a) It only contains one variant
			// Create it.
			String infinitive = getVariantForm(parts[0], 0);
			String form1 = getVariantForm(parts[1], 0);
			String form2 = getVariantForm(parts[2], 0);
			String form3 = getVariantForm(parts[3], 0);

			Variant variant = createLexemeFormsAndVariant(config, infinitive, form1, form2, form3);
			result.add(variant);
		}
		return result;
	}

	private String getVariantForm(String part, int index)
	{
		if (part.contains("~")) {
			return part.split("~")[index].trim();
		}
		return part.trim();
	}

	/**
	 * Examples of input:
	 *
	 * bedrägen
	 * bekritteln
	 * bläken
	 *
	 * @param config
	 * @param line
	 * @return
	 * @throws CodeException
	 */
	private List<Variant> createVerbVariants_SaxonGermanBased(CreationConfig config, String line) throws CodeException {
		List<Variant> result = new LinkedList<>();
		if (line.isBlank()) {
			return result;
		}

		LexemeForm infinitive = new LexemeForm();
		infinitive.setState(LexemeForm.STATE_TYPED);
		infinitive.setFormTypeID(ftInf.getId());
		infinitive.setText(line.trim());

		Variant variant = createLexemeFormsAndVariant(config, List.of(infinitive),
			getOrthographyIdByAbbr(Orthography.ABBR_SAXON_GERMAN_BASED));
		result.add(variant);

		return result;
	}

	private int getOrthographyIdByAbbr(String abbr) throws CodeException {
		for (Orthography ortho : adminControllers.getOrthographiesController().list()) {
			if (ortho.getAbbreviation().equals(abbr)) {
				return ortho.getId();
			}
		}
		throw new RuntimeException(String.format("Orthography with abbreviation '%s' does not exist", abbr));
	}

	private Variant createLexemeFormsAndVariant(CreationConfig config, String infinitiveText, String form1Text,
		String form2Text,  String form3Text)
	{
		// Create the LexemeForms
		LexemeForm infinitive = createLexemeForm(ftInf.getId(), infinitiveText);
		LexemeForm form1 = createLexemeForm(ftForm1.getId(), form1Text);
		LexemeForm form2 = createLexemeForm(ftForm2.getId(), form2Text);
		LexemeForm form3 = createLexemeForm(ftForm3.getId(), form3Text);

		// Create the variant
		return createLexemeFormsAndVariant(config, List.of(infinitive, form1, form2, form3), config.getLanguage().getMainOrthographyID());
	}

	private Variant createLexemeFormsAndVariant(CreationConfig config, String infinitiveText, int orthographyID)
	{
		// Create the LexemeForm
		LexemeForm infinitive = createLexemeForm(ftInf.getId(), infinitiveText);

		// Create the variant
		return createLexemeFormsAndVariant(config, List.of(infinitive), orthographyID);
	}
}