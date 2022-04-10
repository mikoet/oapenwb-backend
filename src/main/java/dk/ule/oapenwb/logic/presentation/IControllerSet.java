package dk.ule.oapenwb.logic.presentation;

import dk.ule.oapenwb.entity.content.basedata.*;
import dk.ule.oapenwb.logic.admin.generic.ICEntityController;

public interface IControllerSet
{
	ICEntityController<Orthography, Integer> getOrthographiesController();
	ICEntityController<Language, Integer> getLanguagesController();
	ICEntityController<Category, Integer> getCategoriesController();
	ICEntityController<Level, Integer> getUnitLevelsController();
}
