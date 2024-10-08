// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base;

import com.google.inject.Singleton;
import dk.ule.oapenwb.util.EmailConfig;
import lombok.Getter;

/**
 * <p>Configuration object for the dictionary that will be loaded from a JSON file.</p>
 */
@Singleton
public class AppConfig
{
	@Getter
	private int port = 60_100;

	@Getter
	private String[] allowedOrigins = {"*"};

	@Getter
	private int minThreads = 8;

	@Getter
	private int maxThreads = 32;

	@Getter
	private int timeOutMillis = 60_000;

	@Getter
	private String secret = null;

	@Getter
	private boolean verbose = false;

	@Getter
	private boolean logSearchRuns = false;

	/**
	 * Shall emails be sent when a user registers, for notifications, etc.?
	 */
	@Getter
	private boolean sendEmails = false;

	@Getter
	private EmailConfig emailConfig = new EmailConfig();

	@Getter
	private DbConfig dbConfig = new DbConfig();

	@Getter
	private ImportConfig importConfig = new ImportConfig();

	@Getter
	private String rpcHost = "localhost";

	@Getter
	private int rpcHostPort = 60_300;

	public static class DbConfig
	{
		@Getter
		private String hostname;

		@Getter
		private int port;

		@Getter
		private String database;

		@Getter
		private String username;

		@Getter
		private String password;

		@Getter
		private boolean showSQL;

		/**
		 * The minimal number of idle connections in the pool. Ideally it is the same as minThreads.
		 */
		@Getter
		private int minPoolSize = 8;

		/**
		 * The maximum size of the pool. Ideally it is the same as maxThreads.
		 */
		@Getter
		private int maxPoolSize = 32;

		/**
		 * Number of miliseconds for a connection to be in idle mode before being closed (only if minimumIdle is not
		 * reached)
		 */
		@Getter
		private int poolIdleTimeout = 120_000;

		/**
		 * The maximum number of miliseconds to wait for a connection from the pool. Else a SQLException will be thrown.
		 */
		@Getter
		private int poolConnectionTimeout = 20_000;
	}

	public static class ImportConfig
	{
		@Getter
		private String inputDir;

		@Getter
		private String outputDir;
	}
}