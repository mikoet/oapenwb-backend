// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv.data;

import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * <p>Each instance of ProviderData contains the LexemeDetailedDTOs provided by the LexemeProviders
 * and MultiLexemeProviders for each RowData instance.</p>
 * <p>Instances of these data objects are created and filled in the second step.</p>
 */
@Data
@AllArgsConstructor
public class ProviderData
{
	private RowData rowData;
	private Map<String, LexemeDetailedDTO> providerResults;
	private Map<String, List<LexemeDetailedDTO>> multiProviderResults;
}
