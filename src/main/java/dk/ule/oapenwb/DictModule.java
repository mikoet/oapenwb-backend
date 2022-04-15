// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.faces.ConfigFace;
import dk.ule.oapenwb.faces.L10nFace;
import dk.ule.oapenwb.faces.SearchFace;
import dk.ule.oapenwb.faces.UsersFace;
import dk.ule.oapenwb.logic.config.ConfigController;
import dk.ule.oapenwb.logic.l10n.L10nController;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.logic.users.UserController;
import dk.ule.oapenwb.logic.users.ViolationController;

public class DictModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		configureDictClasses();
	}

	private void configureDictClasses()
	{
		bind(AppConfig.class).asEagerSingleton();
		bind(DictJwtProvider.class);

		bind(ViolationController.class);
		bind(ConfigController.class);
		bind(L10nController.class);
		bind(SearchController.class);
		bind(UserController.class);
		bind(DictControllers.class);

		bind(ConfigFace.class);
		bind(L10nFace.class);
		bind(SearchFace.class);
		bind(UsersFace.class);
	}
}
