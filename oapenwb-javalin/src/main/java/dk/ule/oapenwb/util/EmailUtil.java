// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Utility class for checking, converting and sending e-mails. Before sending e-mails the method
 * {@link #configureEmail(EmailConfig)} has once to be called for setup.</p>
 */
public class EmailUtil
{
	private static final Logger LOG = LoggerFactory.getLogger(EmailUtil.class);

	public static boolean isEmailAddress(final String str) {
		return str != null && !str.isEmpty() && str.contains("@")
				&& !str.startsWith("@") && !str.endsWith("@");
	}

	public static String convertEmailToAscii(final String email) {
		if (email.chars().filter(ch -> ch == '@').count() == 1) {
			final String localPart = email.substring(0, email.indexOf('@'));
			String domain = email.substring(email.indexOf('@') + 1);
			// evtl. internationale Domain zu Puny Code Domain wandeln
			// Beispiele: "kontakt@michaelköther.de", "michael@www.例如.中国"
			domain = java.net.IDN.toASCII(domain);
			final String convertedEmail = localPart + '@' + domain;
			return convertedEmail;
		} else {
			return email;
		}
	}

	public static boolean verifyEmail(final String email) {
		return email.matches("\\b[a-zA-Z0-9.!#$%&’*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\\b");
		// TODO
		// DNS-Lookup für Domain wäre noch ein Plus
		// (QDnsLookup: http://doc.qt.io/qt-5/qdnslookup.html oder http://doc.qt.io/qt-5/qhostinfo.html)
	}

	public static void configureEmail(EmailConfig config) {
		emailConfig = config;
	}

	public static void sendEmail(String from, String to, String subject, String message) throws CodeException {
		if (emailConfig == null) {
			throw new CodeException(ErrorCode.Email_NotConfigured);
		}

		try {
			Email email = new SimpleEmail();
			email.setHostName(emailConfig.getSmtpHostname());
			email.setSmtpPort(emailConfig.getSmtpPort());
			if (emailConfig.isUseAuthentication()) {
				email.setAuthenticator(
					new DefaultAuthenticator(emailConfig.getSmtpUsername(), emailConfig.getSmtpPassword()));
			}
			if (emailConfig.isUseSSL()) {
				email.setSSLOnConnect(true);
			}
			if (emailConfig.isEnableStartTLS()) {
				email.setStartTLSEnabled(true);
			}
			email.setFrom(from != null ? from : emailConfig.getFrom());
			final String finalSubject = emailConfig.getSubjectPrefix() != null && !emailConfig.getSubjectPrefix().isEmpty()
				? String.format("%s – %s", emailConfig.getSubjectPrefix(), subject)
				: String.format("oapenwb – %s", subject);
			email.setSubject(finalSubject);
			email.setMsg(message);
			email.addTo(to);
			email.send();
		} catch (EmailException e) {
			LOG.error("Sending mail to " + to + " failed", e);
			throw new CodeException(ErrorCode.Email_SendFailed);
		}
	}

	public static void sendEmail(String to, String subject, String message) throws CodeException {
		EmailUtil.sendEmail(null, to, subject, message);
	}

	private static EmailConfig emailConfig = null;
}