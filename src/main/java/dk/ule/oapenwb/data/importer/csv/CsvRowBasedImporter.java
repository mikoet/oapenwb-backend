// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.data.importer.csv.data.ProviderData;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.csv.dto.CrbiResult;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeSlimDTO;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.context.ITransaction;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.functional.TriCheckFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CsvRowBasedImporter
{
	public static final String CONTEXT_BUILD_STRUCTURES = "build structures";
	public static final String CONTEXT_PERSIST_PROVIDER_DATA = "persist provider data";

	// TypeDef for the TypeFormMap and reuse of the type.
	public  static class TypeFormPair extends Pair<LexemeType, LinkedHashMap<String, LexemeFormType>> {
		public TypeFormPair(LexemeType key, LinkedHashMap<String, LexemeFormType> value) {
			super(key, value);
		}
	}
	// TypeDef for the TypeFormMap and reuse of the type.
	public static class TypeFormMap extends HashMap<String, TypeFormPair> {}

	private static final Logger LOG = LoggerFactory.getLogger(CsvRowBasedImporter.class);

	private final CsvImporterContext context;

	private final AppConfig appConfig;

	private final AdminControllers adminControllers;

	private final CsvImporterConfig config;

	// <Lexeme Type Name, <LexemeType, TypeFormPair (LexemeType, LimkedMap<Name, FormType)>>
	private final TypeFormMap typeFormMap = new TypeFormMap();

	// TODO Keep track of those objects that were loaded and those that have to be persisted?
	//      Maybe do so in HashMap<className, id> for the loaded ones?
	private List<Object> objectsToPersist;


	// TODO How to inject the config? Or what to do about it?
	public CsvRowBasedImporter(
		AppConfig appConfig,
		AdminControllers adminControllers,
		CsvImporterConfig config) throws CodeException
	{
		this.context = new CsvImporterContext(config);
		this.appConfig = appConfig;
		this.adminControllers = adminControllers;
		this.config = config;

		performChecks();
		initialise();
	}

	private void performChecks() throws CodeException
	{
		// !! Check appConfig import directories
		TriCheckFunction<String, String, Boolean> directoryCheck = (property, directory, checkWritable) -> {
			if (directory.isBlank()) {
				throw new CodeException(ErrorCode.Import_AppPropertyEmpty,
					List.of(new Pair<>("property", property)));
			} else {
				Path path = Paths.get(directory);
				if (!Files.exists(path) || !Files.isDirectory(path) || (checkWritable && !Files.isWritable(path))) {
					throw new CodeException(ErrorCode.Import_AppPropertyInvalidPath,
						List.of(new Pair<>("property", property)));
				}
			}
		};
		directoryCheck.perform("importConfig->inputDir", appConfig.getImportConfig().getInputDir(), false);
		directoryCheck.perform("importConfig->outputDir", appConfig.getImportConfig().getOutputDir(), true);

		// !! Check filenames
		TriCheckFunction<String, String, String> filenameCheck = (property, filename, directory) -> {
			if (filename.contains("/") || filename.contains("\\")) {
				throw new CodeException(ErrorCode.Import_FilenameCheckFailed,
					List.of(new Pair<>("property", property)));
			}
			// Perform check if fully specified file exists only when directory is given
			if (directory != null) {
				if (!Files.isReadable(Paths.get(directory, filename))) {
					throw new CodeException(ErrorCode.Import_FileNotExists,
						List.of(new Pair<>("property", property)));
				}
			}
		};
		filenameCheck.perform("filename", config.getFilename(), appConfig.getImportConfig().getInputDir());
		filenameCheck.perform("logFilename", config.getLogFilename(), null);
	}

	/**
	 * Initialises the attributes language and typeBaseFormMap
	 */
	private void initialise() throws CodeException
	{
		// Find the binominal nomenc. LinkType
		/* TODO This is something the LinkMaker should handle
		for (LinkType linkType : adminControllers.getLinkTypesController().list()) {
			if (LinkType.DESC_BINOMIAL_NOMEN.equals(linkType.getDescription())) {
				this.ltBino = linkType;
				break;
			}
		}*/

		// Fill the typeBaseFormMap
		for (LexemeType type : this.adminControllers.getLexemeTypesController().list()) {
			List<LexemeFormType> formTypes = this.adminControllers.getLexemeFormTypesController()
				.getEntitiesByGroupKey(type.getId());
			// Only when formTypes are already created for the lexeme type
			if (formTypes != null && formTypes.size() > 0) {
				// Sort the list by position so that the base type will be on first position
				formTypes.sort(Comparator.comparingInt(LexemeFormType::getPosition));
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

		// Find the languages referenced in the LexemeProviders and MultiLexemeProviders
		// and call initialise() on each of them
		for (var provider : config.getLexemeProviders().values()) {
			context.getLanguages().put(provider.getLang(), getLanguage(provider.getLang()));
			provider.initialise(typeFormMap);
		}
		for (var provider : config.getMultiLexemeProviders().values()) {
			context.getLanguages().put(provider.getLang(), getLanguage(provider.getLang()));
			provider.initialise(typeFormMap);
		}
	}

	private Language getLanguage(String locale) throws CodeException {
		List<Language> languageList = this.adminControllers.getLanguagesController().getBy(
			new FilterCriterion("locale", locale, FilterCriterion.Operator.Equals));
		if (languageList.size() != 1) {
			throw new RuntimeException("Number of languages found for locale " + locale + " is " + languageList.size());
		}
		return languageList.get(0);
	}

	public CrbiResult run() {
		context.setResult(new CrbiResult());
		try {
			// De ID-prövings uutstellen
			HibernateUtil.setDisableJsonIdChecks(true);

			// Inleasen un echte strukturen upbouwen
			buildStructures(readData());

			// Seakern
			if (!config.isSimulate()) {
				// TODO saveData(result, directives);
				/*
				c.setRevisionComment(String.format(
					"Import via FileImporter (locale='%s', filename='%s', totalFrequency=%d, "
						+ "minFrequencyPercentage=%f, transaction#=%d)",
					config.getLocale(), config.getFilename(), result.getTotalFrequency(),
					config.getMinFrequencyPercentage(), transactionNumber));
				 */
			}
			context.getResult().setSuccessful(true);
		} catch (IOException /*| CodeException */ e) {
			LOG.error("Import was aborted");
		}

		HibernateUtil.setDisableJsonIdChecks(false);

		// Save the recorded messages to file system
		try {
			Path outputFilePath = Paths.get(appConfig.getImportConfig().getOutputDir(), config.getLogFilename());
			dk.ule.oapenwb.util.io.Logger msgLogger = new dk.ule.oapenwb.util.io.Logger(outputFilePath.toString());
			context.getMessages().printToLogger(msgLogger);
			msgLogger.close();
		} catch (IOException e) {
			LOG.error("Writing message log to file failed", e);
		}

		// Return the result and reset it on the context
		CrbiResult result = context.getResult();
		LOG.info("readCount: " + result.getReadCount());
		LOG.info("saveCount: " + result.getSaveCount());
		LOG.info("skipCount: " + result.getSkipCount());
		LOG.info("successful: " + result.isSuccessful());
		context.setResult(null);

		return result;
	}

	private List<RowData> readData() throws IOException
	{
		LOG.info("Reading file");
		List<RowData> result = new LinkedList<>();
		Set<Integer> skipRows = config.getSkipRows();
		int lineNumber = 0;
		Integer stopLineNumber = 25;

		Path inputFilePath = Paths.get(appConfig.getImportConfig().getInputDir(), config.getFilename());

		try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath.toFile()))) {
			for(String line; (line = br.readLine()) != null; ) {
				lineNumber++;

				if (stopLineNumber != null && lineNumber > stopLineNumber) {
					break;
				}

				// Skip row if configured to do so
				if (skipRows.contains(lineNumber)) {
					// TODO Add INFO message
					context.getResult().incSkipCount();
					continue;
				}

				// Empty and blank lines will be skipped, too
				if (line.isBlank()) {
					context.getResult().incSkipCount();
					continue;
				}

				try {
					// Split to an array of n parts, create a RowData instance and add it to the result list
					String[] parts = splitLineToFixedParts(line, lineNumber);

					// Get the LexemeType only for checking its existance.
					// We'll not save it here to save a little memory.
					String PoS = parts[config.getPosColIndex() - 1];
					if (PoS == null || PoS.isBlank()) {
						throw new RuntimeException("PoS is empty. Skipping row.");
					}
					if (!config.getAllowedPos().contains(PoS)) {
						throw new RuntimeException(String.format("PoS '%s' is not allowed. Skipping row.", PoS));
					}
					TypeFormPair typeFormPair = this.typeFormMap.get(PoS);
					if (typeFormPair == null || typeFormPair.getLeft() == null) {
						throw new RuntimeException(String.format("PoS '%s' is not (fully) configured in database", PoS));
					}

					// Create the RowData instance
					RowData row = new RowData(lineNumber, parts);

					if (config.getImportCondition() == null || config.getImportCondition().meetsCondition(row)) {
						result.add(row);
						context.getResult().incReadCount();
					} else {
						// TODO Add INFO message
						context.getResult().incSkipCount();
					}
				/*
				} catch (CodeException e) {
					// TODO replace the parameters
					messages.add("read data", MessageType.Error, e.getMessage(), lineNumber, -1);
				 */
				} catch (Exception e) {
					context.getMessages().add("read data", MessageType.Error, e.getMessage(), lineNumber, -1);
				}
			}
		} catch (Exception e) {
			LOG.error(String.format("Some mysterious error in line# %d stopped the import", lineNumber), e);
			context.getMessages().add("read data", MessageType.Error,
				String.format("Some mysterious error in line# %d stopped the import", lineNumber),
				lineNumber, -1);
			throw e;
		}
		return result;
	}

	private String[] splitLineToFixedParts(String line, int lineNumber)
	{
		// Split by the tab char
		String[] parts = line.split("\t");

		// Constraint for each line
		if (parts.length < config.getMinColumnCount()) {
			throw new RuntimeException(String.format("Line %d does not fullfill constraint 'minColumnCount == %d'",
				lineNumber, config.getMinColumnCount()));
		}

		if (parts.length < config.getColumnCount()) {
			// If the parts array has less elements than columnCount then create a new array of size columnCount
			String[] oldParts = parts;
			parts = new String[config.getColumnCount()];
			// This is better than an own for-loop to transfer from oldParts to parts
			System.arraycopy(oldParts, 0, parts, 0, oldParts.length);
		}

		return parts;
	}

	private void buildStructures(List<RowData> rowDataList)
	{
		LOG.info("Building structures");

		List<ProviderData> providerDataList = new LinkedList<>();

		rowLoop: for (RowData row : rowDataList) {
			try {
				// Get the LexemeType
				String PoS = row.getParts()[config.getPosColIndex() - 1];
				TypeFormPair typeFormPair = this.typeFormMap.get(PoS);

				//CreationConfig config = new CreationConfig(row.getLineNumber(), );

				if (typeFormPair == null) {
					String message = String.format("Entry in line %d utilises unknown PoS '%s' and is skipped",
						row.getLineNumber(), PoS);
					throw new RuntimeException(message);
				}

				/*
				 * hwa
				 * Wat bruke ik allens? Wat wil ik hyr nauw doon?
				 * Ik wil doon:
				 * - dat formaat van de reyge pröven
				 *     - dat heyt do müs ik wul eyns öäver alle linker, mapper, creators usw. itereren
				 *       un dee dat formaat pröven laten
				 *     - het en reyge dår al feylers dän kan ik düsse feylers sammelen un den import van düsse reyge afbreaken
				 * - gung de pröven döär dän bruke ik den mechanismus as uutdacht (med de mappers, creators, usw.)
				 *   un bouwe de datastruktuur up.
				 *     - (!!) wåneyr kyke ik dårby of dat dee al gaev in de databank? (!!)
				 *     - by dat persisteren wardet de lemmata bilded, un dår skal ik my achteran vöär jead variante van
				 *       en lekseem dat lemma ruutgrypen un in en hashmap invögen (<lemma, variante or lekseem>).
				 *         - dat lemma to string a la -> pre + | main + | + post + | + also
				 *     - med düsse hashmap kan ik later al kyken of en variante dår al binnen is un as dat sou is
				 *       en feyler geaven vanweagen "dat woord hadst du doch al angeaven, du döösbaddel!"
				 *         - man wo makede ik dat eygens nauw?
				 *         - to noud kun ik dat lemma vöär de hashmap ouk man blout med de grundform bilden
				 * -
				 * -
				 */

				// Structure for all results of the LexemeProvider instances
				Map<String, LexemeDetailedDTO> providerResults = new HashMap<>();

				for (var provider : config.getLexemeProviders().values()) {
					try {
						LexemeDetailedDTO dto = provider.provide(context, typeFormPair, row);
						if (provider.isMustProvide() && dto == null) {
							// If an empty lexeme is provided we will skip this row
							String message = String.format("Row was skipped since %s returned no lexeme but should have",
								provider.getMessageContext());
							context.getMessages().add(CONTEXT_BUILD_STRUCTURES, MessageType.Warning, message,
								row.getLineNumber(), -1);
							continue rowLoop;
						}
						// Add the DTO to the providerResults
						providerResults.put(provider.getLang(), dto);
					} catch (Exception e) {
						String message = String.format("Row was skipped since %s reported a problem: %s",
							provider.getMessageContext(), e.getMessage());
						throw new RuntimeException(message);
					}
				}

				// Structure for all results of the MultiLexemeProvider instances
				Map<String, List<LexemeDetailedDTO>> multiProviderResults = new HashMap<>();

				for (var provider : config.getMultiLexemeProviders().values()) {
					try {
						List<LexemeDetailedDTO> dtoList = provider.provide(context, typeFormPair, row);
						if (provider.isMustProvide() && (dtoList == null || dtoList.size() == 0)) {
							// If no or an empty list is provided we will skip this row
							String message = String.format("Row was skipped since %s returned no lexemes but should have",
								provider.getMessageContext());
							context.getMessages().add(CONTEXT_BUILD_STRUCTURES, MessageType.Warning, message,
								row.getLineNumber(), -1);
							continue rowLoop;
						}
						// Add the DTO to the multiProviderResults
						multiProviderResults.put(provider.getLang(), dtoList);
					} catch (Exception e) {
						String message = String.format("Row was skipped since %s reported a problem: %s",
							provider.getMessageContext(), e.getMessage());
						throw new RuntimeException(message);
					}
				}

				// TODO hwa
				providerDataList.add(new ProviderData(row, providerResults, multiProviderResults));

				// TODO commit every XY results...or everything after import (by config)
				persistProviderDataAndMakeMappingsAndLinks(providerDataList);

				/*LOG.info(String.format("Row with index %d is there and has %d parts",
					row.getLineNumber(), row.getParts().length));*/
			} catch (Exception e) {
				context.getMessages().add(CONTEXT_BUILD_STRUCTURES, MessageType.Error, e.getMessage(), row.getLineNumber(), -1);
			}

		} // -- rowLoop

		int abc = 0;
		LOG.info(String.format("TODO sememeIDsOfLoop pröven / test %d", abc));
	}

	private void persistProviderDataAndMakeMappingsAndLinks(List<ProviderData> providerDataList)
		throws CodeException, MultiCodeException
	{
		if (providerDataList == null || providerDataList.size() == 0) {
			context.getMessages().add(CONTEXT_PERSIST_PROVIDER_DATA, MessageType.Warning,
				"Method persistProviderData() was called without any data to persist", -1, -1);
			return;
		}

		// TODO dat skul wul beater jichtenswår anders dån warden, villicht an den kontekst?
		//  A la en metood vanweagen handleTransaction() un dän maakt dee dat intern?
		Context c = new Context(true);
		ITransaction t = null;

		// Start a new transaction
		// TODO: t = c.beginTransaction();


		// !! Persist all not yet persistent detailedDTOs from the LexemeProviders and MultiLexemeProviders
		for (var data : providerDataList) {
			// <lang code, List<sememe ID>> contains all sememeIDs of this loop grouped by their belonging locale
			Map<String, List<Long>> sememeIDsOfLoop = new HashMap<>();

			// Process the results of the LexemeProviders
			for (String locale : data.getProviderResults().keySet()) {
				LexemeDetailedDTO detailedDTO = data.getProviderResults().get(locale);
				persistLexemeAndCollectSememeID(sememeIDsOfLoop, locale, detailedDTO);
			}

			// Process the results of the MultiLexemeProviders
			for (String locale : data.getMultiProviderResults().keySet()) {
				List<LexemeDetailedDTO> dtoList = data.getMultiProviderResults().get(locale);
				if (dtoList != null) {
					for (var detailedDTO : dtoList) {
						persistLexemeAndCollectSememeID(sememeIDsOfLoop, locale, detailedDTO);
					}
				}
			}

			// !! hwa Create and persist the mappings and links
			// Process the ???

			int abc = 0;
			LOG.info(String.format("TODO sememeIDsOfLoop pröven / test %d", abc));
		}
	}

	/**
	 * <p>Persists the given lexeme detailedDTO – if not yet peristent – and adds the ID of the first sememe
	 * (lowest ID) into the map sememeIDsOfLoop.</p>
	 *
	 * @param sememeIDsOfLoop map that will contain all sememe IDs of one CSV row
	 * @param locale the locale of the given lexeme
	 * @param detailedDTO the lexeme that was just created or loaded from the database
	 * @throws CodeException can be thrown when errors occur when persisting the lexeme
	 * @throws MultiCodeException can be thrown when errors occur when persisting the lexeme
	 */
	private void persistLexemeAndCollectSememeID(
		Map<String, List<Long>> sememeIDsOfLoop,
		String locale,
		LexemeDetailedDTO detailedDTO) throws CodeException, MultiCodeException
	{
		Long id = detailedDTO.getLexeme().getId();
		Long sememeID;

		if (config.isSimulate() || context.getLoadedLexemeIDs().contains(id)) {
			// This lexeme already existed in the database and thus it already has a persistent sememeID to be used,
			// or we are running in simulation mode and will just take the generated ID.
			sememeID = detailedDTO.getSememes().get(0).getId();
		} else {
			// This lexeme was created by the import and thus will be persisted now in non-simulation mode.
			LexemeSlimDTO slimDTO = this.adminControllers.getLexemesController().create(detailedDTO,
				Context.USE_OUTER_TRANSACTION_CONTEXT);
			context.getResult().incSaveCount();
			sememeID = slimDTO.getFirstSememeID();
		}

		if (sememeID == null) {
			throw new RuntimeException(String.format(
				"Something went wrong. Lexeme for locale '%s' did not result in some kind of a sememe ID (%s).",
				locale, LexemeDetailedDTO.generatedLogStr(detailedDTO)));
		}

		List<Long> sememesList = sememeIDsOfLoop.computeIfAbsent(locale, k -> new LinkedList<>());
		sememesList.add(sememeID);
	}
}
