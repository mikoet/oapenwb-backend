// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.lexeme;

import com.fasterxml.jackson.annotation.JsonView;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.entity.ApiAction;

/**
 * Interface for entities participating in Remote Procedure Calls.
 */
public interface IRPCEntity<T>
{
	@JsonView(Views.REST.class)
	T getId();

	@JsonView(Views.REST.class)
	ApiAction getApiAction();
}