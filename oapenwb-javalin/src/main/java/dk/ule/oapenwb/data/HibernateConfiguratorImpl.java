// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data;

import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.RunMode;
import dk.ule.oapenwb.entity.basis.*;
import dk.ule.oapenwb.entity.statistics.CondensedData;
import dk.ule.oapenwb.entity.statistics.SearchRun;
import dk.ule.oapenwb.persistency.entity.content.basedata.*;
import dk.ule.oapenwb.persistency.entity.content.basedata.tlConfig.TypeLanguageConfig;
import dk.ule.oapenwb.persistency.entity.content.lexemes.*;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Lexeme;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Tag;
import dk.ule.oapenwb.persistency.entity.content.lexemes.lexeme.Variant;
import dk.ule.oapenwb.persistency.entity.ui.UiLanguage;
import dk.ule.oapenwb.persistency.entity.ui.UiResultCategory;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslation;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslationScope;
import dk.ule.oapenwb.util.HibernateConfigurator;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;

/**
 * Implementation of the {@link HibernateConfigurator} interface for this application.
 */
public class HibernateConfiguratorImpl implements HibernateConfigurator
{
	private RunMode runMode;
	private AppConfig appConfig;
	@Getter @Setter
	private boolean createTables = false;

	public HibernateConfiguratorImpl(RunMode runMode, AppConfig appConfig) {
		this.runMode = runMode;
		this.appConfig = appConfig;
	}

	@Override
	public String getHibernateConfigFile() {
		switch (this.runMode) {
			case Normal:
			default:
				return "/hibernate.prod.cfg.xml";

			case Development:
				return "/hibernate.dev.cfg.xml";

			case Testing:
				return "/hibernate.testing.cfg.xml";
		}
	}

	@Override
	public void configurateMinimal(Configuration configuration) {
		configuration.setProperty(AvailableSettings.USE_QUERY_CACHE, "false");
		configuration.setProperty(AvailableSettings.SHOW_SQL,
				this.appConfig.getDbConfig().isShowSQL() ? "true" : "false");
		configuration.setProperty(AvailableSettings.FORMAT_SQL,
				this.appConfig.getDbConfig().isShowSQL() ? "true" : "false");

		configuration.setProperty(AvailableSettings.URL, this.buildConnectionURL());
		configuration.setProperty(AvailableSettings.USER, this.appConfig.getDbConfig().getUsername());
		configuration.setProperty(AvailableSettings.PASS, this.appConfig.getDbConfig().getPassword());
	}

	@Override
	public void configurate(Configuration configuration) {
		configurateMinimal(configuration);

		if (this.isCreateTables()) {
			/* TODO Hibernate shall not anymore create the tables etc., it is done manually
			   by the initialization script for now. */
			//configuration.setProperty(AvailableSettings.HBM2DDL_DATABASE_ACTION, "create");
			//configuration.setProperty(AvailableSettings.HBM2DDL_AUTO, "update");
		}

		// Minimal number of idle connections in the pool
		configuration.setProperty("hibernate.hikari.minimumIdle",
				String.valueOf(this.appConfig.getDbConfig().getMinPoolSize()));
		// Maximum size of the pool
		configuration.setProperty("hibernate.hikari.maximumPoolSize",
				String.valueOf(this.appConfig.getDbConfig().getMaxPoolSize()));
		// Number of miliseconds for a connection to be in idle mode before being closed
		// (only if minimumIdle is not reached)
		configuration.setProperty("hibernate.hikari.idleTimeout", "120000");
		// Maximum number of miliseconds to wait for a connection from the pool. Else SQLException will be thrown.
		configuration.setProperty("hibernate.hikari.connectionTimeout", "20000");

		// Add the entities
		configuration.addAnnotatedClass(Ban.class);
		configuration.addAnnotatedClass(Permission.class);
		configuration.addAnnotatedClass(RegistryToken.class);
		configuration.addAnnotatedClass(User.class);
		configuration.addAnnotatedClass(UserRevisionEntity.class);
		configuration.addAnnotatedClass(VersionInfo.class);
		configuration.addAnnotatedClass(Violation.class);

		configuration.addAnnotatedClass(UiResultCategory.class);
		configuration.addAnnotatedClass(UiLanguage.class);
		configuration.addAnnotatedClass(UiTranslationScope.class);
		configuration.addAnnotatedClass(UiTranslation.class);

		configuration.addAnnotatedClass(CondensedData.class);
		configuration.addAnnotatedClass(SearchRun.class);

		configuration.addAnnotatedClass(Orthography.class);
		configuration.addAnnotatedClass(Language.class);
		configuration.addAnnotatedClass(LangOrthoMapping.class);
		configuration.addAnnotatedClass(LangPair.class);
		configuration.addAnnotatedClass(LexemeType.class);
		configuration.addAnnotatedClass(LexemeFormType.class);
		configuration.addAnnotatedClass(TypeLanguageConfig.class);
		configuration.addAnnotatedClass(LemmaTemplate.class);
		configuration.addAnnotatedClass(LinkType.class);
		configuration.addAnnotatedClass(Category.class);
		configuration.addAnnotatedClass(Level.class);

		configuration.addAnnotatedClass(Variant.class);
		configuration.addAnnotatedClass(Lexeme.class);
		configuration.addAnnotatedClass(Sememe.class);
		configuration.addAnnotatedClass(Tag.class);

		configuration.addAnnotatedClass(Audio.class);
		configuration.addAnnotatedClass(LexemeForm.class);
		configuration.addAnnotatedClass(Mapping.class);
		configuration.addAnnotatedClass(Link.class);
		configuration.addAnnotatedClass(MetaInfo.class);
		configuration.addAnnotatedClass(SynGroup.class);
		configuration.addAnnotatedClass(SynLink.class);
	}

	private String buildConnectionURL()
	{
		return String.format(
			"jdbc:postgresql://%s:%d/%s",
			this.appConfig.getDbConfig().getHostname(),
			this.appConfig.getDbConfig().getPort(),
			this.appConfig.getDbConfig().getDatabase());
	}
}