// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import java.time.Duration;
import java.time.Instant;

/**
 * <p>Utility class to perform time messeasurements per thread (via {@link ThreadLocal}).
 * First call {@link #startTimeMeasure()} and then call either {@link #durationInMilis()} or
 * {@link #durationInNanos()} to receive the duration since the call of the start method.</p>
 */
public class TimeUtil
{
	private static final ThreadLocal<Instant> TIME_STORE = new ThreadLocal<>();

	public static void startTimeMeasure() {
		TIME_STORE.set(Instant.now());
	}

	public static long durationInMilis() {
		Instant end = Instant.now();
		return Duration.between(TIME_STORE.get(), end).toMillis();
	}

	public static long durationInNanos() {
		Instant end = Instant.now();
		return Duration.between(TIME_STORE.get(), end).toNanos();
	}
}