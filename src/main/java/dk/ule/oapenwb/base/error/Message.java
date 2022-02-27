// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base.error;

import dk.ule.oapenwb.util.Pair;
import lombok.Data;

import java.util.List;

/**
 * Concrete implementation of the interface {@link IMessage} as a non-exception class.
 */
@Data
public class Message implements IMessage
{
	private final int code;
	private final String message;
	private final List<Pair<String, Object>> arguments;

	public Message(IMessage er) {
		this.code = er.getCode();
		this.message = er.getMessage();
		this.arguments = er.getArguments();
	}

	public Message(IMessage er, List<Pair<String, Object>> arguments) {
		this.code = er.getCode();
		this.message = er.getMessage();
		this.arguments = arguments;
	}

	public Message(int code, String message, List<Pair<String, Object>> arguments) {
		this.code = code;
		this.message = message;
		this.arguments = arguments;
	}
}