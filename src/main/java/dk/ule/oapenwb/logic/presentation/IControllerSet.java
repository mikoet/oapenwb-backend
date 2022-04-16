package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.entity.content.basedata.Category;
import dk.ule.oapenwb.entity.content.basedata.Language;
import dk.ule.oapenwb.entity.content.basedata.Level;
import dk.ule.oapenwb.entity.content.basedata.Orthography;
import dk.ule.oapenwb.logic.admin.generic.CEntityController;

public interface IControllerSet
{
	CEntityController<Orthography, Integer> getOrthographiesController();
	CEntityController<Language, Integer> getLanguagesController();
	CEntityController<Category, Integer> getCategoriesController();
	CEntityController<Level, Integer> getUnitLevelsController();
}
