// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util.json;

import dk.ule.oapenwb.base.error.IMessage;
import dk.ule.oapenwb.base.error.Message;
import lombok.Data;

/**
 * <p>Response for most of the backend calls.</p>
 */
@Data
public class Response
{
	private ResponseStatus status = ResponseStatus.Success;

	private Object data = null;

	private IMessage message = null;

	public void setMessage(IMessage msg)
	{
		if (msg instanceof Throwable) {
			this.message = new Message(msg);
		} else {
			this.message = msg;
		}
	}
}