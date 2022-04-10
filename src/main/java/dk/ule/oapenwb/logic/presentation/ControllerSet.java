// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.generic.ICEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememeController;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contains all controllers needed to generated the lemmas / presentation.
 */
@NoArgsConstructor
public class ControllerSet implements IControllerSet
{
	@Getter
	private ICEntityController<Orthography, Integer> orthographiesController;

	@Getter
	private ICEntityController<Language, Integer> languagesController;

	@Getter
	private ICEntityController<Category, Integer> categoriesController;

	@Getter
	private ICEntityController<Level, Integer> unitLevelsController;

	@Getter
	private SememeController sememeController;

	public void setControllers(
		ICEntityController<Orthography, Integer> orthographiesController,
		ICEntityController<Language, Integer> languagesController,
		ICEntityController<Category, Integer> categoriesController,
		ICEntityController<Level, Integer> unitLevelsController,
		SememeController sememeController)
	{
		this.orthographiesController = orthographiesController;
		this.languagesController = languagesController;
		this.categoriesController = categoriesController;
		this.unitLevelsController = unitLevelsController;
		this.sememeController = sememeController;
	}
}