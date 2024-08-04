// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.data.importer.csv;

public enum CheckType
{
	/**
	 * No check is performed at all.
	 * This is for files that were already checked before / where the person
	 * doing the import is sure runtime errors will not stop the import.
	 */
	None,

	/**
	 * All checks are performed before the actually import is started.
	 */
	EverythingBeforeImport,

	/**
	 * Each row is checked just before being processed.
	 */
	EachRowBeforeImport
}
