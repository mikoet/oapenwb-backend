// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.entity.ui.UiLanguage;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.TimeUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>This controller compiles and provides the dictionary's base configuration ({@see getBaseConfig}) and also offers
 * parts of the configuration for use in other controllers if needed ({@see getConfig}).</p>
 *
 * TODO REFACT See the TODO comment in {@link BaseConfig} about splitting the base config up
 */
@Singleton
public class ConfigController
{
	private static final Logger LOG = LoggerFactory.getLogger(ConfigController.class);

	/**
	 * <p>This method offers the base configuration as already converted Json string and should be used in the REST
	 * interface.</p>
	 *
	 * @return The base configuration as Json string
	 * @throws CodeException Will be thrown if the base configuration is not loaded
	 */
	public String getBaseConfig() throws CodeException
	{
		readLock.lock();
		String jsonData = this.baseConfig;
		readLock.unlock();

		if (jsonData == null || jsonData.isEmpty()) {
			throw new CodeException(ErrorCode.Config_BaseConfigNotLoaded);
		}

		return jsonData;
	}

	/**
	 * Will reload the configuration. This method should be used in the REST interface.
	 */
	public void reloadConfig() throws CodeException
	{
		loadConfig();
	}

	/**
	 * Loads the configuration. This method has to be called manually for initialization.
	 */
	public void loadConfig() throws CodeException
	{
		TimeUtil.startTimeMeasure();
		Session session = HibernateUtil.getSession();

		/* 1) Load all needed config data */
		// Load all active UiLanguages
		Query<UiLanguage> qLang = session.createQuery(
				"FROM UiLanguage L WHERE L.active = true",
				UiLanguage.class);
		List<UiLanguage> uiLanguages = qLang.list();

		// Load all Orthographies
		Query<Orthography> qOrtho = session.createQuery(
				"FROM Orthography",
				Orthography.class);
		List<Orthography> orthographies = qOrtho.list();

		// Load all Languages
		Query<Language> tQuery = session.createQuery(
				"FROM Language",
				Language.class);
		List<Language> langs = tQuery.list();

		// Load all TOMappings
		Query<LangOrthoMapping> tomQuery = session.createQuery(
				"FROM LangOrthoMapping ORDER BY langID ASC, position ASC",
				LangOrthoMapping.class);
		List<LangOrthoMapping> loMappings = tomQuery.list();

		// Load all LangPairs
		Query<LangPair> lpQuery = session.createQuery(
				"FROM LangPair",
				LangPair.class);
		List<LangPair> langPairs = lpQuery.list();

		// Load all LexemeTypes
		Query<LexemeType> etQuery = session.createQuery(
				"FROM LexemeType",
				LexemeType.class);
		List<LexemeType> lexemeTypes = etQuery.list();

		// 2) Create and fill the BaseConfig and make a JSON string from it
		BaseConfig baseConfig = new BaseConfig();
		baseConfig.setUiLanguages(uiLanguages);
		baseConfig.setOrthographies(orthographies);
		baseConfig.setLangs(langs);
		baseConfig.setLoMappings(loMappings);
		baseConfig.getDictionarySettings().setLangPairs(langPairs);
		baseConfig.setLexemeTypes(lexemeTypes);

		String result = null;
		try {
			result = toJson(baseConfig, Views.BaseConfig.class);
		} catch (JsonProcessingException e) {
			LOG.error("Serializing BaseConfig to JSON failed.", e);
			throw new CodeException(ErrorCode.Config_BaseConfigJsonError);
		}

		// 3) Create a config object containing all loaded entities for other controllers, i.e. the SearchController
		//    needs it to verify incoming queries
		Config config = new Config(uiLanguages, orthographies, langs, loMappings, langPairs, lexemeTypes);

		// 4) Set the now loaded baseconfig as a JSON string in the class attribute while using a writelock (!)
		try {
			writeLock.lock();
			this.baseConfig = result;
			this.config = config;
			LOG.info("Loaded config in " + TimeUtil.durationInMilis() + " milis");
		} catch (Exception e) {
			LOG.error("An exception was thrown while trying to set the newly loaded config", e);
		} finally {
			writeLock.unlock();
		}
	}

	// Serializes an object o to JSON with a given view class (see: https://www.baeldung.com/jackson-json-view-annotation)
	private String toJson(Object o, Class view) throws JsonProcessingException
	{
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		return mapper.writerWithView(view).writeValueAsString(o);
	}

	/**
	 * @return The configuration in object form
	 */
	public Config getConfig()
	{
		Config config;
		readLock.lock();
		config = this.config;
		readLock.unlock();
		return config;
	}

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	private String baseConfig = "";
	private Config config = null;
}