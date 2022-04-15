// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.users;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.basis.RegistryToken;
import dk.ule.oapenwb.entity.basis.User;
import dk.ule.oapenwb.entity.basis.ViolationType;
import dk.ule.oapenwb.util.EmailUtil;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.SecurityUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Arrays;

/**
 * <p>The UserController handles the registration of new users, the login and the logout of a user, as well as
 * the forgot-password functionality.</p>
 */
@Singleton
public class UserController
{
	private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

	private final boolean sendEmails;
	private final ViolationController violations;

	@Inject
	public UserController(AppConfig appConfig, ViolationController violations)
	{
		this.sendEmails = appConfig.isSendEmails();
		this.violations = violations;
	}

	public LoginUser createDirectUser(RegisterObject ro, String ipAddr) throws CodeException
	{
		// Verify the data
		final String preparedEmail = EmailUtil.convertEmailToAscii(ro.getEmail());
		if (!EmailUtil.verifyEmail(preparedEmail)) {
			throw new CodeException(ErrorCode.Register_EmailInvalid);
		}
		String preparedPassword = ro.getPassword().trim();
		if (!preparedPassword.equals(ro.getPassword()) || preparedPassword.length() < 10) {
			throw new CodeException(ErrorCode.Register_PasswordInvalid);
		}
		String preparedName = ro.getUsername().trim();
		if (preparedName != null && preparedName.isEmpty()) {
			preparedName = null;
		}
		// Username can be empty.
		/*if (preparedName.isEmpty()) {
			throw new CodeException(ErrorCode.Register_UsernameMissing);
		}*/
		String preparedFirstname = ro.getFirstname();
		String preparedLastname = ro.getLastname();

		if (preparedFirstname == null || preparedFirstname.trim().isEmpty()) {
			preparedFirstname = null;
		}
		if (preparedLastname == null || preparedLastname.trim().isEmpty()) {
			preparedLastname = null;
		}

		// Verify the token
		String token = ro.getToken();
		if (token == null || token.isEmpty()) {
			throw new CodeException(ErrorCode.Register_TokenIsEmpty);
		}
		RegistryToken registryToken = getRegistryToken(token, preparedEmail);
		if (registryToken == null || !registryToken.getToken().equals(token) || registryToken.isUsed()) {
			this.violations.createAndHandleViolations(ViolationType.Registration, ipAddr, "Token: " + token);
			throw new CodeException(ErrorCode.Register_TokenInvalid);
		}

		// TODO Do a blacklist check for the username, firstname and lastname?

		User newUser;
		synchronized (emailLock) {
			newUser = findUserByEmail(preparedEmail);
			if (newUser != null) {
				throw new CodeException(ErrorCode.Register_EmailExists);
			}

			char[] salt = SecurityUtil.createSalt();
			byte[] hashedPw = SecurityUtil.hashPassword(preparedPassword, salt);

			// Create a new user
			newUser = new User(preparedEmail, preparedName, hashedPw, salt, preparedFirstname, preparedLastname);

			// Set the token to be used
			registryToken.setUsed(true);

			Session session = HibernateUtil.getSession();
			Transaction t = session.beginTransaction();
			session.persist(newUser);
			session.update(registryToken);
			t.commit();
		}

		// TODO Make a concept for mail localization
		if (sendEmails) {
			String person = "";
			if (preparedFirstname != null && !preparedFirstname.isEmpty()) {
				person = " " + preparedFirstname;
			} else if (preparedName != null && !preparedName.isEmpty()) {
				person = " " + preparedName;
			}
			EmailUtil.sendEmail(preparedEmail, "ULE.DK – Welkoamen",
				String.format(
					"Moin%s, dyn konto is jüst anlegt warden. Nu skalst du töyven bet en admin dyn konto aktiv setten deit un du dy inloggen kanst.",
					person));
		}

		return createLoginUser(newUser, 0);
	}

