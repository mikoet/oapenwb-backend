// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.context;

import javax.persistence.OptimisticLockException;

/**
 * <p>An instance of this class is used when a controller's operation does not create
 * it's own transaction.</p>
 *
 * TODO Describe why it's needed.
 */
public class NoTransaction implements ITransaction
{
	NoTransaction()
	{
	}

	@Override
	public void commit() throws OptimisticLockException
	{
	}

	@Override
	public void rollback()
	{
	}
}
