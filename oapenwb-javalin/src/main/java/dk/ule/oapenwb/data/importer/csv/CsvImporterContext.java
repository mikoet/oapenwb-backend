// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.data.importer.csv.dto.CrbiResult;
import dk.ule.oapenwb.data.importer.messages.MessageContainer;
import dk.ule.oapenwb.entity.content.basedata.Language;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class CsvImporterContext
{
	private final CsvImporterConfig config;
	private final MessageContainer messages = new MessageContainer();

	private final Map<String, Language> languages = new HashMap<>();

	private final Set<Long> loadedLexemeIDs = new HashSet<>();

	private CrbiResult result;
}
