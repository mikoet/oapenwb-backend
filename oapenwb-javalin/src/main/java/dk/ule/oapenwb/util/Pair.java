// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>A pair stores two objects – left and right – of up to two different types. This class is similar
 * to {@link Triple}.</p>
 *
 * @param <L> Type of left instance
 * @param <R> Type of right instance
 */
@Data
@AllArgsConstructor
public class Pair<L, R>
{
	private L left;
	private R right;

	@Override
	public String toString() {
		return "Pair{" +
			"left=" + left +
			", right=" + right +
			'}';
	}
}