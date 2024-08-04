// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>A triple stores three objects – left, middle, and right – of up to three different types. This class is
 * an extended version of {@link Pair}.</p>
 *
 * @param <L> Type of left instance
 * @param <M> Type of middle instance
 * @param <R> Type of right instance
 */
@Data
@AllArgsConstructor
public class Triple<L, M, R>
{
	private L left;
	private M middle;
	private R right;

	@Override
	public String toString() {
		return "Triple{" +
			"left=" + left +
			", middle=" + middle +
			", right=" + right +
			'}';
	}
}