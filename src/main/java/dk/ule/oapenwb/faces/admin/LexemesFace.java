// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces.admin;

import com.google.inject.Inject;
import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.base.error.Message;
import dk.ule.oapenwb.base.error.MultiCodeException;
import dk.ule.oapenwb.logic.admin.lexeme.LexemesController;
import dk.ule.oapenwb.logic.admin.lexeme.LexemeDetailedDTO;
import dk.ule.oapenwb.logic.admin.lexeme.LSearchRequest;
import dk.ule.oapenwb.util.Pair;
import dk.ule.oapenwb.util.json.MultiResponse;
import dk.ule.oapenwb.util.json.PaginatedResponse;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Javalin face to the {@link LexemesController}.
 */
public class LexemesFace
{
	private static final Logger LOG = LoggerFactory.getLogger(LexemesFace.class);
	private final LexemesController controller;

	@Inject
	public LexemesFace(LexemesController controller)
	{
		this.controller = controller;
	}

	public void list(@NotNull Context ctx) throws Exception
	{
		PaginatedResponse res = new PaginatedResponse();
		try {
			LSearchRequest request = ctx.bodyAsClass(LSearchRequest.class);
			res.setData(controller.list(res.getPagination(), request));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void get(@NotNull Context ctx) throws Exception
	{
		Response res = new Response();
		try {
			Long id = Long.parseLong(ctx.pathParam("id"));
			res.setData(controller.get(id));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void getSlim(@NotNull Context ctx)
	{
		Response res = new Response();
		try {
			Long id = Long.parseLong(ctx.pathParam("id"));
			res.setData(controller.getOneSlim(id));
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void create(@NotNull Context ctx) throws Exception
	{
		MultiResponse res = new MultiResponse();
		try {
			LexemeDetailedDTO entity = ctx.bodyAsClass(LexemeDetailedDTO.class);
			res.setData(controller.create(entity));
		} catch (CodeException e) {
			res.getMessages().add(e);
			res.setStatus(ResponseStatus.Error);
		} catch (MultiCodeException e) {
			res.setMessages(e.getErrors());
			res.setStatus(ResponseStatus.Error);
		} catch (Exception e) {
			LOG.error("Unknown error occured when creating a lexeme", e);
			res.getMessages().add(new Message(ErrorCode.Admin_UnknownError.getCode(), e.getMessage(), null));
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void update(@NotNull Context ctx) throws Exception
	{
		MultiResponse res = new MultiResponse();
		try {
			LexemeDetailedDTO entity = ctx.bodyAsClass(LexemeDetailedDTO.class);
			Long id = Long.parseLong(ctx.pathParam("id"));
			res.setData(controller.update(id, entity));
		} catch (CodeException e) {
			res.getMessages().add(e);
			res.setStatus(ResponseStatus.Error);
		} catch (MultiCodeException e) {
			res.setMessages(e.getErrors());
			res.setStatus(ResponseStatus.Error);
		} catch (Exception e) {
			LOG.error("Unknown error occured when updating a lexeme", e);
			res.getMessages().add(new Message(ErrorCode.Admin_UnknownError.getCode(), e.getMessage(), null));
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void delete(@NotNull Context ctx) throws Exception
	{
		Response res = new Response();

		res.setStatus(ResponseStatus.Error);
		res.setMessage(new Message(ErrorCode.Admin_EntityOperation_NotSupported, Arrays.asList(
			new Pair<>("operation", "DELETE"), new Pair<>("entity", "Lexeme")
		)));

		// TODO Lexemes cannot be deleted as of now. Not sure if any editoring user should be able to do so.
		/* try {
			S id = controller.stringToId(ctx.pathParam("id"));
			controller.delete(id);
		} catch (CodeException e) {
			res.setMessage(e);
			res.setStatus(ResponseStatus.Error);
		} */

		ctx.json(res);
	}
}