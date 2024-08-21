// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * <p>Interface that must be implemented by entities that are to be managed by a generic controller
 * as used in <b>oapenwb-javalin</b> project, i.e. EntityController, CEntityController, or CGEntityController.</p>
 *
 * <ul>
 *   <li>TODO Compound IDs (embeddables) may not work 100% at all times in the controllers. This is actually to be
 *     tested. But so far the <b>UiTranslationSetsController</b> makes use of
 *     a standard EntityController.</li>
 *   <li>As this interface is coupled to the EntityController and similar other controllers, you may ask,
 *   why the heck is this interface part of the persistence project. And you're right. This interface is, ideally,
 *   to be elimanated entirely one day together with the oapenwb-javalin project. But that needs a lot of refactoring
 *   and rewriting of code, so this is a compromise.</li>
 * </ul>
 *
 * @param <T> datatype of the ID object
 */
public interface IEntity<T>
{
	void setEntityID(T id); // needed for deletion of entities
	@JsonIgnore
	T getEntityID();
}