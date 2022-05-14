// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.data.importer.csv.modules.ImportCondition;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

/**
 * Contains the settings
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
	 * Add the indices of the rows to be skipped on import.
	 */
	private Set<Integer> skipRows = new HashSet<>();

	/**
	 * Index of the column that contains the Part of Speech tag which is used to reference
	 * the {@link dk.ule.oapenwb.entity.content.basedata.LexemeType}.
	 */
	private int posColIndex;

	// -- Fancy settings

	/**
	 * Optional import condition that can check if a row shall be imported.
	 */
	private ImportCondition importCondition = null;

	//private HashMap<LexemeProvider> lexemeProviders;
	//private List<MappingMaker> mappingMakers;
	//
}