// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.base;

public enum RunMode
{
	Normal,
	Development,
	Testing	/* Mode can be set to testing for unit testing etc. The app will run with different configuration
			 * file then and drop the database on start and recreate it. */
}