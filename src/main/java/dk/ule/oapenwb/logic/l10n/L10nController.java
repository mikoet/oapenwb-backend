// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.l10n;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.ui.UiLanguage;
import dk.ule.oapenwb.entity.ui.UiTranslation;
import dk.ule.oapenwb.entity.ui.UiTranslationScope;
import dk.ule.oapenwb.util.HibernateUtil;
import dk.ule.oapenwb.util.TimeUtil;
import io.javalin.plugin.json.JavalinJackson;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>L10NController loads the localization data and exposes it.</p>
 *
 * TODO Describe how it works (caching in translations map)
 * TODO Tell about the reload mechanism
 * TODO FEATURE Offer a new method for lazy loading single translations/texts for a single locale which
 *   shall be used for lazy loading long descriptive texts
 * TODO Make it work to support scopes. Edit: That was already done?
 */
@Singleton
public class L10nController
{
	private static final Logger LOG = LoggerFactory.getLogger(L10nController.class);

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	// key: scope, value: (key: locale, value: all translations for that value already as a json string)
	private Map<String, Map<String, String>> translations = new HashMap<>();

	public String getTranslations(final String scope, @NotNull final String locale) throws CodeException
	{
		String jsonData;

		readLock.lock();
		Map<String, String> scopeContent = translations.get(scope);
		if (scopeContent != null) {
			jsonData = scopeContent.get(locale);
		} else {
			jsonData = "{}";
		}
		readLock.unlock();

		if (jsonData == null) {
			throw new CodeException(ErrorCode.L10n_TranslationsNotLoaded);
		}

		return jsonData;
	}

	public void reloadTranslations() throws JsonProcessingException {
		loadTranslations();
	}

	public void loadTranslations() throws JsonProcessingException {
		TimeUtil.startTimeMeasure();
		Session session = HibernateUtil.getSession();

		// 1) Query all active UiLanguages
		Query<UiLanguage> qLang = session.createQuery(
				"FROM UiLanguage L WHERE L.active = true",
				UiLanguage.class);
		List<UiLanguage> languages = qLang.list();

		// 2) Query all scopes
		Query<UiTranslationScope> qScopes = session.createQuery(
				"FROM UiTranslationScope",
				UiTranslationScope.class);
		List<UiTranslationScope> scopes = qScopes.list();

		// 3) Put the locales of all active UiLanguages into a seperate list, so that the locales can be used
		//    in a foloowing IN operation
		List<String> locales = new ArrayList<>(languages.size());
		for (UiLanguage language : languages) {
			locales.add(language.getLocale());
		}

		// 4) Iterate over all scopes and query the belonging translations
		// key: scope, value: (key: locale, value: all translations for that value already as a json string)
		Map<String, Map<String, String>> result = new HashMap<>();
		for (UiTranslationScope scope : scopes) {
			// 4.a) Query all UiTranslations that belong to the locales put into the locales list, all this for the
			//      current scope.
			List<UiTranslation> translations;
			if (locales.size() > 0) {
				Query<UiTranslation> qTranslation = session.createQuery(
					"FROM UiTranslation T WHERE T.uitKey.locale IN (:locales) AND T.uitKey.scopeID = :scope " +
					"ORDER BY T.uitKey.id ASC", UiTranslation.class);
				qTranslation.setParameterList("locales", locales);
				qTranslation.setParameter("scope", scope.getId());
				translations = qTranslation.list();
			} else {
				translations = new ArrayList<>();
			}

			// Map<locale, Map<translation ID, translation text>>
			Map<String, Map<String, String>> groupedTranslations = new HashMap<>();
			// 4.b) Sort the list of loaded translations into the groupedTranslations container
			translations.forEach((translation) -> {
				String locale = translation.getLocale();
				Map<String, String> translationsMap =  groupedTranslations.computeIfAbsent(
					locale, keyValue -> new TreeMap<>());
				// using TreeMap so the translations are sorted by the id
				translationsMap.put(translation.getId(), translation.getText());
			});

			// Map<locale, translations as JSON string>
			Map<String, String> jsonStringsByLocale = new HashMap<>();
			for (Map.Entry<String, Map<String, String>> entry : groupedTranslations.entrySet()) {
				String locale = entry.getKey();
				Map<String, String> translationsMap = entry.getValue();
				// 4.c) Convert each TreeMap into a JSON string
				// TODO JavalinJson should not be used here. Edit: Because...?
				String json = JavalinJackson.Companion.defaultMapper().writeValueAsString(translationsMap);
				jsonStringsByLocale.put(locale, json);
			}

			result.put(scope.getId(), jsonStringsByLocale);
		}

		// 5) Set the now (re)loaded translations in the class attribute while using a writelock (!)
		try {
			writeLock.lock();
			this.translations = result;
			LOG.info("Loaded translations in " + TimeUtil.durationInMilis() + " milis");
		} catch (Exception e) {
			LOG.error("An exception was thrown while trying to set the newly loaded translations", e);
		} finally {
			writeLock.unlock();
		}
	}
}