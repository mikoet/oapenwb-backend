// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * <p>The FilterCriterion represents a criteron for filtering entities in the controllers in methods such as
 * getBy(…) or deleteBy(…). As such it consists of the attribute's name to be filtered, the value to look for
 * as well as an operator to perform the filtering.</p>
 * <p>The criterion is to be configured via code and then translated into SQL.
 * TODO translated into SQL by..?</p>
 */
@Data
@AllArgsConstructor
public class FilterCriterion
{
	public enum Operator
	{
		Equals("="),
		EqualsNot("<>"),
		Like("like");
		private String sql;
		Operator(final String sql) {
			this.sql = sql;
		}
		@Override
		public String toString() {
			return sql;
		}
	}

	private String attribute;
	private String value;
	private Operator operator;
}