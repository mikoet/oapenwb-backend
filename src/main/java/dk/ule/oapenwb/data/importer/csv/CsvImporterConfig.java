// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.data.importer.csv.components.*;
import dk.ule.oapenwb.data.importer.messages.MessageType;
import dk.ule.oapenwb.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

import java.util.*;

/**
 * Contains the configuration for a {@link CsvRowBasedImporter}.
 */
@Data
public class CsvImporterConfig
{
	// -- Basic settings

	/**
	 * Filename to import. This is meant to be the filename only and it will be looked
	 * for in the import directory.
	 */
	private String filename;

	/**
	 * Character that separates the column in the file.
	 */
	private char separationCharacter = '\t';

	/**
	 * Number of columns to be read from the import file for each row. If a row contains less
	 * rows empty values will be created. If it contains more columns than specified the additional
	 * columns will be there and can just be ignored.
	 */
	private int columnCount;

	/**
	 * Number of columns to be at minimum in a row. Else the row cannot be imported.
	 */
	private int minColumnCount;

	/**
	 * Sets how checking shall be performed on import. For more read at {@link CheckType}.
	 */
	private CheckType checkType = CheckType.EverythingBeforeImport;

	/**
	 * If true no data will be persisted.
	 */
	private boolean simulate;

	/**
	 * Name of the filename the log will be written to. Will be automatically generated in constructor.
	 */
	@Setter(AccessLevel.NONE)
	private String logFilename;

	/**
	 * Only messages having this type or higher will be printed into output log.
	 */
	private MessageType outputMinimumType = MessageType.Info;

	/**
	 * Number of rows to be read, handled and persisted before committing a transaction.
	 */
	private int transactionSize = 50;

	/**
	 * List of tag names that all created lexemes will be tagged with.
	 */
	private Set<String> tagNames;

	/**
	 * Add the indices of the rows to be skipped on import.
	 */
	private Set<Integer> skipRows = new HashSet<>();

	/**
	 * Index of the column that contains the Part of Speech tag which is used to reference
	 * the {@link dk.ule.oapenwb.entity.content.basedata.LexemeType}.
	 */
	private int posColIndex;

	/**
	 * Contains all allowed Part of Speech values. Rows not having one of these values for PoS will be skipped.
	 */
	private Set<String> allowedPos = new HashSet<>();

	// -- Fancy settings

	/**
	 * Optional import condition that can check if a row shall be imported.
	 */
	private ImportCondition importCondition = null;

	/**
	 *
	 */
	private Map<String, LexemeProvider> lexemeProviders = new HashMap<>();

	/**
	 *
	 */
	private Map<String, MultiLexemeProvider> multiLexemeProviders = new HashMap<>();

	/**
	 *
	 */
	private List<MappingMaker> mappingMakers = new LinkedList<>();

	/**
	 *
	 */
	private List<LinkMaker> linkMakers = new LinkedList<>();

	/**
	 * Default constructor
	 */
	public CsvImporterConfig()
	{
		this.logFilename = SecurityUtil.createRandomString(12, SecurityUtil.ALPHABET_AND_NUMBERS);
	}
}
