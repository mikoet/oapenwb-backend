// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>CEntityControler stands for Cached Entity Controller and it implements an EntityController that caches its entities
 * for fast retrieval. It's best to be used when there do not exist too many entities of its type T (like maybe some
 * hundreds, maybe a few thousands, as they will all be kept in memory – so in the end it depends on the actual size
 * of the entities).</p>
 *
 * @param <T> Entity type
 * @param <S> ID type of entity
 */
@Singleton
public class CEntityController<T extends IEntity<S>, S extends Serializable> extends EntityController<T, S>
	implements ICEntityController<T, S>
{
	private static final Logger LOG = LoggerFactory.getLogger(CEntityController.class);

	// Caches that may only be accessed if readLock or writeLock are getting locked for read/write access
	private final Map<S, T> directCache = new LinkedHashMap<>();
	private boolean initialized = false;

	// The locks
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	public CEntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn,
		boolean resetIdOnCreate)
	{
		super(supplier, clazz, convertFn, resetIdOnCreate);
	}

	public CEntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn)
	{
		this(supplier, clazz, convertFn, true);
	}

	@Override
	public T get(S id) throws CodeException {
		if (!initialized) {
			initialize();
		}
		readLock.lock();
		T entity = directCache.get(id);
		readLock.unlock();
		return entity;
	}

	@Override
	public List<T> list() throws CodeException {
		if (!initialized) {
			initialize();
		}
		List<T> result;
		readLock.lock();
		result = new LinkedList<>(directCache.values());
		readLock.unlock();
		return result;
	}

	@Override
	public Object create(T entity, final Context context) throws CodeException {
		if (!initialized) {
			initialize();
		}
		S id = (S) super.create(entity, context);
		// Hyr mut de cäche wegsmeaten un allens ny laden warden sodännig dat allens richtig sorteerd is.
		initialize();

		return id;
	}

	@Override
	public void update(S id, T entity, final Context context) throws CodeException {
		if (!initialized) {
			initialize();
		}
		super.update(id, entity, context);
		// Reload the entity and cache it
		T reloaded = super.get(id);
		writeLock.lock();
		directCache.put(reloaded.getEntityID(), reloaded);
		writeLock.unlock();
	}

	@Override
	public void delete(S id, T entity, final Context context) throws CodeException {
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
			List<T> allEntities = super.list();

			writeLock.lock();
			// Make den cache leddig
			directCache.clear();
			// Iterere öäver alle entiteten un sortere se in'n cache in
			for (T entity : allEntities) {
				// do't entity in'n directCache
				directCache.put(entity.getEntityID(), entity);
			}
			initialized = true;
		} catch (Exception e) {
			LOG.error("Could not initialize this controller (entity: " + this.getClazz().getSimpleName() + "): ", e);
			throw e;
		} finally {
			writeLock.unlock();
		}
	}
}