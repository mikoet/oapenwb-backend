// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.logic.admin.locking;

import dk.ule.oapenwb.util.CurrentUser;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The LockController allows (one day) locking of entities by a user to avoid editing conflicts. The locking
 * and the communication of newly locked entities will be done via a websocket connection.
 * <ul>
 *   <li>This class is not used by now and only for getting the idea down.</li>
 *   <li>What to do if the connection is lost? Tell the user that the connection was lost, start the blocker,
 *     try to reconnect?</li>
 * </ul>
 * </p>
 */
public class LockController
{
	private static final Logger LOG = LoggerFactory.getLogger(LockController.class);
	
	private static class UserSession
	{
		private boolean authenticated = false;
	}
	
	private Map<WsContext, String> userUsernameMap = new ConcurrentHashMap<>();

	public void onConnect(@NotNull WsConnectContext ctx)
	{
		LOG.info("onConnect: " + CurrentUser.INSTANCE.get());
		ctx.send("Hi!");
	}

	public void onClose(@NotNull WsCloseContext ctx)
	{
	}

	public void onMessage(@NotNull WsMessageContext ctx)
	{
		LOG.info("onMessage: " + ctx.message());
		/*
		 * Sperren: Entity of type "Lexeme" with ID xy
		 *          and all of its entities of type "Mapping" and "Link"
		 * Websocket:
		 * - Teilt alle neuen Sperrungen mit, an alle aktiven Admins und Editoren, sofern diese Leserechte für den
		 *   Entitätstypen haben
		 *     - also müssen die aktiven Admins/Editoren im Speicher gehalten werden samt ihrer Berechtigungen
		 * - teilt bei Verbindungsaufbau alle aktuellen Sperren mit (nach JWT-Auth)
		 * - gibt bei Verbindungsabbruch/-ende alle gesperrten Entitäten des Admins/Editors frei
		 *
		 * - givt by vorbindingsafbrook/-ende alle sperden entitaeten van den admin/editoor vry
		 */
	}

	public void handleError(@NotNull WsErrorContext ctx) throws Exception
	{
	}
}