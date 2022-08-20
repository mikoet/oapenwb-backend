// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.IMessage;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.data.importer.csv.components.LinkMaker;
import dk.ule.oapenwb.data.importer.csv.components.MappingMaker;
import dk.ule.oapenwb.data.importer.csv.data.ProviderData;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.csv.dto.CrbiResult;
import dk.ule.oapenwb.data.importer.messages.Message;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.entity.content.lexemes.Link;
import dk.ule.oapenwb.entity.content.lexemes.Mapping;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeSlimDTO;
import dk.ule.oapenwb.logic.context.Context;
import dk.ule.oapenwb.logic.context.ITransaction;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.functional.TriCheckFunction;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * <p>The CsvRowBasedImporter was written to import the initial data for the Low Saxon dictionary that was
 * written into a Google sheets and then exported into a tab separated file. CSV does not stand for Comma- but
 * Character-Separated Value here, and the character that devides the single values of a row can be configured.</p>
 */
public class CsvRowBasedImporter
{
	public static final String CONTEXT_READ_DATA = "read data";
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
			buildAndPersistStructures(readData());

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

		Path inputFilePath = Paths.get(appConfig.getImportConfig().getInputDir(), config.getFilename());

		try (BufferedReader br = new BufferedReader(new FileReader(inputFilePath.toFile()))) {
			for(String line; (line = br.readLine()) != null; ) {
				lineNumber++;

				// Skip row if configured to do so
				if (skipRows.contains(lineNumber)) {
					context.getMessages().add(CONTEXT_READ_DATA, MessageType.Info, "Skipped row as configured",
						lineNumber, -1);
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
					if (PoS.isBlank()) {
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
						context.getMessages().add(CONTEXT_READ_DATA, MessageType.Info, "Skipped row by import condition",
							lineNumber, -1);
						context.getResult().incSkipCount();
					}
				} catch (Exception e) {
					context.getMessages().add(CONTEXT_READ_DATA, MessageType.Error, e.getMessage(), lineNumber, -1);
				}
			}
		} catch (Exception e) {
			LOG.error(String.format("Some mysterious error in line# %d stopped the import", lineNumber), e);
			context.getMessages().add(CONTEXT_READ_DATA, MessageType.Error,
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

		// If the parts array has less elements than columnCount then create a new array of size columnCount
		if (parts.length < config.getColumnCount()) {
			String[] oldParts = parts;
			parts = new String[config.getColumnCount()];
			// This is better than an own for-loop to transfer from oldParts to parts
			System.arraycopy(oldParts, 0, parts, 0, oldParts.length);
		}

		// Set an empty string for those that are null and trim all other non-null parts
		for (int i = 0; i < parts.length; i++) {
			if (parts[i] == null) {
				parts[i] = "";
			} else {
				parts[i] = parts[i].trim();
			}
		}

		return parts;
	}

	private void buildAndPersistStructures(List<RowData> rowDataList)
	{
		LOG.info("Building and persisting structures");

		List<ProviderData> providerDataList = new LinkedList<>();

		// Variables for transaction management
		Context c = new Context(true);
		ITransaction t = null;
		int transactionNumber = 0;
		int counter = 0;

		rowLoop: for (RowData row : rowDataList) {
			// Transaction handling: Do a commit every N handled rows
			if (!config.isSimulate() && counter % config.getTransactionSize() == 0) {
				// Commit and start a new transaction
				commitTransaction(t, transactionNumber, row.getLineNumber(), providerDataList);
				t = c.beginTransaction();
				transactionNumber++;
				LOG.info("Starting transaction# {}", transactionNumber);
				c.setRevisionComment(String.format("Import via %s (filename='%s', transaction#=%d)",
					getClass().getSimpleName(), config.getFilename(), transactionNumber));
				// …as well as a new run on filling the list.
				providerDataList = new LinkedList<>();
			}

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

				// Another row was successfully handled
				providerDataList.add(new ProviderData(row, providerResults, multiProviderResults));
				counter++;

				// Before committing the transaction in the next loop… do this:
				if (counter % config.getTransactionSize() == 0) {
					persistProviderDataAndMakeMappingsAndLinks(providerDataList);
				}
			} catch (Exception e) {
				Message message = context.getMessages().add(CONTEXT_BUILD_STRUCTURES, MessageType.Error, e.getMessage(),
					row.getLineNumber(), -1);
				LOG.warn(message.toString(), e);
			}
		} // -- rowLoop

		if (!config.isSimulate() && providerDataList.size() > 0) {
			try {
				persistProviderDataAndMakeMappingsAndLinks(providerDataList);
				commitTransaction(t, transactionNumber, -1, providerDataList);
			} catch (CodeException e) {
				// TODO MessageUtil.printFormatted(IMessage)
				Message message = context.getMessages().add(CONTEXT_BUILD_STRUCTURES, MessageType.Error, e.getMessage(),
					rowDataList.size() + 1, -1);
				LOG.error(message.toString(), e);
			} catch (MultiCodeException e) {
				for (IMessage message : e.getErrors()) {
					// TODO MessageUtil.printFormatted(IMessage)
				}
				int z = 0;
				// TODO hwa maybe rethrow these two expeptions (böäverne catch-blok ouk)
			}
		}
	}

