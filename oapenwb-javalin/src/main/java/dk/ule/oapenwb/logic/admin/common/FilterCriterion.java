// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * <p>The FilterCriterion represents a criteron for filtering entities in the controllers in methods such as
 * getBy(…) or deleteBy(…). As such it consists of the attribute's name to be filtered, the value to look for
 * as well as an operator to perform the filtering.</p>
 * <p>The criterion is to be configured via code and then translated into SQL by
 * {@link dk.ule.oapenwb.logic.admin.generic.EntityController}.</p>
 */
@Data
@AllArgsConstructor
public class FilterCriterion
{
	public enum Operator
	{
		Equals("=", true),
		EqualsNot("<>", true),
		Like("like", true),
		IsNull("is null", false),
		IsNotNull("is not null", false);

		@Getter
		private final String sql;

		@Getter
		private final boolean useValue;

		Operator(final String sql, boolean useValue) {
			this.sql = sql;
			this.useValue = useValue;
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
