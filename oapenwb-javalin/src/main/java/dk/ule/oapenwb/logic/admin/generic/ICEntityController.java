// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import dk.ule.oapenwb.entity.IEntity;

import java.io.Serializable;

public interface ICEntityController<T extends IEntity<S>, S extends Serializable> extends IEntityController<T, S>
{
}
