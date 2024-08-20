// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.persistency.entity;

import com.fasterxml.jackson.annotation.JsonView;

/**
 * <p>Interface for entities participating in Remote Procedure Calls.</p>
 * <p>Just like the interface {@link IRPCEntity}, this one is to be ideally eliminated one day,
 * as well.</p>
 */
public interface IRPCEntity<T>
{
	@JsonView(Views.REST.class)
	T getId();

	@JsonView(Views.REST.class)
	ApiAction getApiAction();
}