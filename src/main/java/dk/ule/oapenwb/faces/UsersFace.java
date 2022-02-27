// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb.faces;

import dk.ule.oapenwb.base.ErrorCode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.logic.users.LoginObject;
import dk.ule.oapenwb.logic.users.LoginUser;
import dk.ule.oapenwb.logic.users.RegisterObject;
import dk.ule.oapenwb.logic.users.UserController;
import dk.ule.oapenwb.util.json.Response;
import dk.ule.oapenwb.util.json.ResponseStatus;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import javalinjwt.JWTProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.hibernate.JDBCException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Javalin face to the {@link UserController} that utilizes the {@link JWTProvider}, an aditional library built
 * for Javalin to generate JWTs, along the way.
 */
public class UsersFace
{
	@Data
	@AllArgsConstructor
	public static class LoginResponse {
		private String firstname;
		private String lastname;
		private String username;
		private String token;
	}

	private static final Logger LOG = LoggerFactory.getLogger(UsersFace.class);
	private UserController controller;
	private JWTProvider jwtProvider;

	public UsersFace(UserController controller, JWTProvider jwtProvider) {
		this.controller = controller;
		this.jwtProvider = jwtProvider;
	}

	public void registerByEmail(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			RegisterObject registerObject = ctx.bodyAsClass(RegisterObject.class);
			String ipAddr = ctx.ip();

			LoginUser user = controller.createDirectUser(registerObject, ipAddr);

			if (user == null) {
				throw new CodeException(ErrorCode.Register_Critical);
			}

			res.setData(new LoginResponse(user.getFirstname(), user.getLastname(),
					user.getUsername(), jwtProvider.generateToken(user.getToken())));
		} catch (CodeException ee) {
			res.setMessage(ee);
			res.setStatus(ResponseStatus.Error);
		} catch (JDBCException e) {
			// TODO Can this still happen?
			res.setStatus(ResponseStatus.Error);
		} catch (Exception e) {
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}

	public void login(@NotNull Context ctx) throws Exception {
		Response res = new Response();
		try {
			// Take login information from context
			LoginObject loginObject = ctx.bodyAsClass(LoginObject.class);
			// Take IP address from context
			String ipAddr = ctx.ip();

			// Load user from database with credentials
			LoginUser user = controller.login(loginObject, ipAddr);

			if (user == null) {
				throw new CodeException(ErrorCode.Login_Critical);
			}
			res.setData(new LoginResponse(user.getFirstname(), user.getLastname(),
					user.getUsername(), jwtProvider.generateToken(user.getToken())));
		} catch (CodeException ee) {
			res.setMessage(ee);
			res.setStatus(ResponseStatus.Error);
		} catch (BadRequestResponse e) {
			res.setMessage(ErrorCode.Login_NoCredentials);
			res.setStatus(ResponseStatus.Error);
		} catch (Exception e) {
			res.setMessage(ErrorCode.Login_Critical);
			res.setStatus(ResponseStatus.Error);
		}
		ctx.json(res);
	}
}