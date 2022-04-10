package dk.ule.oapenwb.logic.admin.generic;

import dk.ule.oapenwb.logic.admin.common.IRestController;

public interface IEntityController<T extends IEntity<S>, S> extends IRestController<T, S>
{
}
