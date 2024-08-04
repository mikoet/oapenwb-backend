// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.messages;

import dk.ule.oapenwb.util.io.Logger;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * <p>An instance of this class can gather messages organized by a context. It can print all messages to
 * a {@link Logger}.</p>
 */
public class MessageContainer
{
	private LinkedHashMap<String, List<Message>> messages = new LinkedHashMap<>();

	public Message add(String context, MessageType type, String text, int lineNumber, int colNumber)
	{
		Message message = new Message(type, text, lineNumber, colNumber);
		messages.computeIfAbsent(context, k -> new LinkedList<>()).add(message);
		return message;
	}

	public Message add(String context, MessageType type, String text, int lineNumber)
	{
		return add(context, type, text, lineNumber, -1);
	}

	public void printToLogger(Logger logger, MessageType minType)
	{
		for (Map.Entry<String, List<Message>> entry : messages.entrySet()) {
			String context = entry.getKey();
			List<Message> messageList = entry.getValue();

			// Log messages of current contextual section
			logger.log("Context '%s':", context);
			for (Message msg : messageList) {
				if (msg.getType().getWeight() >= minType.getWeight())
				logger.log(msg.toString());
			}
			// Add an empty line after each section
			logger.log("");
		}
	}

	public void reset()
	{
		messages = new LinkedHashMap<>();
	}
}
