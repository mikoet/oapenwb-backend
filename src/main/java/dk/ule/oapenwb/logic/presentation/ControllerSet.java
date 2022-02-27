// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememeController;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Contains all controllers needed to generated the lemmas / presentation.
 */
@NoArgsConstructor
public class ControllerSet
{
	@Getter
	private CEntityController<Orthography, Integer> orthographiesController;

	@Getter
	private CEntityController<Language, Integer> languagesController;

	@Getter
	private CEntityController<Category, Integer> categoriesController;

	@Getter
	private CEntityController<Level, Integer> unitLevelsController;

	@Getter
	private CEntityController<LinkType, Integer> linkTypesController;

	@Getter
	private SememeController sememeController;

	public void setControllers(
		CEntityController<Orthography, Integer> orthographiesController,
		CEntityController<Language, Integer> languagesController,
		CEntityController<Category, Integer> categoriesController,
		CEntityController<Level, Integer> unitLevelsController,
		CEntityController<LinkType, Integer> linkTypesController,
		SememeController sememeController)
	{
		this.orthographiesController = orthographiesController;
		this.languagesController = languagesController;
		this.categoriesController = categoriesController;
		this.unitLevelsController = unitLevelsController;
		this.linkTypesController = linkTypesController;
		this.sememeController = sememeController;
	}
}