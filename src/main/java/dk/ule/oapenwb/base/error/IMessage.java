// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base.error;

import dk.ule.oapenwb.util.Pair;

import java.util.List;

/**
 * <p>Interface for an app's error message that contains a code number, a message string and
 * optional arguments.<br>
 * The arguments consist of a pair, 1st containing an identifier for the argument, and 2nd containing the argument's
 * value.<br>
 * The message string must contain a placeholder for an argument to be used.</p>
 * <p>Example format string: My exception message tells you about an {{entity}}.</p>
 */
public interface IMessage
{
	/**
	 * @return A unique error code among all errors thrown within the application.
	 */
	int getCode();

	/**
	 * @return The text of the message which may contain placeholders for the arguments.
	 */
	String getMessage();

	/**
	 * @return A list of pairs each to consist of an identifier (to be used in the message text) and the actuall
	 * value of the argument.
	 */
	List<Pair<String, Object>> getArguments();
}