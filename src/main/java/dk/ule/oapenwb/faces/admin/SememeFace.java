// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.entity.content.lexemes.lexeme.Sememe;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SSearchRequest;
import dk.ule.oapenwb.logic.admin.lexeme.sememe.SememeController;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link SememeController}.
 */
public class SememeFace extends EntityFace<Sememe, Long>
{
	private static final Logger LOG = LoggerFactory.getLogger(SememeFace.class);

	@Override
	protected SememeController getController() {
		return (SememeController) super.getController();
	}

	public SememeFace(SememeController controller)
	{
		super(controller);
	}

	public void find(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			SSearchRequest request = ctx.bodyAsClass(SSearchRequest.class);
			res.setData(getController().find(request));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void getSlim(@NotNull Context ctx) throws Exception
	{
		Response res = new Response();
		try {
			Long id = Long.parseLong(ctx.pathParam("id"));
			res.setData(getController().getOneSlim(id));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}
}