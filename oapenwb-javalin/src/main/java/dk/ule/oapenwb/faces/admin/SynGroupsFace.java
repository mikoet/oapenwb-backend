// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.admin.syngroup.SGSearchRequest;
import dk.ule.oapenwb.logic.admin.syngroup.SynGroupsController;
import dk.ule.oapenwb.persistency.entity.content.lexemes.SynGroup;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link SynGroupsController}.
 */
public class SynGroupsFace extends EntityFace<SynGroup, Integer>
{
	private static final Logger LOG = LoggerFactory.getLogger(SynGroupsFace.class);

	@Override
	protected SynGroupsController getController() {
		return (SynGroupsController) super.getController();
	}

	@Inject
	public SynGroupsFace(SynGroupsController controller)
	{
		super(controller);
	}

	public void find(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			SGSearchRequest request = ctx.bodyAsClass(SGSearchRequest.class);
			res.setData(getController().find(request));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}
}
