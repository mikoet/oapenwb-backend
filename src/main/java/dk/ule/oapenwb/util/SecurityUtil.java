// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * TODO COMMENT
 */
public class SecurityUtil
{
	public static final String SALT_CHARACTERS = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789!$%&()[]{}@#+-*/";

	public static char[] createSalt() {
		return SecurityUtil.createRandomString(20, SALT_CHARACTERS).toCharArray();
	}

	public static String createRandomString(int length, final String inputCharacters) {
		StringBuilder builder = new StringBuilder();
		while (length-- != 0) {
			int character = (int) (Math.random() * inputCharacters.length());
			builder.append(inputCharacters.charAt(character));
		}
		return builder.toString();
	}

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

	public static byte[] hashPassword(final String password, final char[] salt) {
		return hashString(password + String.valueOf(salt));
	}
}
