// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.entity;

/**
 * The different views to be used in the @JsonView annotation are defined as subclasses of this Views class.
 */
public class Views
{
	public static class BaseConfig {
	}

	public static class REST {
	}

	public static class Exclude {
	}
}