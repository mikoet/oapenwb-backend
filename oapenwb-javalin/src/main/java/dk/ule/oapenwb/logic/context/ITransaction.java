// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.context;

import jakarta.persistence.OptimisticLockException;

/**
 * <p>Interface for a transaction returned by a Controller's context.
 * One would use this transaction in two ways:
 * <ol>
 *   <li>Either one would call manage to call commit() and rollback() by oneself, i.e. commit()
 *     would be called at the end of a try block when things are done, and rollback() in each
 *     of the catch blocks for that try block respectively.</li>
 *   <li>One would not use commit() and rollback() at all, but instead call the done() once all
 *     operations succeeded at the end of a try block, and call aftermath() in a finally block
 *     after the catch blocks. Then aftermath() would decide to do a commit() or rollback()
 *     itself.</li>
 * </ol>
 * </p>
 */
public interface ITransaction {
	void commit() throws OptimisticLockException;
	void rollback();

	//void done();
	//void aftermath();
}