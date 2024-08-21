// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.ule.oapenwb.Dict;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.RunMode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.basis.RoleType;
import dk.ule.oapenwb.entity.basis.User;
import dk.ule.oapenwb.entity.basis.VersionInfo;
import dk.ule.oapenwb.persistency.entity.ui.UiLanguage;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslation;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslationScope;
import dk.ule.oapenwb.util.HibernateConfigurator;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.SecurityUtil;
import dk.ule.oapenwb.util.io.FilesProcessor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.Map;

/**
 * <p>The DataInitializer will perform two checks: 1) Is the configured database containing a setup that is compatible
 * to this version of the dictionary backend? If not, an exception will be thrown. And this class will check if 2) the
 * database is still empty and if so, fill the database with some initial data.</p>
 * <p>In order to use this class an instance shall first be created and run be called on it.</p>
 */
public class DataInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

	private RunMode runMode;
	private AppConfig appConfig;
	private boolean createData = false;
	private boolean versionMismatch = false;

	public DataInitializer(RunMode runMode, AppConfig appConfig)
	{
		this.runMode = runMode;
		this.appConfig = appConfig;
	}

	public void run() throws CodeException
	{
		HibernateConfigurator configurator = new HibernateConfiguratorImpl(runMode, appConfig);

		// Check if table VersionInfo exists and if so check for the version of the persistent data.
		performCheckForVersionInfo(configurator);

		// Initialize Hibernate
		HibernateUtil.initialize(configurator);

		if (this.createData) {
			createData(HibernateUtil.getSession());
		}
		HibernateUtil.closeSession();
	}

	@SafeVarargs
	static void createUiTranslations(Session session, String scope, String uitID, boolean essential, Pair<String,
				String>...translations)
	{
		for (Pair<String, String> translation : translations) {
			session.save(new UiTranslation(uitID, translation.getLeft(), scope, translation.getRight(), essential));
		}
	}

	private void performCheckForVersionInfo(HibernateConfigurator configurator) throws CodeException
	{
		// TODO Schema creation or running update script could be run here via JDBC before Hibernate is initialized
		// try... catch ... finally { session.close(); factory.close(); }
		SessionFactory factory = null;
		Session session = null;
		try
		{
			factory = HibernateUtil.createMinimalSessionFactory(configurator);
			session = factory.openSession();

			session.doWork(connection -> {
				try
				{
					Statement statement = connection.createStatement();
					ResultSet resultSet = statement.executeQuery("SELECT version FROM VersionInfos ORDER BY actionTS DESC");
					if (resultSet.isBeforeFirst()) {
						resultSet.next();
						String version = resultSet.getString(1);
						if (!Dict.VERSION.equals(version)) {
							// Throw an exception when there is a version mismatch between the database content and the app version
							this.versionMismatch = true;
						}
					} else {
						// Table exists but is empty? We assume an admin has just created the tables by him-/herself.
						this.createData = true;
					}
				} catch (SQLException e) {
					// This catch is for PostgreSQL
					if ("42P01".equals(e.getSQLState())) {
						// Tabelle nicht vorhanden -> Anlegen aller Tabellen
						configurator.setCreateTables(true);
					} else {
						throw e;
					}
				}
			});

			if (this.versionMismatch) {
				throw new CodeException(ErrorCode.General_VersionMismatch);
			}
			LOG.info("Version of database model equals Dict version. Create data? " + this.createData);
		} catch (SQLGrammarException e) {
			// This catch is for MySQL
			if (e.getErrorCode() == 1146) {
				// Tabelle nicht vorhanden -> Anlegen aller Tabellen
				configurator.setCreateTables(true);
			} else {
				throw e;
			}
		} catch (CodeException e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if (session != null) {
				session.close();
			}
			if (factory != null) {
				factory.close();
			}
		}
	}

	private void createData(Session session) {
		HibernateUtil.setRevisionComment("Initial data");
		Transaction transaction = null;
		boolean commit = false;
		try {
			transaction = session.beginTransaction();

			createVersionInfo(session);
			createUsers(session);
			createUiData(session);

			// For now it will always initialize the dictionary with the Saxon base data
			DataStrategy strategy = new SaxonDictData();
			strategy.createData(session);

			commit = true;
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			throw e;
		} finally {
			if (commit) {
				transaction.commit();
			} else {
				transaction.rollback();
			}
			HibernateUtil.closeSession();
		}
	}

	private void createVersionInfo(Session session) {
		session.save(new VersionInfo(Dict.VERSION, Instant.now()));
	}

	private void createUsers(Session session) {
		char[] salt = SecurityUtil.createSalt();
		byte[] hashedPw = SecurityUtil.hashPassword("admin", salt);
		User admin = new User("admin@admin.admin", "admin", hashedPw, salt, "Ulla", "Mustervrouw");
		admin.setActivated(true);
		admin.setRole(RoleType.Admin);
		session.save(admin);

		salt = SecurityUtil.createSalt();
		hashedPw = SecurityUtil.hashPassword("test", salt);
		User test = new User("test@test.test", "test", hashedPw, salt, "Max", "Musterman");
		test.setActivated(true);
		session.save(test);
	}

	/**
	 * Creates the UiLanguages and UiTranslations
	 */
	private void createUiData(Session session) {
		// Create the default UiLanguages
		UiLanguage english = new UiLanguage("en", null, "English", true, true);
		UiLanguage german = new UiLanguage("de", null, "Deutsch", false, true);
		UiLanguage lowSaxon = new UiLanguage("nds", null, "Sassisk", false, true);

		session.save(english);
		session.save(german);
		session.save(lowSaxon);

		// Create the default scopes
		session.save(new UiTranslationScope("", null, "Default scope", true));
		session.save(new UiTranslationScope("son", null, "Everything that belongs to Sign-On module", true));
		session.save(new UiTranslationScope("admin", null, "Everything that belongs to the Admin Center", true));
		session.save(new UiTranslationScope("abbr", null, "Created to contain all abbreviated category, level and (dialect) / language names.", true));
		session.save(new UiTranslationScope("full", null, "Created to contain all non-abbreviated category, level, orthography and (dialect) / language names.", true));
		session.save(new UiTranslationScope("formType", null, "Created to contain all localised LexemeFormType names.", true));
		session.save(new UiTranslationScope("linkType", null, "Created to contain all localised LinkType data.", true));
		session.save(new UiTranslationScope("dftpa", null, "Dialog: Form Type Position Adjusting", true));
		session.save(new UiTranslationScope("dyn", null, "Dialog: Yes No questions", true));
		session.save(new UiTranslationScope("derr", null, "Dialog: Errors", true));
		session.save(new UiTranslationScope("metaTags", null, "Meta tags for SEO and Social Media", true));

		createUiTranslations(session, "dyn", "discardL:title", true,
			new Pair<>("nds", "Lekseem wegdoon"),
			new Pair<>("de", "Lexem verwerfen"),
			new Pair<>("en", "Discard lexeme"));
		createUiTranslations(session, "dyn", "discardL:content", true,
			new Pair<>("nds", "Dat lekseem wur ändered.\nBüst du seaker wat du de änderings wegdoon wult?"),
			new Pair<>("de", "Das Lexem wurde verändert.\nBist du sicher, dass du die Änderungen verwerfen willst?"),
			new Pair<>("en", "The lexeme has been changed.\nAre you sure you want to discard the changes?"));

		//session.save(new UiTranslationScope("admin_long", "Contains all (long text) translations for the Admin-Center", true));
		//session.save(new UiTranslationScope("error:long", "Contains all (long text) error messages", true));

		// Create the default translations
		// To extract the data from an existing MySQL database, use this statement:
		// select concat('session.save(new UiTranslation("', id, '", "', locale, '", "', text, '", ',
		//   if(base = 1, 'true', 'false'), '));') from UiTranslation order by id, locale;

		FilesProcessor i18nProcessor = new FilesProcessor("i18n", (String filePath, String content) -> {
			LOG.info("Reading UiTranslations from file " + filePath);
			// Remove the first directory from the path
			filePath = filePath.replace("i18n/", "");
			// Take the scope and locale
			String scope;
			String locale;
			int slashPos = filePath.indexOf("/");
			if (slashPos != -1) {
				scope = filePath.substring(0, slashPos);
				locale = filePath.substring(slashPos + 1, filePath.lastIndexOf("."));
			} else {
				scope = ""; // default scope
				locale = filePath.substring(0, filePath.lastIndexOf("."));
			}
			ObjectMapper mapper = new ObjectMapper();
			try {
				// Convert the JSON string to a map
				Map<String, String> map = mapper.readValue(content, Map.class);
				// Iterate over the map's entries
				int i = 0;
				for (Map.Entry<String, String> entry : map.entrySet()) {
					String key = entry.getKey();
					String text = entry.getValue();
					// Persist an UiTranslation instance
					session.save(new UiTranslation(key, locale, scope, text, true));
					i++;
				}
				LOG.info("  Imported " + i + " translations");
			} catch (IOException e) {
				LOG.error("Error", e);
			}
		});
		i18nProcessor.work();
	}
}