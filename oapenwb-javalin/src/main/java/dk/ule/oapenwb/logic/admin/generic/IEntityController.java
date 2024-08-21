// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.generic;

import dk.ule.oapenwb.logic.admin.common.IRestController;
import dk.ule.oapenwb.persistency.entity.IEntity;

public interface IEntityController<T extends IEntity<S>, S> extends IRestController<T, S>
{
}
