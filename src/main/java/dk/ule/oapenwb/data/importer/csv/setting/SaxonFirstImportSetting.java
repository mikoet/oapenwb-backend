// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.setting;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.CsvImporterConfig;
import dk.ule.oapenwb.data.importer.csv.components.*;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.multi.MultiVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.multi.OperationMode;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.ImportMode;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.MiscVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.NounVariantCreator;
import dk.ule.oapenwb.data.importer.csv.components.variantcreators.saxon.VerbVariantCreator;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.entity.content.basedata.LinkType;
import dk.ule.oapenwb.entity.content.basedata.Orthography;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.util.Pair;

import java.util.*;

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

	public static final short DEFAULT_MAPPING_WEIGHT = (short) 50;

	private final AdminControllers adminControllers;

	private final int langIdLowSaxon;
	private final Map<String, Language> dialectMap;

	public SaxonFirstImportSetting(AdminControllers adminControllers) throws CodeException
	{
		this.adminControllers = adminControllers;

		// Fetch the lang ID for Low Saxon
		List<Language> langList = adminControllers.getLanguagesController().getBy(
			new FilterCriterion("locale", "nds", FilterCriterion.Operator.Equals));
		if (langList.size() == 1) {
			this.langIdLowSaxon = langList.get(0).getId();
		} else {
			throw new CodeException(ErrorCode.Admin_EntitiesNotFoundViaSingleCriterion,
				Arrays.asList(new Pair<>("type", "Language"), new Pair<>("property", "locale"),
					new Pair<>("value", "nds")));
		}

		// Fetch all dialect IDs
		this.dialectMap = adminControllers.getLanguagesController().getImportMapForLanguage("nds");
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
		cfg.setOutputMinimumType(MessageType.Info);
		cfg.setTagNames(Set.of("imported", "loup1"));
		cfg.setTransactionSize(50);
		cfg.setSkipRows(Set.of(1));
		cfg.setPosColIndex(COL_PART_OF_SPEECH);
		cfg.getAllowedPos().addAll(Set.of(LexemeType.TYPE_ADJ, LexemeType.TYPE_ADP, LexemeType.TYPE_ADV,
			LexemeType.TYPE_AUX, LexemeType.TYPE_CCONJ, LexemeType.TYPE_DET,  LexemeType.TYPE_INTJ,
			LexemeType.TYPE_NOUN , LexemeType.TYPE_NUM, LexemeType.TYPE_PART, LexemeType.TYPE_PRON,
			LexemeType.TYPE_PROPN, /*LexemeType.TYPE_PUNCT, */ LexemeType.TYPE_SCONJ, LexemeType.TYPE_VERB /*,
			LexemeType.TYPE_X*/, LexemeType.TYPE_C_UTDR));
		cfg.setImportCondition(row -> {
			String firstPart = row.getParts()[COL_NICH_IMPORTEREN - 1];
			return firstPart.isEmpty();
		});
		// Duplicate check is implemented for Low Saxon only
		cfg.setLangsForDuplicateCheck(Set.of("nds"));
		cfg.setDuplicateCheckKeyBuilder(dto -> {
			// Uut de deepsten deepden weren wy koamen,
			// koamen to blyven weren wy.
			if (langIdLowSaxon != dto.getLexeme().getLangID()) {
				return null;
			}
			Set<String> result = new HashSet<>();
			for (Variant variant : dto.getVariants()) {
				if (variant.getLexemeForms().size() > 0) {
					result.add(variant.getLexemeForms().get(0).getText());
				}
			}
			return result;
		});

		// !! Set up LexemeProviders
		cfg.getLexemeProviders().put("nds", buildLexemeProviderNds());

		// !! Set up MultiLexemeProviders
		Map<String, MultiLexemeProvider> multiLexemeProviders = cfg.getMultiLexemeProviders();
		multiLexemeProviders.put("fi", buildMultiLexemeProvider(
			OperationMode.Default, "fi", Orthography.ABBR_FINNISH, COL_FINISH, false));
		multiLexemeProviders.put("sv", buildMultiLexemeProvider(
			OperationMode.Swedish, "sv", Orthography.ABBR_SWEDISH, COL_SWEDISH, false));
		multiLexemeProviders.put("da", buildMultiLexemeProvider(
			OperationMode.Danish, "da", Orthography.ABBR_DANISH, COL_DANISH, false));
		multiLexemeProviders.put("de", buildMultiLexemeProvider(
			OperationMode.Default, "de", Orthography.ABBR_GERMAN_FEDERAL, COL_GERMAN, false));
		multiLexemeProviders.put("en", buildMultiLexemeProvider(
			OperationMode.English, "en", Orthography.ABBR_ENGLISH_BRITISH, COL_ENGLISH, false));
		multiLexemeProviders.put("nl", buildMultiLexemeProvider(
			OperationMode.English, "nl", Orthography.ABBR_DUTCH, COL_DUTCH, false));
		multiLexemeProviders.put("bino", buildMultiLexemeProvider(
			OperationMode.Default, "bino", Orthography.ABBR_BINOMIAL_NOMENCLATURE, COL_SCIENTIFIC_NAME, false));

		// !! Set up the MappingMakers
		List<MappingMaker> mappingMakers = cfg.getMappingMakers();
		mappingMakers.add(createMappingMaker("nds-fi"));
		mappingMakers.add(createMappingMaker("nds-sv"));
		mappingMakers.add(createMappingMaker("nds-da"));
		mappingMakers.add(createMappingMaker("nds-de"));
		mappingMakers.add(createMappingMaker("nds-en"));
		mappingMakers.add(createMappingMaker("nds-nl"));

		// !! Set up the LinkMaker
		Optional<LinkType> ltBino = adminControllers.getLinkTypesController().list().stream().filter(
			lt -> lt.getDescription().equals(
				LinkType.DESC_BINOMIAL_NOMEN)).findFirst();
		if (ltBino.isEmpty()) {
			throw new RuntimeException(String.format("LinkType '%s' not found", LinkType.DESC_BINOMIAL_NOMEN));
		}
		List<LinkMaker> linkMakers = cfg.getLinkMakers();
		linkMakers.add(new LinkMaker(MakerMode.SingleAndSingle, adminControllers, ltBino.get(), "nds", "bino"));

		return cfg;
	}

	private LexemeProvider buildLexemeProviderNds() throws CodeException
	{
		// !! Setup VariantBuilder
		// Northern Low Saxon in NSS
		VariantBuilder variantBuilderNndsNss = setupVariantBuilder(
			ImportMode.NSS,
			getOrthographyID(Orthography.ABBR_SAXON_NYSASSISKE_SKRYVWYSE),
			COL_NDS_NSS,
			COL_NDS_NSS_DIALECTS,
			dialectMap,
			Set.of(dialectMap.get("dns").getId()));

		// Westphalian Dutch Low Saxon in NSS
		VariantBuilder variantBuilderNdsNlNss = setupVariantBuilder(
			ImportMode.NSS,
			getOrthographyID(Orthography.ABBR_SAXON_NYSASSISKE_SKRYVWYSE),
			COL_NDS_NL_NSS,
			COL_NDS_NL_NSS_DIALECTS,
			dialectMap,
			Set.of(dialectMap.get("nwf").getId()));

		// Northern Low Saxon in DBO
		// TODO The origin should be imported as well
		VariantBuilder variantBuilderNndsDbo = setupVariantBuilder(
			ImportMode.DBO,
			getOrthographyID(Orthography.ABBR_SAXON_GERMAN_BASED),
			COL_NDS_DBO,
			COL_NDS_DBO_DIALECTS,
			dialectMap,
			Set.of(dialectMap.get("dns").getId()));

		LexemeProvider lexemeProvider = new LexemeProvider(adminControllers, "nds", true);
		lexemeProvider.getVariantBuilders().add(variantBuilderNndsNss);
		lexemeProvider.getVariantBuilders().add(variantBuilderNdsNlNss);
		lexemeProvider.getVariantBuilders().add(variantBuilderNndsDbo);

		return lexemeProvider;
	}

	private VariantBuilder setupVariantBuilder(ImportMode mode, int orthographyID, int columnIndex,
		int dialectColumnIndex, Map<String, Language> dialectMap, Set<Integer> defaultDialectID)
	{
		VariantBuilder variantBuilderNndsNss = new VariantBuilder();

		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_ADJ, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_ADP, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_ADV, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_AUX, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_CCONJ, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_DET, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_INTJ, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);

		variantBuilderNndsNss.getPosToCreator().put(LexemeType.TYPE_NOUN, new NounVariantCreator(adminControllers,
			LexemeType.TYPE_NOUN, mode, orthographyID, columnIndex, dialectColumnIndex, dialectMap, defaultDialectID));

		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_NUM, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PART, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PRON, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PROPN, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_PUNCT, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_SCONJ, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);

		variantBuilderNndsNss.getPosToCreator().put(LexemeType.TYPE_VERB, new VerbVariantCreator(adminControllers,
			LexemeType.TYPE_VERB, orthographyID, columnIndex, dialectColumnIndex, dialectMap, defaultDialectID));

		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_X, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);
		addMiscVariantCreator(variantBuilderNndsNss, LexemeType.TYPE_C_UTDR, orthographyID, columnIndex,
			dialectColumnIndex, dialectMap, defaultDialectID);

		return variantBuilderNndsNss;
	}

	private void addMiscVariantCreator(VariantBuilder builder, String partOfSpeech, int orthographyID, int columnIndex,
		int columnIndexDialects, Map<String, Language> dialectMap, Set<Integer> defaultDialectID)
	{
		builder.getPosToCreator().put(partOfSpeech,
			new MiscVariantCreator(adminControllers, partOfSpeech, orthographyID, columnIndex, columnIndexDialects,
				dialectMap, defaultDialectID));
	}

	private MultiLexemeProvider buildMultiLexemeProvider(OperationMode mode, String langCode, String orthography,
		int columnIndex, boolean mustProvide) throws CodeException
	{
		// !! Setup VariantBuilder
		VariantBuilder variantBuilder = setupVariantMultiBuilder(
			mode,
			getOrthographyID(orthography),
			columnIndex);

		MultiLexemeProvider lexemeProvider = new MultiLexemeProvider(adminControllers, langCode, mustProvide, columnIndex);
		lexemeProvider.getVariantBuilders().add(variantBuilder);

		return lexemeProvider;
	}

	private VariantBuilder setupVariantMultiBuilder(OperationMode mode, int orthographyID, int columnIndex)
	{
		VariantBuilder variantBuilder = new VariantBuilder();

		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_ADJ, orthographyID, columnIndex);

		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_ADP, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_ADV, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_AUX, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_CCONJ, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_DET, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_INTJ, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_NOUN, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_NUM, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_PART, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_PRON, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_PROPN, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_PUNCT, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_SCONJ, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_VERB, orthographyID, columnIndex, mode, false);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_X, orthographyID, columnIndex);
		addMultiVariantCreator(variantBuilder, LexemeType.TYPE_C_UTDR, orthographyID, columnIndex,
			OperationMode.Default, true);

		return variantBuilder;
	}

	private void addMultiVariantCreator(VariantBuilder builder, String partOfSpeech, int orthographyID, int columnIndex,
		OperationMode mode, boolean multiWord)
	{
		builder.getPosToCreator().put(partOfSpeech, new MultiVariantCreator(adminControllers, partOfSpeech,
			orthographyID, columnIndex, mode, multiWord));
	}

	private void addMultiVariantCreator(VariantBuilder builder, String partOfSpeech, int orthographyID, int columnIndex)
	{
		builder.getPosToCreator().put(partOfSpeech, new MultiVariantCreator(adminControllers, partOfSpeech,
			orthographyID, columnIndex, OperationMode.Default, false));
	}

	private int getOrthographyID(String abbreviation) throws CodeException
	{
		int id = -1;
		for (var orthography : adminControllers.getOrthographiesController().list()) {
			if (orthography.getAbbreviation().equals(abbreviation)) {
				id = orthography.getId();
				break;
			}
		}
		if (id == -1) {
			throw new RuntimeException(String.format("Orthography not found called '%s'", abbreviation));
		}
		return id;
	}

	private MappingMaker createMappingMaker(String langPair) throws CodeException
	{
		return new MappingMaker(MakerMode.SingleAndMulti, adminControllers, langPair, DEFAULT_MAPPING_WEIGHT);
	}
}
