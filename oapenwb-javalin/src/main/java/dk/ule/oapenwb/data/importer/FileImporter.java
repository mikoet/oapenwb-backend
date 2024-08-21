// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.context.ITransaction;
import dk.ule.oapenwb.persistency.entity.ApiAction;
import dk.ule.oapenwb.persistency.entity.content.basedata.Language;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.persistency.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.persistency.entity.content.lexemes.LexemeForm;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lemma;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Imports a
 */
public class FileImporter
{
	private static final Logger LOG = LoggerFactory.getLogger(FileImporter.class);
	private static final String LEMMA_REGEX_DE = "[A-Za-zÄÖÜäöüßéè]{1,128}";
	private static final String LEMMA_REGEX_EN = "[A-Za-z]{1,128}";
	private static final String LEMMA_REGEX_NL = "[A-Za-zëéè]{1,128}";

	private static final int TRANSACTION_SIZE = 1000;

	private static String GetRegex(String locale)
	{
		switch (locale) {
			case "de":
				return LEMMA_REGEX_DE;
			case "en":
				return LEMMA_REGEX_EN;
			case "nl":
				return LEMMA_REGEX_NL;
		}
		throw new RuntimeException("Unsupported locale: " + locale);
	}

	@AllArgsConstructor
	private class ImportDirective
	{
		LexemeDetailedDTO detailedDTO;
		int lineNumber;
		String lemma;
		int frequency;
	}

	private final ImportConfig config;
	private final AdminControllers adminControllers;

	private Language language;
	// <Lexeme Type Name, <LexemeType, BaseFormType e.g. infinitive>
	private Map<String, Pair<LexemeType, LexemeFormType>> typeBaseFormMap = new HashMap<>();

	public FileImporter(ImportConfig config, AdminControllers adminControllers) throws CodeException
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
		// Load the language
		List<Language> languageList = this.adminControllers.getLanguagesController().getBy(
			new FilterCriterion("locale", config.getLocale(), FilterCriterion.Operator.Equals));
		if (languageList.size() != 1) {
			throw new RuntimeException("Number of languages found for locale " + config.getLocale() + " is "
				+ languageList.size());
		}
		this.language = languageList.get(0);

