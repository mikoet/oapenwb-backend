// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import dk.ule.oapenwb.AdminControllers;
import dk.ule.oapenwb.persistency.entity.content.basedata.Category;
import dk.ule.oapenwb.persistency.entity.content.basedata.Level;
import dk.ule.oapenwb.persistency.entity.content.basedata.Orthography;
import dk.ule.oapenwb.logic.admin.LanguagesController;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememesController;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contains all controllers needed to generated the lemmas / presentation.
 */
@Singleton
@NoArgsConstructor
public class ControllerSet implements IControllerSet
{
	@Getter
	@Inject
	@Named(AdminControllers.CONTROLLER_ORTHOGRAPHIES)
	private CEntityController<Orthography, Integer> orthographiesController;

	@Getter
	@Inject
	private LanguagesController languagesController;

	@Getter
	@Inject
	@Named(AdminControllers.CONTROLLER_CATEGORIES)
	private CEntityController<Category, Integer> categoriesController;

	@Getter
	@Inject
	@Named(AdminControllers.CONTROLLER_UNIT_LEVELS)
	private CEntityController<Level, Integer> unitLevelsController;

	@Getter
	@Inject
	private SememesController sememesController;

	// For use in testing without Guice
	public void setControllers(
		CEntityController<Orthography, Integer> orthographiesController,
		LanguagesController languagesController,
		CEntityController<Category, Integer> categoriesController,
		CEntityController<Level, Integer> unitLevelsController,
		SememesController sememesController)
	{
		this.orthographiesController = orthographiesController;
		this.languagesController = languagesController;
		this.categoriesController = categoriesController;
		this.unitLevelsController = unitLevelsController;
		this.sememesController = sememesController;
	}
}
