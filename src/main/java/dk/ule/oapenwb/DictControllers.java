// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.logic.config.ConfigController;
import dk.ule.oapenwb.logic.l10n.L10nController;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.logic.users.UserController;
import dk.ule.oapenwb.logic.users.ViolationController;
import lombok.Getter;

/**
 * <p>This class creates the controllers for the standard dictionary's interface and connects them to each other if
 * necessary.</p>
 */
class DictControllers
{
	@Getter
	private ViolationController violations;

	@Getter
	private ConfigController config;

	@Getter
	private L10nController l10n;

	@Getter
	private SearchController search;

	@Getter
	private UserController users;

	DictControllers(AppConfig appConfig)
	{
		this.violations = new ViolationController();
		this.config = new ConfigController();
		this.l10n = new L10nController();
		this.search = new SearchController();
		this.users = new UserController(appConfig.isSendEmails(), violations);
	}
}