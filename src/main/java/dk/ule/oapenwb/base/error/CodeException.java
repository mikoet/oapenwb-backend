// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base.error;

import dk.ule.oapenwb.util.Pair;
import lombok.Getter;

import java.util.List;

/**
 * <p>The CodeException is the app specific exception class that contains a code number, a message string and
 * optional arguments. For more information on the three parts see {@link IMessage}.</p>
 */
public class CodeException extends Exception implements IMessage {
	@Getter
	private final int code;

	@Getter
	private final String message;

	@Getter
	private final List<Pair<String, Object>> arguments;

	public CodeException(IMessage er) {
		this.code = er.getCode();
		this.message = er.getMessage();
		this.arguments = er.getArguments();
	}

	public CodeException(IMessage er, List<Pair<String, Object>> arguments) {
		this.code = er.getCode();
		this.message = er.getMessage();
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		return "CodeException{" +
			"code=" + code +
			", message='" + message + '\'' +
			", arguments=" + arguments +
			'}';
	}
}