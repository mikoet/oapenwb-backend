// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dk.ule.oapenwb.logic.admin.UiTranslationSetsController;

/**
 * <p>Interface that must be implemented by entities that are to be managed by a generic controller,
 * i.e. {@link EntityController}, {@link CEntityController}, or {@link CGEntityController}.</p>
 *
 * <ul>
 *   <li>TODO Compound IDs (embeddables) may not work 100% at all times in the controllers. This is actually to be
 *     tested. But so far the {@link UiTranslationSetsController makes use of
 *     a standard EntityController.}</li>
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