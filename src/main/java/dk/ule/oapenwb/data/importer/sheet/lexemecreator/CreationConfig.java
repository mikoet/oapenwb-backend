// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.sheet.lexemecreator;

import dk.ule.oapenwb.entity.content.basedata.Language;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreationConfig
{
	private int lineNo;
	private Language language;
	private String tagName;
	private String pos;
}
