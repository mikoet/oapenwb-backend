// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base.error;

import lombok.Getter;

import java.util.List;

/**
 * The MultiCodeException is similar to the {@link CodeException} in that it transfers an error message, but different in
 * that it doesn't contains single message but a list of messages.
 */
public class MultiCodeException extends Exception
{
	@Getter
	private final List<IMessage> errors;

	public MultiCodeException(List<IMessage> errors) {
		this.errors = errors;
	}

	@Override
	public String toString() {
		return "MultiCodeException{" + "errors.size=" + errors.size() + '}';
	}
}