	private RegistryToken getRegistryToken(String token, String email)
	{
		Instant now = Instant.now();
		Session session = HibernateUtil.getSession();
		Query<RegistryToken> qToken = session.createQuery(
			"FROM RegistryToken T WHERE token = :token AND (email = :email OR email IS NULL) AND validUntil >= :now AND used = false",
			RegistryToken.class);
		qToken.setParameter("token", token.trim());
		qToken.setParameter("email", email);
		qToken.setParameter("now", now);
		RegistryToken registryToken = HibernateUtil.getSingleResult(qToken);
		return registryToken;
	}

	public LoginUser login(LoginObject lo, final String ipAddr) throws CodeException
	{
		// 1 Make basic checks
		if (lo == null) {
			throw new CodeException(ErrorCode.Login_NoCredentials);
		}
		String identifier = lo.getIdentifier();
		String password = lo.getPassword();

		if (identifier == null || identifier.isBlank()) {
			throw new CodeException(ErrorCode.Login_IdentifierBlank);
		}
		if (password == null || password.isBlank()) {
			throw new CodeException(ErrorCode.Login_PasswordBlank);
		}
		// 2 Try loading the user by username or email address
		Session session = HibernateUtil.getSession();
		User user = null;

		if (EmailUtil.isEmailAddress(identifier)) {
			// 2a If the identifier looks like an email address, try loading the user by the email
			user = UserController.findUserByEmail(identifier);
		}

		if (user == null) {
			// 2b If loading by the email address did not work, try loading by the username
			user = UserController.findUserByUsername(identifier);
		}

		if (user == null) {
			// 2c No user was found on both ways -> user does not exist
			this.violations.createAndHandleViolations(ViolationType.Login, ipAddr,
				"Identifier length: " + identifier.length() + ", value: " + identifier);
			throw new CodeException(ErrorCode.Login_NoUserFound);
		}

		// 3 Check the password
		byte[] hash = SecurityUtil.hashPassword(password, user.getSalt());
		if (!Arrays.equals(hash, user.getPwHash())) {
			Transaction t = session.beginTransaction();
			user.setFailedLogins(user.getFailedLogins() + 1);
			session.save(user);
			t.commit();

			if (user.getFailedLogins() >= 3) {
				this.violations.createAndHandleViolations(ViolationType.Password, ipAddr,
					"user ID: " + user.getId());
			}
			throw new CodeException(ErrorCode.Login_WrongCredentials);
		}

		// Update data on user for successful login
		int failedLogins = user.getFailedLogins();
		Transaction t = session.beginTransaction();
		user.setFailedLogins(0);
		user.setLastActiveTS(Instant.now());
		session.save(user);
		t.commit();

		return createLoginUser(user, failedLogins);
	}

	public void logout()
	{
	}

	public void forgotPassword()
	{
		// TODO
	}

	private static User findUserByEmail(final String email) {
		Session session = HibernateUtil.getSession();
		Query<User> qUser = session.createQuery(
				"FROM User U WHERE type = :type AND email = :email AND activated = true",
				User.class);
		qUser.setParameter("type", User.TYPE_DIRECT);
		qUser.setParameter("email", email);
		return HibernateUtil.getSingleResult(qUser);
	}

	private static User findUserByUsername(final String username) {
		Session session = HibernateUtil.getSession();
		Query<User> qUser = session.createQuery(
				"FROM User U WHERE type = :type AND username = :uname AND activated = true",
				User.class);
		qUser.setParameter("type", User.TYPE_DIRECT);
		qUser.setParameter("uname", username);
		return HibernateUtil.getSingleResult(qUser);
	}

	private LoginUser createLoginUser(final User user, int failedLogins) {
		final LoginToken token = new LoginToken();
		token.setId(user.getId());
		token.setRole(user.getRole());

		final LoginUser loginUser = new LoginUser();
		loginUser.setUsername(user.getUsername());
		loginUser.setFirstname(user.getUsername());
		loginUser.setLastname(user.getUsername());
		loginUser.setFailedLogins(failedLogins);
		loginUser.setToken(token);

		return loginUser;
	}

	private final Object emailLock = new Object();
}