	private void persistProviderDataAndMakeMappingsAndLinks(List<ProviderData> providerDataList)
		throws CodeException, MultiCodeException
	{
		if (providerDataList == null || providerDataList.size() == 0) {
			context.getMessages().add(CONTEXT_PERSIST_PROVIDER_DATA, MessageType.Warning,
				"Method persistProviderData() was called without any data to persist", -1, -1);
			return;
		}

		Session session = HibernateUtil.getSession();

		// !! Persist all not yet persistent detailedDTOs from the LexemeProviders and MultiLexemeProviders
		for (var data : providerDataList) {
			try {
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

				// Create the mappings
				if (config.getMappingMakers() != null && config.getMappingMakers().size() > 0) {
					for (MappingMaker maker : config.getMappingMakers()) {
						List<Mapping> mappings = maker.build(config, context, sememeIDsOfLoop);
						if (mappings != null && mappings.size() > 0) {
							for (Mapping mapping : mappings) {
								// TODO Check if such mapping already exists
								session.persist(mapping);
							}
						}
					}
				}

				// Create the links
				if (config.getLinkMakers() != null && config.getLinkMakers().size() > 0) {
					for (LinkMaker maker : config.getLinkMakers()) {
						List<Link> links = maker.build(config, context, sememeIDsOfLoop);
						if (links != null && links.size() > 0) {
							for (Link link : links) {
								// TODO Check if such link already exists
								session.persist(link);
							}
						}
					}
				}
			} catch (Exception e) {
				int lineNumber = data.getRowData().getLineNumber();
				int batchFirstLine = -1;
				int batchLastLine = -1;

				if (providerDataList.size() > 0) {
					batchFirstLine = providerDataList.get(0).getRowData().getLineNumber();
					batchLastLine = providerDataList.get(providerDataList.size() - 1).getRowData().getLineNumber();
				}

				String message = String.format(
					"A critical error occured in method 'persistProviderDataAndMakeMappingsAndLinks': " +
						"processed line = %d, batch first line = %d, batch last line = %d - error: %s",
					lineNumber, batchFirstLine, batchLastLine, e.getMessage());
				LOG.warn(message, e);
				throw new RuntimeException(message);
			}

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

	private void commitTransaction(ITransaction t, int transactionNumber, int lineNumber,
		final List<ProviderData> providerDataList)
	{
		if (t != null) {
			try {
				// Commit the active transaction
				t.commit();
			} catch (Exception e) {
				String failureMessage = String.format("Transaction handling failed in transaction# %d",
					transactionNumber);
				LOG.error(failureMessage);
				LOG.error("Error is", e);

				context.getMessages().add(CONTEXT_PERSIST_PROVIDER_DATA, MessageType.Error, failureMessage, lineNumber, -1);
				context.getMessages().add(CONTEXT_PERSIST_PROVIDER_DATA, MessageType.Error,
					String.format("Error: %s", e.getMessage()), lineNumber, -1);

				if (providerDataList.size() > 0) {
					int first = providerDataList.get(0).getRowData().getLineNumber();
					int last = providerDataList.get(providerDataList.size() - 1).getRowData().getLineNumber();
					String warnMessage = String.format("Rows %d to %d have probably not been committed.",
						first, last);
					LOG.warn(warnMessage);
					context.getMessages().add(CONTEXT_PERSIST_PROVIDER_DATA, MessageType.Error, warnMessage, lineNumber, -1);
				}

				if (!config.isSimulate()) {
					t.rollback();
				}
			}
		}
	}
}
