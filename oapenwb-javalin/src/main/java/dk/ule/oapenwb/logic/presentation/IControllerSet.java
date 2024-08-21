package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.persistency.entity.content.basedata.Category;
import dk.ule.oapenwb.persistency.entity.content.basedata.Level;
import dk.ule.oapenwb.persistency.entity.content.basedata.Orthography;
import dk.ule.oapenwb.logic.admin.LanguagesController;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;

public interface IControllerSet
{
	CEntityController<Orthography, Integer> getOrthographiesController();
	LanguagesController getLanguagesController();
	CEntityController<Category, Integer> getCategoriesController();
	CEntityController<Level, Integer> getUnitLevelsController();
}
