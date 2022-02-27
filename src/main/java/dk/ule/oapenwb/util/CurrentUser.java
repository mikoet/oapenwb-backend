// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

/**
 * <p>This class will keep track of the logged in users for each 'query'. With the used web framework called Javalin
 * the webservice is running with a defined (and configurable) number of threads, and each thread will only execute
 * one client request at a time.</p>
 * <p>Therefore, at the beginning of each user specific request the method {@link #logIn(Integer)} will be called
 * with the ID of the requesting user. {@link #logOut()} will be called when the request is over/finishing.<br>
 * There are also requests where the user does not matter and will likely not be authenticated (set to null).</p>
 */
public class CurrentUser
{
	public static final CurrentUser INSTANCE = new CurrentUser();

	private static final ThreadLocal<Integer> storage = new ThreadLocal<>();

	public void logIn(Integer userID) {
		storage.set(userID);
	}

	public void logOut() {
		storage.remove();
	}

	/**
	 * @return The ID of the user currently logged in.
	 */
	public Integer get() {
		return storage.get();
	}
}