// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.util;

import dk.ule.oapenwb.data.UserRevisionListener;
import jakarta.persistence.Table;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * HibernateUtil is a mingle-mangle of things useful in the work with Hibernate on PostgreSQL. The method
 * {@link #initialize(HibernateConfigurator)} has to be called on application startup.
 */
public class HibernateUtil
{
	/**
	 * In Hibernate queries the type hint (e.g. ::string in PostgreSQL) cannot be simply used in queries as
	 * a colon is a special character for Hibernate. This constant includes propper escaping
	 * for ::jsonb type hint. It also contains a trailing space to be on the safe side in use.
	 */
	public static final String CONSTANT_JSONB = "\\:\\:jsonb ";
	public static final String CONSTANT_INT = "\\:\\:int ";

	private static final Logger LOGGER = LoggerFactory.getLogger(HibernateUtil.class);

	private static SessionFactory SESSION_FACTORY;
	private static final ThreadLocal<Session> SESSIONS = new ThreadLocal<>();
	private static final ThreadLocal<String> REVISION_COMMENTS = new ThreadLocal<>();

	// Temporary here for as long as mass inserts of lexemes are slow due to reflection.
	// Lets disable the JSON ID checks for the thread a big import job is run on.
	private static final ThreadLocal<Boolean> DISABLE_JSON_ID_CHECKS = new ThreadLocal<>();

	public static Session getSession() {
		Session session = SESSIONS.get();
		if (session == null) {
			session = SESSION_FACTORY.openSession();
			SESSIONS.set(session);
			REVISION_COMMENTS.set(null);
			DISABLE_JSON_ID_CHECKS.set(false);
		}
		return session;
	}

	public static void closeSession() {
		Session session = SESSIONS.get();
		if (session != null) {
			session.close();
			SESSIONS.remove();
			REVISION_COMMENTS.remove();
			DISABLE_JSON_ID_CHECKS.remove();
		}
	}

	/**
	 * @return A revision comment to be used by the {@link UserRevisionListener}.
	 */
	public static String getRevisionComment() {
		return REVISION_COMMENTS.get();
	}

	public static void setRevisionComment(final String comment) {
		REVISION_COMMENTS.set(comment);
	}

	public static boolean isDisableJsonIdChecks()
	{
		return DISABLE_JSON_ID_CHECKS.get();
	}

	public static void setDisableJsonIdChecks(boolean disable)
	{
		DISABLE_JSON_ID_CHECKS.set(disable);
	}

	public static void initialize(HibernateConfigurator configurator)
	{
		if (configurator == null) {
			throw new ExceptionInInitializerError("No HibernateConfigurator was set for HibernateUtil");
		}
		SESSION_FACTORY = createSessionFactory(configurator, false);
	}

	/**
	 * Helper method to avoid an unchecked warning when using {@link org.hibernate.query.NativeQuery}s
	 * (as in NativeQuery<?> and the resulting list of type List<Object[]>).
	 *
	 * @param q The Hibernate query
	 * @param <T> The type of the query object
	 * @return List containing the query's result.
	 */
	public static <T> List<T> listAndCast(Query q)
	{
		@SuppressWarnings("unchecked")
		List list = q.list();
		return list;
	}

	/**
	 * @param query
	 * @param <T>
	 * @return The first result of a query, or null if the query's result is empty.
	 */
	public static <T> T getSingleResult(Query<T> query) {
		query.setMaxResults(1);
		List<T> list = query.getResultList();
		if (list == null || list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}

	/**
	 * @param clazz Class instance that is an {@link jakarta.persistence.Entity}.
	 * @return The table name from the {@link Table} annotation if present. If not the class' simple name will be returned.
	 */
	public static String getTableName(Class clazz)
	{
		Table table = (Table) clazz.getAnnotation(Table.class);
		if (table != null) {
			return table.name();
		} else {
			return clazz.getSimpleName();
		}
	}

	public static SessionFactory createMinimalSessionFactory(HibernateConfigurator configurator)
	{
		if (configurator == null) {
			throw new ExceptionInInitializerError("No HibernateConfigurator was set for HibernateUtil");
		}
		return createSessionFactory(configurator, true);
	}

	private static SessionFactory createSessionFactory(HibernateConfigurator configurator, boolean minimal)
	{
		try {
			final String configFile = configurator.getHibernateConfigFile();
			Configuration configuration = configuration(configFile);
			configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "thread");

			if (minimal) {
				configurator.configurateMinimal(configuration);
			} else {
				configurator.configurate(configuration);
			}

			StandardServiceRegistryBuilder serviceRegistryBuilder =
				new StandardServiceRegistryBuilder().applySettings(configuration.getProperties());
			return configuration.buildSessionFactory(serviceRegistryBuilder.configure(configFile).build());
		} catch (RuntimeException ex) {
			LOGGER.error("Failed to create session factory", ex);
			throw new ExceptionInInitializerError(ex);
		}
	}

	private static Configuration configuration(final String hibernateConfigFile)
	{
		Configuration configuration = new Configuration();
		configuration.configure(hibernateConfigFile);
		return configuration;
	}
}