		// Fill the typeBaseFormMap
		for (LexemeType type : this.adminControllers.getLexemeTypesController().list()) {
			List<LexemeFormType> formTypes = this.adminControllers.getLexemeFormTypesController()
												 .getEntitiesByGroupKey(type.getId());
			// Only when formTypes are already created for the lexeme type
			if (formTypes != null && formTypes.size() > 0) {
				// Find the base form type = the one with the smallest positional index
				LexemeFormType baseFormType = formTypes.get(0);
				for (LexemeFormType formType : formTypes) {
					if (formType.getPosition() < baseFormType.getPosition()) {
						baseFormType = formType;
					}
				}
				//
				this.typeBaseFormMap.put(type.getName(), new Pair<>(type, baseFormType));
			}
		}
	}

	public ImportResult run() {
		ImportResult result = new ImportResult();
		try {
			// De ID-prövings uutstellen
			HibernateUtil.setDisableJsonIdChecks(true);

			// Inleasen
			LOG.info("Lease in");
			List<ImportDirective> directives = readData(result);

			// Sorteren
			LOG.info("Sortere");
			directives.sort(Comparator.comparingInt(o -> o.frequency));

			// Wo houg is de frequens alle ca. 10%?
			double step = directives.size() / 50.0;
			int relativePosition = 0;
			for (double position = 0.0; position <= (directives.size() + step/2.0); position += step) {
				int index = (int) Math.round(position);
				if (index >= directives.size()) {
					index = directives.size() - 1;
				}
				LOG.info("Frequency at position {}% (index {}) is {}", relativePosition, index, directives.get(index).frequency);
				relativePosition += 100 / 50;
			}

			// Seakern
			if (!config.isSimulate()) {
				saveData(result, directives);
			}
			result.setSuccessful(true);
		} catch (IOException e) {
			LOG.error("Import was aborted");
		}

		LOG.info("readCount: " + result.getReadCount());
		LOG.info("saveCount: " + result.getSaveCount());
		LOG.info("skipCount: " + result.getSkipCount());
		LOG.info("totalFrequency: " + result.getTotalFrequency());
		LOG.info("bellowMinFrequency: " + result.getBellowMinFrequency());
		LOG.info("successful: " + result.isSuccessful());

		return result;
	}

	private List<ImportDirective> readData(ImportResult result) throws IOException {
		List<ImportDirective> detailedDTOs = new LinkedList<>();
		int lineNumber = 0;
		try (BufferedReader br = new BufferedReader(new FileReader(config.getFilename()))) {
			for(String line; (line = br.readLine()) != null; ) {
				lineNumber++;
				// Split by the tab char
				String[] parts = line.split("\t");
				// Constraint for each line
				if (parts.length < 3 || parts.length % 2 != 1) {
					throw new RuntimeException("Line " + lineNumber + " does not fullfill constraint 'length >= 3 || length % 2 == 1'");
				}
				// Iterate over the parts. First part is always the lemma followed by 1..n pairs
				// of a) a lexeme type (PoS) and b) a frequency for the lemma with that lexeme type.
				String lemma = parts[0];

				if (!lemma.matches(GetRegex(config.getLocale()))) {
					LOG.warn("Lemma '{}' in line {} contains not allowed characters", lemma, lineNumber);
				} else {
					for (int pairIndex = 0; pairIndex < (parts.length - 1) / 2; pairIndex++) {
						String pos = parts[pairIndex * 2 + 1];
						int frequency = Integer.parseInt(parts[pairIndex * 2 + 2]);
						//if (frequency >= config.getMinFrequency()) {
						LOG.debug("Importing lemma '{}' in line {} with frequency {}", lemma, lineNumber, frequency);
						LexemeDetailedDTO detailedDTO = this.createDetailedLexeme(lemma, pos, frequency);
						if (detailedDTO != null) {
							detailedDTOs.add(new ImportDirective(detailedDTO, lineNumber, lemma, frequency));
							result.incReadCount();
							result.incTotalFrequency(frequency);
						} else {
							// Additional logging to that done in #createDetailedLexeme()
							LOG.warn("\tSkipped in line {}", lineNumber);
							result.incSkipCount();
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.error(String.format("Error in line# %d", lineNumber), e);
			throw e;
		}
		return detailedDTOs;
	}

	private void saveData(ImportResult result, List<ImportDirective> directives)
	{
		Context c = new Context(true);
		ITransaction t = null;
		int transactionNumber = 0;
		long counter = 0L;
		ImportDirective directive = null;

		// Utilise the transaction skip count
		if (config.getTransactionSkipCount().isPresent()) {
			counter = config.getTransactionSkipCount().get() * TRANSACTION_SIZE;
		}

		try {
			ExistenceChecker checker = new ExistenceChecker();
			ListIterator<ImportDirective> iterator = directives.listIterator();
			while (iterator.hasNext()) {
				// Transaction handling: Do a commit every X lines
				if (!config.isSimulate() && counter % TRANSACTION_SIZE == 0) {
					if (t != null) {
						// Commit the active transaction
						t.commit();
					}
					// Start a new transaction
					t = c.beginTransaction();
					LOG.info("Started transaction no# {}", transactionNumber);
					c.setRevisionComment(String.format(
						"Import via FileImporter (locale='%s', filename='%s', totalFrequency=%d, "
							+ "minFrequencyPercentage=%f, transaction#=%d)",
						config.getLocale(), config.getFilename(), result.getTotalFrequency(),
						config.getMinFrequencyPercentage(), transactionNumber));
					transactionNumber++;
				}

				directive = iterator.next();
				LexemeDetailedDTO detailedDTO = directive.detailedDTO;
				Lexeme lexeme = detailedDTO.getLexeme();
				int frequency = (int) lexeme.getProperties().get("import-frequency");
				double share = (double) frequency / (double) result.getTotalFrequency() * 100.0;
				if (share < config.getMinFrequencyPercentage()) {
					lexeme.getTags().add("low frequency");
					lexeme.setActive(false);
					result.incBellowMinFrequency();
				}
				// Check if the lexeme is already available in the database
				// TODO
				if (!config.isSimulate()) {
					if (!checker.lexemeExists(directive.lemma, lexeme.getTypeID(), lexeme.getLangID())) {
						//LOG.debug("Creating lexeme for lemma '{}'", directive.lemma);
						this.adminControllers.getLexemesController().create(detailedDTO, Context.USE_OUTER_TRANSACTION_CONTEXT);
						result.incSaveCount();
					} else {
						//LOG.debug("Skipping to create lexeme for lemma '{}'", directive.lemma);
					}
				}
				counter++;
			}
			if (!config.isSimulate() && t != null) {
				t.commit();
			}
		} catch (Exception e) {
			String lemma = directive != null ? directive.lemma : null;
			int line = directive != null ? directive.lineNumber : -1;
			LOG.error(String.format(
				"Error for lexeme in line %d, lemma='%s' transaction# %d / counter %d",
				line, lemma, transactionNumber, counter), e);
			if (!config.isSimulate()) {
				t.rollback();
			}
		}
	}

	private LexemeDetailedDTO createDetailedLexeme(String lemmaText, String pos, int frequency)
	{
		Pair<LexemeType, LexemeFormType> baseTypePair = this.typeBaseFormMap.get(pos);
		if (baseTypePair == null) {
			LOG.warn("Lemma '{}' utilises unknown PoS '{}' and is skipped", lemmaText, pos);
			return null;
		}

		LexemeDetailedDTO result = new LexemeDetailedDTO();

		// Create the lexeme itself
		{
			Lexeme lexeme = new Lexeme();
			lexeme.setLangID(this.language.getId());
			lexeme.setTypeID(baseTypePair.getLeft().getId());
			lexeme.getTags().add("imported");
			if (config.getTagName() != null && !config.getTagName().isEmpty()) {
				lexeme.getTags().add(config.getTagName());
			}
			lexeme.setCreatorID(null);
			lexeme.getProperties().put("import-frequency", frequency);

			lexeme.setActive(true);
			lexeme.setApiAction(ApiAction.Insert);
			lexeme.setChanged(true);

			result.setLexeme(lexeme);
		}

		// Create the main variant and its base lexeme form
		{
			LexemeForm form = new LexemeForm();
			form.setState(LexemeForm.STATE_TYPED);
			form.setFormTypeID(baseTypePair.getRight().getId());
			form.setText(lemmaText);

			Lemma lemma = new Lemma();
			lemma.setFillLemma(Lemma.FILL_LEMMA_AUTOMATICALLY);

			Variant variant = new Variant();
			variant.setId(-1L);
			variant.setMainVariant(true);
			variant.setOrthographyID(this.language.getMainOrthographyID());
			variant.setLexemeForms(List.of(form));
			variant.setLemma(lemma);

			variant.setActive(true); // TODO
			variant.setApiAction(ApiAction.Insert);
			variant.setChanged(true);
			result.setVariants(List.of(variant));
		}

		// Create a common default sememe
		{
			Sememe sememe = new Sememe();
			sememe.setId(-1L);
			sememe.setInternalName("$default");
			sememe.setVariantIDs(Set.of(-1L));
			sememe.setFillSpec(Sememe.FILL_SPEC_NONE);

			sememe.setActive(true); // TODO
			sememe.setApiAction(ApiAction.Insert);
			sememe.setChanged(true);
			result.setSememes(List.of(sememe));
		}

		result.setMappings(new ArrayList<>());
		result.setLinks(new ArrayList<>());

		return result;
	}
}