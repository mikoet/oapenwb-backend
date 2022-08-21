// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

import lombok.Getter;

public class ContextedRuntimeException extends RuntimeException
{
	@Getter
	private final String context;

	public ContextedRuntimeException(String context, String message) {
		super(message);
		this.context = context;
	}
}