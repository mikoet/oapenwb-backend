// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces;

import dk.ule.oapenwb.logic.search.QueryObject;
import dk.ule.oapenwb.logic.search.ResultObject;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.util.json.Response;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link SearchController}.
 */
public class SearchFace
{
	private static final Logger LOG = LoggerFactory.getLogger(SearchFace.class);
	private SearchController controller;

	public SearchFace(SearchController controller) {
		this.controller = controller;
	}

	public void executeQuery(@NotNull Context ctx) throws Exception {
		LOG.info("Got search query");
		QueryObject queryData = ctx.bodyAsClass(QueryObject.class);
		ResultObject result = this.controller.executeQuery(queryData);
		Response res = new Response();
		res.setData(result);
		ctx.json(res);
	}
}