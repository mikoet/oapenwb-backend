// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.config.ConfigController;
import dk.ule.oapenwb.util.json.RawDataRepsonse;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link ConfigController}.
 */
@Singleton
public class ConfigFace
{
	private static final Logger LOG = LoggerFactory.getLogger(ConfigFace.class);
	private final ConfigController controller;

	@Inject
	public ConfigFace(ConfigController controller)
	{
		this.controller = controller;
	}

	public void getBaseConfig(@NotNull Context ctx)
	{
		RawDataRepsonse res = new RawDataRepsonse();
		try {
			res.setData(this.controller.getBaseConfig());
		} catch (CodeException ee) {
			res.setMessage(ee);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void reloadConfig(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			this.controller.reloadConfig();
			res.setData("done");
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}
}