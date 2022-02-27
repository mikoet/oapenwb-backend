// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import dk.ule.oapenwb.faces.ConfigFace;
import dk.ule.oapenwb.faces.L10nFace;
import dk.ule.oapenwb.faces.SearchFace;
import dk.ule.oapenwb.faces.UsersFace;
import javalinjwt.JWTProvider;
import lombok.Getter;

/**
 * <p>This class creates the Javalin faces for the controllers.</p>
 */
class DictFaces {
	@Getter
	private ConfigFace config;

	@Getter
	private L10nFace l10n;

	@Getter
	private SearchFace search;

	@Getter
	private UsersFace users;

	DictFaces(DictControllers controllers, JWTProvider jwtProvider)
	{
		this.config = new ConfigFace(controllers.getConfig());
		this.l10n = new L10nFace(controllers.getL10n());
		this.search = new SearchFace(controllers.getSearch());
		this.users = new UsersFace(controllers.getUsers(), jwtProvider);
	}
}