// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.context;

import dk.ule.oapenwb.util.HibernateUtil;
import lombok.Getter;

/**
 * The class Context represents a transaction context.
 */
public class Context
{
	/**
	 * Use this context instance for inner controllers that shall neither create new transactions
	 * nor publish revision comments they set.
	 */
	public static Context USE_OUTER_TRANSACTION_CONTEXT = new Context(false);

	@Getter
	private final boolean transactional;

	public Context(boolean transactional)
	{
		this.transactional = transactional;
	}

	public ITransaction beginTransaction()
	{
		if (transactional) {
			return new Transaction(
				HibernateUtil.getSession().beginTransaction());
		} else {
			return new NoTransaction();
		}
	}

	public void setRevisionComment(final String comment)
	{
		if (transactional) {
			HibernateUtil.setRevisionComment(comment);
		}
	}
}