// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import dk.ule.oapenwb.data.importer.csv.dto.CrbiResult;
import dk.ule.oapenwb.data.importer.messages.MessageContainer;
import dk.ule.oapenwb.entity.content.basedata.Language;
import lombok.Data;

import java.util.HashMap;

@Data
public class CsvImporterContext
{
	private final CsvImporterConfig config;
	private final MessageContainer messages = new MessageContainer();

	private final HashMap<String, Language> languages = new HashMap<>();

	private CrbiResult result;
}
