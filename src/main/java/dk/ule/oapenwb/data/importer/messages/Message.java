// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.messages;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>A message that can occur during a file based import process and which can be added to
 * a {@link MessageContainer}.</p>
 */
@AllArgsConstructor
public class Message
{
	@Getter
	private MessageType type;

	@Getter
	private String text;

	@Getter
	private int lineNumber;

	@Getter
	private int columnNumber;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		switch (type) {
			case Info -> sb.append("INFO ");
			case Warning -> sb.append("WARN ");
			case Error -> sb.append("ERROR ");
		}
		// Print the line number only when it's a positive index
		if (lineNumber > 0) {
			sb.append(lineNumber);
			// Print the column number only when it's a positive index and the line# was printed
			if (columnNumber > 0) {
				sb.append(':');
				sb.append(columnNumber);
			}
		}
		sb.append(' ');
		sb.append(text);
		return sb.toString();
	}
}
