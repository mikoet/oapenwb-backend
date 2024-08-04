// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.messages;

import lombok.Getter;

/**
 * <p>Type of a message put into the {@link MessageContainer}.</p>
 */
public enum MessageType
{
	Debug(1),
	Info(2),
	Warning(3),
	Error(4);

	@Getter
	private final int weight;

	MessageType(int weight) {
		this.weight = weight;
	}
}
