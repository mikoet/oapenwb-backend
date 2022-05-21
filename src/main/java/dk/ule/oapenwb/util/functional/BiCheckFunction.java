// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util.functional;

import dk.ule.oapenwb.base.error.CodeException;

/**
 * <p>Can perform an operation depending on two parameters. The operation should throw a
 * {@link CodeException} on fail.</p>
 *
 * @param <T> type of param one
 * @param <R> type of param two
 */
@FunctionalInterface
public interface BiCheckFunction<T, R> {
	void perform(T t, R r) throws CodeException;
}
