// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.context;

import jakarta.persistence.OptimisticLockException;

/**
 * <p>This class handles a real transaction.</p>
 */
public class Transaction  implements ITransaction
{
	private final org.hibernate.Transaction transaction;
	private boolean isDone = false;

	Transaction(org.hibernate.Transaction transaction)
	{
		this.transaction = transaction;
	}

	@Override
	public void commit() throws OptimisticLockException
	{
		this.transaction.commit();
	}

	@Override
	public void rollback()
	{
		transaction.rollback();
	}
}