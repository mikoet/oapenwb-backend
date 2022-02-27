// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * <p>CGEntityControler stands for Cached Grouped Entity Controller and it implements an EntityController that caches its
 * entities grouped by a grouping key. Just like {@link CEntityController} it's best to be used when there do not exist
 * too many entities of its type T.</p>
 *
 * @param <T> Entity type
 * @param <S> Type of the entity IDs
 * @param <R> Type of the grouping key
 */
public class CGEntityController<T extends IEntity<S>, S extends Serializable, R>
	extends EntityController<T, S>
	implements IGroupedEntitySupplier<T, S, R>
{
	private static final Logger LOG = LoggerFactory.getLogger(CGEntityController.class);

	// Caches that may only be accessed if readLock or writeLock are getting locked for read/write access
	private Map<R, Map<S, T>> groupedCache = new LinkedHashMap<>();
	private Map<S, T> directCache = new LinkedHashMap<>();
	private boolean initialized = false;
	// The locks
	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock readLock = readWriteLock.readLock();
	private final Lock writeLock = readWriteLock.writeLock();

	// Function to get the grouping key from the entity
	private final Function<T, R> groupKeyFn;

	public CGEntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn,
		boolean resetIdOnCreate, Function<T, R> groupKeyFn)
	{
		super(supplier, clazz, convertFn, resetIdOnCreate);
		this.groupKeyFn = groupKeyFn;
	}

	public CGEntityController(Supplier<T> supplier, Class<T> clazz, Function<String[], S> convertFn,
		Function<T, R> groupKeyFn)
	{
		this(supplier, clazz, convertFn, true, groupKeyFn);
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
		R groupKey = getGroupKey(entity);
		// do't entity in'n groupCache
		insertIntoGroupedCache(entity, groupKey);
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
		if (entity != null) {
			R groupKey = getGroupKey(entity);
			Map<S, T> groupCache = groupedCache.get(groupKey);
			if (groupCache != null) {
				groupCache.remove(entity.getEntityID());
			}
		}
		writeLock.unlock();
	}

	/*
	@Override
	public T getEntityByGroupKeyAndID(R groupKey, S id) throws CodeException {
		if (!initialized) {
			initialize();
		}
		T result = null;
		readLock.lock();
		Map<S, T> groupCache = groupedCache.get(groupKey);
		if (groupCache != null) {
			result = groupCache.get(id);
		}
		readLock.unlock();
		return result;
	}
	 */

	@Override
	public List<T> getEntitiesByGroupKey(R groupKey) throws CodeException {
		if (!initialized) {
			initialize();
		}
		List<T> result = null;
		readLock.lock();
		Map<S, T> groupCache = groupedCache.get(groupKey);
		if (groupCache != null) {
			result = new LinkedList<>(groupCache.values());
		}
		readLock.unlock();
		return result;
	}

	protected R getGroupKey(T entity)
	{
		return groupKeyFn.apply(entity);
	}

	private void initialize() throws CodeException {
		try {
			// Lade alle entities öäver de basisklasse
			List<T> allEntities = super.list();

			writeLock.lock();
			// Make de caches leddig
			directCache.clear();
			groupedCache.clear();
			// Iterere öäver alle entiteten un sortere se in'n cache in
			for (T entity : allEntities) {
				// do't entity in'n directCache
				directCache.put(entity.getEntityID(), entity);
				R groupKey = getGroupKey(entity);
				// do't entity in'n groupCache
				insertIntoGroupedCache(entity, groupKey);
			}
			initialized = true;
		} catch (Exception e) {
			LOG.error("Could not initialize this controller (entity: " + this.getClazz().getSimpleName() + "): ", e);
			throw e;
		} finally {
			writeLock.unlock();
		}
	}

	/**
	 * Only call this method in other methods when you first had the writeLock locked
 	 */
	private void insertIntoGroupedCache(T entity, R groupKey)
	{
		Map<S, T> groupCache = groupedCache.computeIfAbsent(groupKey, k -> new HashMap<>());
		groupCache.put(entity.getEntityID(), entity);
	}
}