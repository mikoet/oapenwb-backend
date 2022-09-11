// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.basedata.LangPair;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.logic.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>The basic concepts of this controller were copied from class CEntityController.
 * <ul>
 *   <li>TODO Why is the localesCache created, but never used? Purpose? Was it meant to be used to aid creation
 *     of the mappings in the frontend? How are the languages mapped there now?</li>
 *   <li>TODO So check if this must be a class on its own, and only if so, do some more commenting.</li>
 * </ul>
 * </p>
 */
@Singleton
public class LangPairsController extends EntityController<LangPair, String>
{
	private static final Logger LOG = LoggerFactory.getLogger(LangPairsController.class);

	// Caches that may only be accessed if readLock or writeLock are getting locked for read/write access
	private Map<String, LangPair> directCache = new LinkedHashMap<>();
	private Map<String, Set<String>> localesCache = new HashMap<>();
	private boolean initialized = false;
	// The locks
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	// LanguageController
	private final LanguagesController languagesController;

	@Inject
	public LangPairsController(
		LanguagesController languagesController
	) {
		super(LangPair::new, LangPair.class, ids -> ids[0], false);
		this.languagesController = languagesController;
	}

	@Override
	protected String getDefaultOrderClause() {
		return " order by E.position ASC";
	}

	/*
	 * Wat bruukt düssen controller?
	 * o En cache van jeade språke to de ander språken nå dee dat språkpåren geaven deit
	 *   HashMap<locale as String, List<other languages locale as String>>
	 */

	@Override
	public LangPair get(String id) throws CodeException {
		if (!initialized) {
			initialize();
		}
		readLock.lock();
		LangPair entity = directCache.get(id);
		readLock.unlock();
		return entity;
	}

	@Override
	public List<LangPair> list() throws CodeException {
		if (!initialized) {
			initialize();
		}
		List<LangPair> result;
		readLock.lock();
		result = new LinkedList<>(directCache.values());
		readLock.unlock();
		return result;
	}

	@Override
	public Object create(LangPair entity, final Context context) throws CodeException {
		if (!initialized) {
			initialize();
		}
		String id = (String) super.create(entity, context);
		// Hyr mut de cäche wegsmeaten un allens ny laden warden sodännig dat allens richtig sorteerd is.
		initialize();

		return id;
	}

	@Override
	public void update(String id, LangPair entity, final Context context) throws CodeException {
		if (!initialized) {
			initialize();
		}
		super.update(id, entity, context);
		// Reload the entity and cache it
		LangPair reloaded = super.get(id);
		writeLock.lock();
		directCache.put(reloaded.getEntityID(), reloaded);
		writeLock.unlock();
	}

	@Override
	public void delete(String id, LangPair entity, final Context context) throws CodeException {
		if (!initialized) {
			initialize();
		}
		super.delete(id, entity, context);
		// Remove the entity from the caches
		writeLock.lock();
		entity = directCache.remove(id);
		writeLock.unlock();
	}

	private void initialize() throws CodeException {
		try {
			// Lade alle entities öäver de basisklasse
			List<LangPair> allEntities = super.list();

			writeLock.lock();
			// Make den cache leddig
			directCache.clear();
			// Iterere öäver alle entiteten un sortere se in'n cache in
			for (LangPair entity : allEntities) {
				// do't entity in'n directCache
				directCache.put(entity.getEntityID(), entity);
				// un bouw ouk den localesCache up (this is additional to CEntityController)
				cacheLanguagesOfPair(entity);
			}
			initialized = true;
		} catch (Exception e) {
			LOG.error("Could not initialize this controller (entity: " + this.getClazz().getSimpleName() + "): ", e);
			throw e;
		} finally {
			writeLock.unlock();
		}
	}

	// May only be called when the writeLock is already locked
	private void cacheLanguagesOfPair(final LangPair pair) throws CodeException
	{
		Language langOne = languagesController.get(pair.getLangOneID());
		Language langTwo = languagesController.get(pair.getLangTwoID());

		if (langOne == null) {
			throw new RuntimeException("Language with ID " + pair.getLangOneID()
				+ " does not exist. But it should indeed! This occured for LangPair " + pair.getId() + ".");
		}
		if (langTwo == null) {
			throw new RuntimeException("Language with ID " + pair.getLangTwoID()
				+ " does not exist. But it should indeed! This occured for LangPair " + pair.getId() + ".");
		}

		Set<String> langOneSet = localesCache.get(langOne.getLocale());
		if (langOneSet == null) {
			langOneSet = new HashSet<>();
			localesCache.put(langOne.getLocale(), langOneSet);
		}
		langOneSet.add(langTwo.getLocale());

		Set<String> langTwoSet = localesCache.get(langTwo.getLocale());
		if (langTwoSet == null) {
			langTwoSet = new HashSet<>();
			localesCache.put(langTwo.getLocale(), langTwoSet);
		}
		langTwoSet.add(langOne.getLocale());
	}
}