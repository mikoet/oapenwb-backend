// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.sheet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SheetConfig
{
	@Getter
	private String filename;
	@Getter
	private boolean simulate;
	@Getter
	private String tagName;
	@Getter
	private Optional<Integer> transactionSkipCount = Optional.empty();
}