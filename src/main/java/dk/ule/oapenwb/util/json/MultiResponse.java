// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util.json;

import dk.ule.oapenwb.base.error.IMessage;
import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
@Data
public class MultiResponse
{
	private ResponseStatus status = ResponseStatus.Success;

	private Object data = null;

	private List<IMessage> messages = new LinkedList<>();
}