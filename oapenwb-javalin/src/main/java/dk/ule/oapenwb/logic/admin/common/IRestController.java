// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.common;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.IEntity;
import dk.ule.oapenwb.logic.context.Context;

import java.util.List;

/**
 * <p>Standard interface for a REST controller in this application.</p>
 * <p>Some useful links:
 * <ul>
 *   <li>http://restcookbook.com/HTTP%20Methods/put-vs-post/</li>
 * </ul>
 * </p>
 *
 * TODO REFACT Is this interface even really needed? If so, then why isn't it implemented by controllers like the
 *   CEntityController or CGEntityController?
 *
 * @param <T> the type of the entity
 * @param <S> the type of the entity's ID
 */
public interface IRestController<T extends IEntity<S>, S>
{
	/**
	 * @return Each controller should have a context that is returned by this method
	 */
	Context getContext();

	/**
	 * @return a list of T instances
	 * @throws CodeException
	 */
	List<T> list() throws CodeException;

	/**
	 * @param id
	 * @return the object by ID or null
	 * @throws CodeException
	 */
	T get(S id) throws CodeException;

	/**
	 * <p>This is meant to be used for HTTP POST, that means when no specific ID is given as
	 * URL parameter (with an URL like "/myentity/").</p>
	 * <p>Basically the backend has to decide whether this results in an INSERT or an UPDATE
	 * command.</p>
	 *
	 * @param entity
	 * @return the ID of the persisted entity
	 * @throws CodeException
	 */
	Object create(T entity, final Context context) throws CodeException;
	default Object create(T entity) throws CodeException
	{
		return this.create(entity, getContext());
	}

	/**
	 * <p>This is meant to be used for HTTP PUT, that means when a specific ID is given as
	 * URL parameter (with an URL like "/myentity/5").</p>
	 *
	 * @param id
	 * @param entity
	 * @throws CodeException
	 */
	void update(S id, T entity, final Context context) throws CodeException;
	default void update(S id, T entity) throws CodeException
	{
		this.update(id, entity, getContext());
	}

	/**
	 * <p>This is meant to be used for HTTP DELETE, that means when a specific ID is given as
	 * URL parameter (with an URL like "/myentity/5") to delete an entity.</p>
	 *
	 * @param id
	 * @throws CodeException
	 */
	void delete(S id, T entity, final Context context) throws CodeException;
	default void delete(S id, T entity) throws CodeException
	{
		this.delete(id, entity, getContext());
	}
}