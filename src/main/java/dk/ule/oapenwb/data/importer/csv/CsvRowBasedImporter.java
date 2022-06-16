// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.importer.csv.data.RowData;
import dk.ule.oapenwb.data.importer.csv.dto.CrbiResult;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.LexemeFormType;
import dk.ule.oapenwb.entity.content.basedata.LexemeType;
import dk.ule.oapenwb.logic.admin.common.FilterCriterion;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
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
		// Find the languages referenced in the LexemeProviders and MultiLexemeProviders
		for (var provider : config.getLexemeProviders().values()) {
			context.getLanguages().put(provider.getLang(), getLanguage(provider.getLang()));
		}
		for (var provider : config.getMultiLexemeProviders().values()) {
			context.getLanguages().put(provider.getLang(), getLanguage(provider.getLang()));
		}

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

		// TODO Create the lexeme creators
		// this.nounCreator = new NounVariantCreator();
		// this.verbCreator = new VerbVariantCreator(adminControllers, typeFormMap.get(LexemeType.TYPE_VERB));
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
		CrbiResult result = new CrbiResult();
		try {
			// De ID-prövings uutstellen
			HibernateUtil.setDisableJsonIdChecks(true);

			// Inleasen un echte strukturen upbouwen
			buildStructures(readData());

			// Seakern
			if (!config.isSimulate()) {
				// TODO saveData(result, directives);
			}
			result.setSuccessful(true);
		} catch (IOException /*| CodeException */ e) {
			LOG.error("Import was aborted");
		}

		HibernateUtil.setDisableJsonIdChecks(false);

		LOG.info("readCount: " + result.getReadCount());
		LOG.info("saveCount: " + result.getSaveCount());
		LOG.info("skipCount: " + result.getSkipCount());
		LOG.info("successful: " + result.isSuccessful());

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
					continue;
				}

				// Empty and blank lines will be skipped, too
				if (line.isBlank()) {
					continue;
				}

				try {
					// Split to an array of n parts, create a RowData instance and add it to the result list
					String[] parts = splitLineToFixedParts(line, lineNumber);

					// Get the LexemeType only for checking its existance.
					// We'll not save it here to save a little memory.
					String PoS = parts[config.getPosColIndex() - 1];
					if (PoS == null || PoS.isBlank()) {
						throw new RuntimeException("PoS is empty");
					}
					TypeFormPair typeFormPair = this.typeFormMap.get(PoS);
					if (typeFormPair == null || typeFormPair.getLeft() == null) {
						throw new RuntimeException(String.format("PoS '%s' is not known", PoS));
					}

					// Create the RowData instance
					RowData row = new RowData(lineNumber, parts);

					if (config.getImportCondition() == null || config.getImportCondition().meetsCondition(row)) {
						result.add(row);
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
		for (RowData row : rowDataList) {

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

				for (var provider : config.getLexemeProviders().values()) {
					LexemeDetailedDTO dto = provider.provide(context, typeFormPair, row);
				}
				for (var provider : config.getMultiLexemeProviders().values()) {
				}

				LOG.info(String.format("Row with index %d is there and has %d parts",
					row.getLineNumber(), row.getParts().length));
			} catch (Exception e) {
				context.getMessages().add("build structures", MessageType.Error, e.getMessage(), row.getLineNumber(), -1);
			}
		}
	}
}
