// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * <p>Simple logging utility to write text output to a specific file. Thus it can be helpful to log
 * details of a process into a separate file from application logging.</p>
 */
public final class Logger
{
	private PrintWriter writer;

	public Logger(String path) throws IOException {
		writer = new PrintWriter(new BufferedWriter(new FileWriter(path)));
	}

	@Override
	@SuppressWarnings("deprecation")
	protected void finalize() {
		close();
	}

	public void log(String line, Object... args) {
		if (writer != null) {
			writer.printf(line, args);
			writer.print('\n');
		} else {
			throw new RuntimeException("Writer was closed");
		}
	}

	public void close() {
		if (writer != null) {
			writer.close();
			writer = null;
		}
	}
}
