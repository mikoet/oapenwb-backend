// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.IEntity;

import java.io.Serializable;
import java.util.List;

/**
 * Interface to specify the access onto entities grouped by a grouping key. For more see {@link CGEntityController}.
 *
 * @param <T> Type of the entity
 * @param <S> Type of the entity's ID
 * @param <R> Type of the grouping key which will be a property of the entity not being its ID
 */
public interface IGroupedEntitySupplier<T extends IEntity<S>, S extends Serializable, R>
{
	//T getEntityByGroupKeyAndID(R groupKey, S id) throws CodeException;
	List<T> getEntitiesByGroupKey(R groupKey) throws CodeException;
}