// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <p>Utility class that provides some helpers that are more or less security related.</p>>
 */
public class SecurityUtil
{
	/**
	 * Contains all letters from A to Z in lower- and uppercase as well as numbers 0 to 9.
	 */
	public static final String ALPHABET_AND_NUMBERS = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789";

	/**
	 * Contains all characters that {@link SecurityUtil#ALPHABET_AND_NUMBERS} contains plus some special characters.
	 */
	public static final String SALT_CHARACTERS = ALPHABET_AND_NUMBERS + "!$%&()[]{}@#+-*/";

	/**
	 * Creates a salt for password encryption.
	 *
	 * @return the salt as an array of characters.
	 */
	public static char[] createSalt() {
		return SecurityUtil.createRandomString(20, SALT_CHARACTERS).toCharArray();
	}

	/**
	 * Creates a random string by the given length and input characters.
	 *
	 * @param length Length the random string shall have
	 * @param inputCharacters Characters that shall be used for creation of the string
	 * @return the random string
	 */
	public static String createRandomString(int length, final String inputCharacters) {
		StringBuilder builder = new StringBuilder();
		while (length-- != 0) {
			int character = (int) (Math.random() * inputCharacters.length());
			builder.append(inputCharacters.charAt(character));
		}
		return builder.toString();
	}

	/**
	 * Creates a SHA-384 hash of the given string <i>input</i>.
	 *
	 * @param input the given string
	 * @return a byte array containing the hash
	 */
	public static byte[] hashString(final String input) {
		byte[] encodedhash = null;
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-384");
			encodedhash = digest.digest(
					input.getBytes(StandardCharsets.UTF_8));
			digest.reset();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return encodedhash;
	}

	/**
	 * Hashes the given password with the given salt.
	 *
	 * @param password the password to hash
	 * @param salt the salt to use
	 * @return a byte array containing the hash
	 */
	public static byte[] hashPassword(final String password, final char[] salt) {
		return hashString(password + String.valueOf(salt));
	}
}
