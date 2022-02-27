// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportConfig
{
	//@JsonView(Views.REST.class)
	@Getter
	private String locale;
	@Getter
	private String filename;
	@Getter
	private String tagName;
	@Getter
	private double minFrequencyPercentage;
	@Getter
	private boolean simulate;
	@Getter
	private Optional<Integer> transactionSkipCount = Optional.empty();
}