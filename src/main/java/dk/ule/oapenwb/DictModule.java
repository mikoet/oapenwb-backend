// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.google.inject.AbstractModule;
import dk.ule.oapenwb.base.AppConfig;
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
		bind(AppConfig.class);
		bind(ViolationController.class);
		bind(ConfigController.class);
		bind(L10nController.class);
		bind(SearchController.class);
		bind(UserController.class);
		bind(DictControllers.class);
	}
}
