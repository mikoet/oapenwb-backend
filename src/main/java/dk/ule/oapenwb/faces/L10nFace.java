// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces;

import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.l10n.L10nController;
import dk.ule.oapenwb.util.json.Response;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link L10nController}.
 */
public class L10nFace
{
	private static final Logger LOG = LoggerFactory.getLogger(L10nFace.class);
	private L10nController controller = new L10nController();

	public L10nFace(L10nController controller) {
		this.controller = controller;
	}

	public void getTranslations(@NotNull Context ctx) throws Exception {
		String result;
		try {
			final String locale = ctx.pathParam("locale");
			result = controller.getTranslations("", locale);
		} catch (CodeException e) {
			result = "{}";
		}
		ctx.contentType("application/json");
		ctx.result(result);
	}

	public void getTranslationsByScope(@NotNull Context ctx) throws Exception {
		String result;
		try {
			final String scope = ctx.pathParam("scope");
			final String locale = ctx.pathParam("locale");
			result = controller.getTranslations(scope, locale);
		} catch (CodeException e) {
			result = "{}";
		}
		ctx.contentType("application/json");
		ctx.result(result);
	}

	public void reloadTranslations(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		controller.reloadTranslations();
		/*
		res.setMessage(new IMessage() {
			@Override
			public int getCode() {
				return 0;
			}

			@Override
			public String getMessage() {
				return "done";
			}

			@Override
			public Pair<String, Object>[] getArguments() {
				return null;
			}
		});
		 */
		ctx.json(res);
	}
}