// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.faces.*;
import lombok.Getter;

/**
 * <p>This class creates the Javalin faces for the controllers.</p>
 */
@Singleton
class DictFaces
{
	@Getter
	@Inject
	private ConfigFace config;

	@Getter
	@Inject
	private L10nFace l10n;

	@Getter
	@Inject
	private SearchFace search;

	@Getter
	@Inject
	private AutocompleteFace autocomplete;

	@Getter
	@Inject
	private UsersFace users;
}
