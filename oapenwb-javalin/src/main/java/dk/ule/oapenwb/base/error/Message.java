// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base.error;

import dk.ule.oapenwb.util.Pair;
import lombok.Data;

import java.util.List;

/**
 * <p>Concrete implementation of the interface {@link IMessage} as a non-exception class.</p>
 */
@Data
public class Message implements IMessage
{
	public static String toString(final IMessage message) {
		String str = message.getPlaceholderMessage();
		if (message.getArguments() != null) {
			for (var argument : message.getArguments()) {
				str = str.replace(
					String.format("{{%s}}", argument.getLeft()),
					argument.getRight() == null ? "null" : argument.getRight().toString()
				);
			}
		}
		return str;
	}

	private final int code;
	private final String placeholderMessage;
	private final List<Pair<String, Object>> arguments;

	public Message(IMessage er) {
		this.code = er.getCode();
		this.placeholderMessage = er.getPlaceholderMessage();
		this.arguments = er.getArguments();
	}

	public Message(IMessage er, List<Pair<String, Object>> arguments) {
		this.code = er.getCode();
		this.placeholderMessage = er.getPlaceholderMessage();
		this.arguments = arguments;
	}

	public Message(int code, String placeholderMessage, List<Pair<String, Object>> arguments) {
		this.code = code;
		this.placeholderMessage = placeholderMessage;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return String.format("Message {code=%d, message='%s'}", this.code, Message.toString(this));
	}
}
