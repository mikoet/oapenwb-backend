// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.setting;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.CsvImporterConfig;
import dk.ule.oapenwb.data.importer.csv.components.LexemeProvider;
import dk.ule.oapenwb.data.importer.csv.components.VariantBuilder;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.ImportMode;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.MiscVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.NounVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.VerbVariantCreator;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;

import java.util.Set;

/**
 * <p>Class to set up the CsvImporterConfig instance for the first import of the (Low) Saxon dictionary.</p>
 */
public class SaxonFirstImportSetting
{
	public static final int COL_NICH_IMPORTEREN = 1;
	public static final int COL_NDS_DBO = 2;
	public static final int COL_NDS_DBO_DIALECTS = 3;
	public static final int COL_NDS_NSS = 4;
	public static final int COL_NDS_NSS_DIALECTS = 5;
	public static final int COL_NDS_NL_NSS = 6;
	public static final int COL_NDS_NL_NSS_DIALECTS = 7;
	public static final int COL_M_S_MULTIPLE_SEMEMES = 8;
	public static final int COL_VASTE_VORBINDING = 9;
	public static final int COL_PART_OF_SPEECH = 10;
	public static final int COL_SCIENTIFIC_NAME = 11;
	public static final int COL_STYLE = 12;
	public static final int COL_FINISH = 13;
	public static final int COL_SWEDISH = 14;
	public static final int COL_DANISH = 15;
	public static final int COL_GERMAN = 16;
	public static final int COL_ENGLISH = 17;
	public static final int COL_DUTCH = 18;
	public static final int COL_NDS_DBO_ORIGIN = 19;

	public static final int COL_MIN_COUNT = COL_FINISH;
	public static final int COL_LAST_INDEX = COL_NDS_DBO_ORIGIN;

	private final AdminControllers adminControllers;

	public SaxonFirstImportSetting(AdminControllers adminControllers)
	{
		this.adminControllers = adminControllers;
	}

	/**
	 * <p>Sets up a CsvImporterConfig instance and returns it. However, the following properties will not be set
	 * on the config instance and must be set afterwards and before running the import:
	 * <ul>
	 *     <li>Filename</li>
	 * </ul>
	 * </p>
	 *
	 * @return A CsvImporterConfig instance with the lexeme providers etc. all set up, but missing the filename
	 * @throws CodeException Can be thrown when there is a problem retrieving the ID of an orthography
	 */
	public CsvImporterConfig getConfig() throws CodeException
	{
		CsvImporterConfig cfg = new CsvImporterConfig();

		cfg.setColumnCount(COL_LAST_INDEX);
		cfg.setMinColumnCount(COL_MIN_COUNT);
		cfg.setTagNames(Set.of("imported", "loup1"));
		cfg.setSkipRows(Set.of(1));
		cfg.setPosColIndex(COL_PART_OF_SPEECH);
		cfg.getAllowedPos().addAll(Set.of(LexemeType.TYPE_ADJ, LexemeType.TYPE_ADP, LexemeType.TYPE_ADV,
			LexemeType.TYPE_AUX, LexemeType.TYPE_CCONJ, LexemeType.TYPE_DET,  LexemeType.TYPE_INTJ,
			LexemeType.TYPE_NOUN , LexemeType.TYPE_NUM, LexemeType.TYPE_PART, LexemeType.TYPE_PRON,
			LexemeType.TYPE_PROPN, /*LexemeType.TYPE_PUNCT, */ LexemeType.TYPE_SCONJ, LexemeType.TYPE_VERB /*,
			LexemeType.TYPE_X*/, LexemeType.TYPE_C_UTDR));
		cfg.setImportCondition(row -> {
			String firstPart = row.getParts()[COL_NICH_IMPORTEREN - 1];
			return firstPart == null || firstPart.isEmpty();
		});

		// !! Set up LexemeProviders
		cfg.getLexemeProviders().put("nds", buildLexemeProviderNds());

		return cfg;
	}

	private LexemeProvider buildLexemeProviderNds() throws CodeException
	{
		// !! Setup VariantBuilder
		// Northern Low Saxon in NSS
		VariantBuilder variantBuilderNndsNss = setupVariantBuilder(
			ImportMode.NSS,
			getOrthographyID("NSS"),
			COL_NDS_NSS,
			COL_NDS_NSS_DIALECTS);

		// Westphalian Dutch Low Saxon in NSS
		VariantBuilder variantBuilderNdsNlNss = setupVariantBuilder(
			ImportMode.NSS,
			getOrthographyID("NSS"),
			COL_NDS_NL_NSS,
			COL_NDS_NL_NSS_DIALECTS);

		// Northern Low Saxon in DBO
		// TODO The origin should be imported as well
		VariantBuilder variantBuilderNndsDbo = setupVariantBuilder(
			ImportMode.DBO,
			getOrthographyID("DBO"),
			COL_NDS_DBO,
			COL_NDS_DBO_DIALECTS);

		LexemeProvider lexemeProvider = new LexemeProvider(adminControllers, "nds", true);
		lexemeProvider.getVariantBuilders().add(variantBuilderNndsNss);
		lexemeProvider.getVariantBuilders().add(variantBuilderNdsNlNss);
		lexemeProvider.getVariantBuilders().add(variantBuilderNndsDbo);

		return lexemeProvider;
	}

	private VariantBuilder setupVariantBuilder(ImportMode mode, int orthographyID, int columnIndex, int dialectColumnIndex)
	{
		VariantBuilder variantBuilderNndsNss = new VariantBuilder();

		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_ADJ, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_ADP, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_ADV, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_AUX, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_CCONJ, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_DET, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_INTJ, orthographyID, columnIndex, dialectColumnIndex);

		variantBuilderNndsNss.getPosToCreator().put(LexemeType.TYPE_NOUN, new NounVariantCreator(adminControllers,
			LexemeType.TYPE_NOUN, mode, orthographyID, columnIndex, dialectColumnIndex));

		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_NUM, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PART, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PRON, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PROPN, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PUNCT, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_SCONJ, orthographyID, columnIndex, dialectColumnIndex);

		variantBuilderNndsNss.getPosToCreator().put(LexemeType.TYPE_VERB, new VerbVariantCreator(adminControllers,
			LexemeType.TYPE_VERB, orthographyID, columnIndex, dialectColumnIndex));

		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_X, orthographyID, columnIndex, dialectColumnIndex);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_C_UTDR, orthographyID, columnIndex, dialectColumnIndex);

		return variantBuilderNndsNss;
	}

	private void addMiscVariantCreator(VariantBuilder builder, String partOfSpeech, int orthographyID, int columnIndex,
		int columnIndexDialects)
	{
		builder.getPosToCreator().put(partOfSpeech,
			new MiscVariantCreator(adminControllers, partOfSpeech, orthographyID, columnIndex, columnIndexDialects));
	}

	private int getOrthographyID(String abbreviation) throws CodeException
	{
		int id = -1;
		for (var orthography : adminControllers.getOrthographiesController().list()) {
			if ("NSS".equals(orthography.getAbbreviation())) {
				id = orthography.getId();
				break;
			}
		}
		if (id == -1) {
			throw new RuntimeException(String.format("Orthography not found called '%s'", abbreviation));
		}
		return id;
	}
}
