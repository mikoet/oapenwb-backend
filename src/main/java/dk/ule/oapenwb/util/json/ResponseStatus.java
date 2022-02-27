// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util.json;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * <p>Represents the status of a JSON response message (see {@link Response} and {@link RawDataRepsonse}).</p>
 */
public enum ResponseStatus
{
	Success("success"),
	Fail("fail"),
	Error("error");

	@Getter
	@JsonValue
	private String value;

	ResponseStatus(String value) {
		this.value = value;
	}
}