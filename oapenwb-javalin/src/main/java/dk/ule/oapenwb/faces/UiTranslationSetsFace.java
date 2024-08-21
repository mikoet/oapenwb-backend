// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.admin.UiTranslationSetsController;
import dk.ule.oapenwb.persistency.entity.ui.UiTranslationSet;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link UiTranslationSetsController}.
 */
@Singleton
public class UiTranslationSetsFace
{
	private static final Logger LOG = LoggerFactory.getLogger(UiTranslationSetsFace.class);
	private final UiTranslationSetsController controller;

	@Inject
	public UiTranslationSetsFace(UiTranslationSetsController controller) {
		this.controller = controller;
	}

	public void create(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			UiTranslationSet uits = ctx.bodyAsClass(UiTranslationSet.class);
			res.setData(controller.create(uits));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void list(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			res.setData(controller.list());
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void get(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			String scope = handleScope(ctx.pathParam("scope"));
			String uitID = ctx.pathParam("uitID");
			res.setData(controller.get(scope, uitID));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void update(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			UiTranslationSet set = ctx.bodyAsClass(UiTranslationSet.class);
			String scope = handleScope(ctx.pathParam("scope"));
			String uitID = ctx.pathParam("uitID");
			controller.update(scope, uitID, set);
			// Get the entity to return it in the response
			res.setData(controller.get(scope, uitID));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void delete(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			String scope = handleScope(ctx.pathParam("scope"));
			String uitID = ctx.pathParam("uitID");
			controller.delete(scope, uitID);
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	/**
	 * Even though the default scope is an empty scope value in the database it will be transferred as '-' in requests.
	 */
	private String handleScope(String scope)
	{
		return "-".equals(scope) ? "" : scope;
	}
}