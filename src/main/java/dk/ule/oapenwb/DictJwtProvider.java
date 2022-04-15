// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.logic.users.LoginToken;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import lombok.Getter;

import java.nio.charset.StandardCharsets;

/**
 * Capsules the javalin-jwt JWTProvider so it can be managed by Guice.
 */
@Singleton
public class DictJwtProvider
{
	@Getter
	private final JWTProvider provider;

	@Inject
	public DictJwtProvider(AppConfig appConfig)
	{
		this.provider = createHMAC512(appConfig.getSecret()); // see also this custom method!
	}

	private JWTProvider createHMAC512(final String secret)
	{
		if (secret == null || secret.length() < 32) {
			throw new RuntimeException("The given secret must contain at least 32 characters.");
		}

		final byte[] byteArray = secret.getBytes(StandardCharsets.UTF_8);
		if (byteArray.length < 32) {
			throw new RuntimeException("The given secret must at least result in 32 bytes.");
		} else if (byteArray.length > 32) {
			throw new RuntimeException("The given secret is longer than 32 bytes which results in an unnessesary hashing.");
		}

		JWTGenerator<LoginToken> generator = (loginToken, alg) -> {
			JWTCreator.Builder token = JWT.create()
				.withClaim("id", loginToken.getId())
				.withClaim("role", loginToken.getRole().toString());
			return token.sign(alg);
		};

		// 256 bits make 32 bytes, so the secret should at least be 32 8-bit characters long
		Algorithm algorithm = Algorithm.HMAC256(byteArray);
		JWTVerifier verifier = JWT.require(algorithm).build();

		return new JWTProvider(algorithm, generator, verifier);
	}
}
