// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.Message;
import dk.ule.oapenwb.entity.IEntity;
import dk.ule.oapenwb.logic.admin.generic.EntityController;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Javalin face to the {@link EntityController}.
 *
 * @param <T> The entity's class that is managed
 * @param <S> The entity's ID type
 */
@Singleton
public class EntityFace<T extends IEntity<S>, S extends Serializable>
{
	private static final Logger LOG = LoggerFactory.getLogger(EntityFace.class);
	private final EntityController<T, S> controller;

	protected EntityController<T, S> getController() {
		return controller;
	}

	public EntityFace(EntityController<T, S> controller)
	{
		this.controller = controller;
	}

	public void create(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			T entity = ctx.bodyAsClass(controller.getClazz());
			res.setData(controller.create(entity));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		} catch (Exception e) {
			res.setMessage(new Message(0, e.getMessage(), null));
		}
		ctx.json(res);
	}

	public void list(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			res.setData(controller.list());
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void get(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			S id = controller.stringToId(ctx.pathParam("id"));
			T entity = controller.get(id);
			res.setData(entity);
			if (entity == null) {
				res.setStatus(ResponseStatus.Error);
				ctx.status(404);
			}
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void update(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			T entity = ctx.bodyAsClass(controller.getClazz());
			S id = controller.stringToId(ctx.pathParam("id"));
			controller.update(id, entity);
			// Get the entity to return it in the response
			res.setData(controller.get(id));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void delete(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			T entity = ctx.bodyAsClass(controller.getClazz());
			S id = controller.stringToId(ctx.pathParam("id"));
			controller.delete(id, entity);
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	/*
	One could imagine several more methods:

	 * Loads multiple entities by the given data parameter, which has to be a complex type specifying the fields to query
	 * with the wished values. Example: {name: 'Peter'}
	getBy(data: any): Promise<Array<EntityClass>>
	save(entity: EntityClass): Promise<EntityClass>
	update(id: any, entity: EntityClass): Promise<void>
	 * Deletes multiple entities by the given data parameter, which has to be a complex type specifying the fields to query
	 * with the wished values. Example: {name: 'Peter'}
	deleteBy(data: any): Promise<Array<EntityClass>>
	 */
}