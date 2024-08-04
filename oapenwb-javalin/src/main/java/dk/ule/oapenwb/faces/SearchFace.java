// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.search.SearchController;
import dk.ule.oapenwb.logic.search.SearchRequest;
import dk.ule.oapenwb.logic.search.SearchResult;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link SearchController}.
 */
@Singleton
public class SearchFace
{
	private static final Logger LOG = LoggerFactory.getLogger(SearchFace.class);
	private final SearchController controller;

	@Inject
	public SearchFace(SearchController controller)
	{
		this.controller = controller;
	}

	public void executeQuery(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			SearchRequest queryData = ctx.bodyAsClass(SearchRequest.class);
			SearchResult result = this.controller.find(queryData);
			res.setData(result);
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}
}
