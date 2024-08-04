// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * SMTP configuration for mailing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class EmailConfig
{
	private String smtpHostname;
	private int smtpPort;
	private String smtpUsername;
	private String smtpPassword;
	private String from;
	private boolean useSSL;
	private boolean useAuthentication;
}