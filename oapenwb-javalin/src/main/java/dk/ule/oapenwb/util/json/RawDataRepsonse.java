// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import dk.ule.oapenwb.base.error.IMessage;
import lombok.Data;

/**
 * @author Michael Köther
 */
@Data
public class RawDataRepsonse {
	ResponseStatus status = ResponseStatus.Success;

	@JsonRawValue
	String data = null;

	IMessage message = null;
}