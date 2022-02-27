// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.sheet;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.sheet.lexemecreator.CreationConfig;
import dk.ule.oapenwb.data.importer.sheet.lexemecreator.NounCreator;
import dk.ule.oapenwb.data.importer.sheet.lexemecreator.VerbCreator;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.entity.content.basedata.LinkType;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class SheetFileImporter
{
	private static final Logger LOG = LoggerFactory.getLogger(SheetFileImporter.class);
	private static final int TRANSACTION_SIZE = 100;

	private static final int INDEX_SAXON_GERMAN_BASED = 0;
	private static final int INDEX_SAXON_NEW_SPELLING = 1;
	private static final int INDEX_PART_OF_SPEECH = 2;
	private static final int INDEX_SCIENTIFIC_NAME = 3;
	private static final int INDEX_LEVELS = 4;
	private static final int INDEX_FINNISH = 5;
	private static final int INDEX_SWEDISH = 6;
	private static final int INDEX_DANISH = 7;
	private static final int INDEX_GERMAN = 8;
	private static final int INDEX_ENGLISH = 9;
	private static final int INDEX_DUTCH = 10;
	private static final int INDEX_SOURCE = 11;

	@AllArgsConstructor
	private class ImportDirective
	{
		int lineNumber;
		String lemma; // the nds-nns lemma
		String pos; // Part of Speech
		LexemeDetailedDTO saxonDTO;	// with NSS and German based variant as well as the level
		LexemeDetailedDTO scientificDTO;
		LexemeDetailedDTO finnishDTO;
		LexemeDetailedDTO swedishDTO;
		LexemeDetailedDTO danishDTO;
		LexemeDetailedDTO englishDTO;
		LexemeDetailedDTO dutchDTO;
	}

	// TypeDef for the TypeFormMap and reuse of the type.
	public  static class TypeFormPair extends Pair<LexemeType, LinkedHashMap<String, LexemeFormType>> {
		public TypeFormPair(LexemeType key, LinkedHashMap<String, LexemeFormType> value) {
			super(key, value);
		}
	};
	// TypeDef for the TypeFormMap and reuse of the type.
	public static class TypeFormMap extends HashMap<String, TypeFormPair> {}

	private final SheetConfig config;
	private final AdminControllers adminControllers;
	// <Lexeme Type Name, <LexemeType, TypeFormPair (LexemeType, LimkedMap<Name, FormType)>>
	private TypeFormMap typeFormMap = new TypeFormMap();

	// Lexeme creators
	private NounCreator nounCreator;
	private VerbCreator verbCreator;

	private Language langSaxon;
	private Language langScientific; // binomial nomenclature
	private Language langFinnish;
	private Language langSwedish;
	private Language langDanish;
	private Language langGerman;
	private Language langEnglish;
	private Language langDutch;

	private LinkType ltBino;

	public SheetFileImporter(SheetConfig config, AdminControllers adminControllers) throws CodeException
	{
		this.config = config;
		this.adminControllers = adminControllers;
		this.initialise();
	}

	/**
	 * Initialises the attributes language and typeBaseFormMap
	 */
	private void initialise() throws CodeException
	{
		// Find the seven languages used in the import
		this.langSaxon = getLanguage("nds");
		this.langScientific = getLanguage("bino"); // binomial nomenclature
		this.langFinnish = getLanguage("fi");
		this.langSwedish = getLanguage("sv");
		this.langDanish = getLanguage("da");
		this.langGerman = getLanguage("de");
		this.langEnglish = getLanguage("en");
		this.langDutch = getLanguage("nl");

		// Find the binominal nomenc. LinkType
		for (LinkType linkType : adminControllers.getLinkTypesController().list()) {
			if (LinkType.DESC_BINOMIAL_NOMEN.equals(linkType.getDescription())) {
				this.ltBino = linkType;
				break;
			}
		}

		// Fill the typeBaseFormMap
		for (LexemeType type : this.adminControllers.getLexemeTypesController().list()) {
			List<LexemeFormType> formTypes = this.adminControllers.getLexemeFormTypesController()
												 .getEntitiesByGroupKey(type.getId());
			// Only when formTypes are already created for the lexeme type
			if (formTypes != null && formTypes.size() > 0) {
				// Sort the list by position so that the base type will be on first position
				formTypes.sort(Comparator.comparingInt(ft -> ft.getPosition()));
				// Find the base form type = the one with the smallest positional index
				LexemeFormType baseFormType = formTypes.get(0);
				for (LexemeFormType formType : formTypes) {
					if (formType.getPosition() < baseFormType.getPosition()) {
						baseFormType = formType;
					}
				}
				// Transfer the list into a LinkedHashMap
				LinkedHashMap<String, LexemeFormType> linkedMap = new LinkedHashMap<>();
				for (LexemeFormType ft : formTypes) {
					linkedMap.put(ft.getName(), ft);
				}
				this.typeFormMap.put(type.getName(), new TypeFormPair(type, linkedMap));
			}
		}

		// Create the lexeme creators
		// TODO this.nounCreator = new NounCreator();
		this.verbCreator = new VerbCreator(adminControllers, typeFormMap.get(LexemeType.TYPE_VERB));
	}

	private Language getLanguage(String locale) throws CodeException {
		List<Language> languageList = this.adminControllers.getLanguagesController().getBy(
			new FilterCriterion("locale", locale, FilterCriterion.Operator.Equals));
		if (languageList.size() != 1) {
			throw new RuntimeException("Number of languages found for locale " + locale + " is " + languageList.size());
		}
		return languageList.get(0);
	}

	public SheetResult run() {
		SheetResult result = new SheetResult();
		try {
			// De ID-prövings uutstellen
			HibernateUtil.setDisableJsonIdChecks(true);

			// Inleasen
			LOG.info("Lease in");
			List<ImportDirective> directives = readData(result);

			// Seakern
			if (!config.isSimulate()) {
				// TODO saveData(result, directives);
			}
			result.setSuccessful(true);
		} catch (IOException | CodeException e) {
			LOG.error("Import was aborted");
		}

		LOG.info("readCount: " + result.getReadCount());
		LOG.info("saveCount: " + result.getSaveCount());
		LOG.info("skipCount: " + result.getSkipCount());
		LOG.info("successful: " + result.isSuccessful());

		return result;
	}

	private List<ImportDirective> readData(SheetResult result) throws IOException, CodeException {
		List<ImportDirective> detailedDTOs = new LinkedList<>();
		int lineNumber = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(config.getFilename()))) {
			// Ignore the header line
			br.readLine();
			// Read the other lines
			for(String line; (line = br.readLine()) != null; ) {
				lineNumber++;

				try {
					// Split to an array of 12 parts
					String[] parts = splitLineToTwelveParts(line, lineNumber);

					// begin tmp
					if (lineNumber == 50) {
						return null;
					}
					//if (line.contains("de Admiral")) {
					LOG.info("PARTs of LINE: {}", line);
					for (int i = 0; i < parts.length; i++) {
						LOG.info("Part {} is: '{}'", i, parts[i]);
					}
					// end tmp

					// Extract the data neccessary for creating the Saxon lexeme
					String saxonGermanBased = parts[INDEX_SAXON_GERMAN_BASED];
					String saxonNewSpelling = parts[INDEX_SAXON_NEW_SPELLING];
					String pos = parts[INDEX_PART_OF_SPEECH];
					String levels = parts[INDEX_LEVELS];
					String source = parts[INDEX_SOURCE];

					// Saxon lemma in NSS and PoS are mandatory
					if (saxonNewSpelling.isEmpty()) {
						throw new RuntimeException("Line " + lineNumber + " does not contain value for nds-nns");
					}
					if (pos.isEmpty()) {
						throw new RuntimeException("Line " + lineNumber + " does not contain Part of Speech");
					}

					// Create the Saxon lexeme
					createSaxonDTO(saxonNewSpelling, saxonGermanBased, pos, levels, source, lineNumber);
				} catch (Exception e) {
					// TODO Sammelen van de feylers hyr implementeren
					LOG.error(e.getMessage());
				}
			}
		} catch (Exception e) {
			LOG.error(String.format("Error in line# %d", lineNumber), e);
			throw e;
		}
		return detailedDTOs;
	}

	private String[] splitLineToTwelveParts(String line, int lineNumber)
	{
		// Split by the tab char
		String[] parts = line.split("\t");

		// Constraint for each line
		if (parts.length < 5) {
			throw new RuntimeException("Line " + lineNumber + " does not fullfill constraint 'length == 12'");
		}

		if (parts.length < 12) {
			// If the parts array has less elements than 12 create a new array of size 12
			String[] oldParts = parts;
			parts = new String[12];
			for (int i = 0; i < oldParts.length; i++) {
				parts[i] = oldParts[i];
			}
		}

		return parts;
	}

	private LexemeDetailedDTO createSaxonDTO(String newSpelling, String germanBased, String pos,
		String levels, String source, int lineNo) throws CodeException
	{
		//String pos = typeFormsPair.getKey().getName();

		final CreationConfig creationConfig = new CreationConfig(lineNo, langSaxon, config.getTagName(), pos);

		switch (pos) {
			case LexemeType.TYPE_VERB -> {
				LexemeDetailedDTO dto = verbCreator.createDTO_Saxon(newSpelling, germanBased, creationConfig);
				logDTO(dto);
				return dto;
			}
			case LexemeType.TYPE_NOUN -> {
			}
		}

		// TODO Towysen van de stylen (levels)
		// TODO Seakern van de born

		throw new RuntimeException(String.format("Unsupported PoS %s in line %d for lemma '%s'", pos, lineNo, newSpelling));
	}

	private void logDTO(LexemeDetailedDTO dto)
	{
		if (dto.getLexeme() != null) {
			Lexeme lexeme = dto.getLexeme();

			StringBuilder sb = new StringBuilder();
			if (dto.getVariants() != null && dto.getVariants().size() > 0) {
				Lemma lemma = dto.getVariants().get(0).getLemma();
				if (lemma != null) {
					if (lemma.getPre() != null && !lemma.getPre().isBlank()) {
						sb.append(lemma.getPre());
						sb.append(' ');
					}

					sb.append(lemma.getMain());

					if (lemma.getPost() != null && !lemma.getPost().isBlank()) {
						sb.append(' ');
						sb.append(lemma.getPost());
					}
				}
			}

			LOG.info("Lemma: {}", sb.toString());
		}
	}
}