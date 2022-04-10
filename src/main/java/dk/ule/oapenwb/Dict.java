// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only
package dk.ule.oapenwb;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.RunMode;
import dk.ule.oapenwb.base.Views;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.DataInitializer;
import dk.ule.oapenwb.data.importer.FileImporter;
import dk.ule.oapenwb.data.importer.ImportConfig;
import dk.ule.oapenwb.data.importer.ImportResult;
import dk.ule.oapenwb.data.importer.sheet.SheetConfig;
import dk.ule.oapenwb.data.importer.sheet.SheetFileImporter;
import dk.ule.oapenwb.data.importer.sheet.SheetResult;
import dk.ule.oapenwb.entity.basis.RoleType;
import dk.ule.oapenwb.logic.users.LoginToken;
import dk.ule.oapenwb.util.CurrentUser;
import dk.ule.oapenwb.util.EmailUtil;
import dk.ule.oapenwb.util.HibernateUtil;
import io.javalin.Javalin;
import io.javalin.core.security.RouteRole;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.plugin.json.JavalinJackson;
import javalinjwt.JWTAccessManager;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import javalinjwt.JavalinJWT;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * This is oapen wöördebook's main class.
 */
public class Dict
{
	private static final Logger LOG = LoggerFactory.getLogger(Dict.class);

	// 8 character version info, e.g. 01.03.05, or SNAP00.01 for 00.01 snapshots on development branch
	public static final String VERSION = "SNAP00.01";

	// oapenwb's entry point
	public static void main(String[] args) throws IOException, CodeException
	{
		RunMode runMode = RunMode.Normal;

		if (args.length > 0) {
			switch (args[0]) {
				case "dev" -> runMode = RunMode.Development;
				case "test" -> runMode = RunMode.Testing;
				default -> {
					System.out.println("Supported first argument values are:");
					System.out.println("  'dev' for using development configuration (config.dev.json)");
					System.out.println("  'test' for using testing configuration (config.test.json)");
					System.out.println("If no value is given the normal (productive) configuration will be used " +
						"(config.json).");
					System.exit(1);
				}
			}
		}

		Dict dict = new Dict();
		dict.setRunMode(runMode);
		dict.run();
	}

	@Getter @Setter
	private RunMode runMode = RunMode.Normal;

	public void run() throws IOException, CodeException
	{
		// Create a customized ObjectMapper for Jackson
		final ObjectMapper objMapper =  new ObjectMapper(); // JavalinJackson.Companion.defaultMapper();
		objMapper.setConfig(objMapper.getSerializationConfig().withView(Views.REST.class));
		objMapper.registerModule(new JavaTimeModule());
		objMapper.registerModule(new Jdk8Module());
		objMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		//objMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);

		// Load the application's configuration depending on the RunMode via the just created ObjectMapper
		final String configFile =
			  getRunMode() == RunMode.Normal ? "config.json"
			: getRunMode() == RunMode.Development ? "config.dev.json"
			: "config.testing.json";
		AppConfig appConfig = objMapper.readValue(
			Files.readString(Paths.get(configFile)),
			AppConfig.class);

		// Initialize email configuration if needed
		if (appConfig.isSendEmails()) {
			EmailUtil.configureEmail(appConfig.getEmailConfig());
		}

		// Initializes database if necessary as well as Hibernate
		DataInitializer dataInit = new DataInitializer(getRunMode(), appConfig);
		dataInit.run();

		// For more info on Javalin-JWT see: https://github.com/kmehrunes/javalin-jwt
		// Initialize Javalin JWT and the AccessManager
		JWTProvider provider = Dict.createHMAC512(appConfig.getSecret()); // see also this custom method!
		Map<String, RouteRole> rolesMapping = new HashMap<>() {{
			// Add new roles here!
			put(RoleType.Anyone.toString(), RoleType.Anyone);
			put(RoleType.User.toString(), RoleType.User);
			put(RoleType.Moderator.toString(), RoleType.Moderator);
			put(RoleType.Editor.toString(), RoleType.Editor);
			put(RoleType.Admin.toString(), RoleType.Admin);
		}};
		// TODO ENHANCE / TODO SECURITY
		//   JWT tokens do not expire yet, maybe should be stored in cookie
		//   https://gist.github.com/soulmachine/b368ce7292ddd7f91c15accccc02b8df
		JWTAccessManager accessManager = new JWTAccessManager("role", rolesMapping, RoleType.Anyone);

		// Create the controllers for the API
		DictControllers controllers = new DictControllers(appConfig);

		// Create the Javalin faces to the controllers
		DictFaces faces = new DictFaces(controllers, provider);

		// Create the controllers for the admin section
		AdminControllers adminControllers = new AdminControllers();

		// Create the Javalin faces for the admin section controllers
		AdminFaces adminFaces = new AdminFaces(adminControllers, controllers);

		// Create and setup the Javalin instance
		Javalin app = Javalin.create(config -> {
			//config.enableRouteOverview("overview", role);
			config.accessManager(accessManager);
			config.showJavalinBanner = false;
			//config.registerPlugin(new MicrometerPlugin());
			config.server(() -> new Server(new QueuedThreadPool(appConfig.getMaxThreads(), appConfig.getMinThreads(),
				appConfig.getTimeOutMillis())));
			// TODO ENHANCE: En heavel in de konfig anleggen?
			//   config.enableDevLogging();
			config.enableCorsForOrigin(appConfig.getAllowedOrigins());
			// Pass the customly configured object mapper over to Javalin
			config.jsonMapper(new JavalinJackson(objMapper));
		}).start(appConfig.getPort());

		// Create predefined role sets
		RoleType[] anyoneRole = { RoleType.Anyone };
		RoleType[] adminRole = { RoleType.Admin };
		RoleType[] adminAndModeratorRole = { RoleType.Moderator, RoleType.Admin };
		RoleType[] adminAndEditor = { RoleType.Editor, RoleType.Admin };
		RoleType[] allRoles = { RoleType.Anyone, RoleType.User, RoleType.Moderator, RoleType.Editor, RoleType.Admin };

		/*
		 * A decode handler which captures the value of a JWT from an
		 * authorization header in the form of "Bearer {jwt}". The handler
		 * decodes and verifies the JWT then puts the decoded object as
		 * a context attribute for future handlers to access directly.
		 */
		Handler decodeHandler = JavalinJWT.createHeaderDecodeHandler(provider);

		/* TODO Websocket handler for locking. For later use.
		app.ws("locks", ws -> {
			ws.onConnect(adminControllers.getLockController()::onConnect);
			ws.onClose(adminControllers.getLockController()::onClose);
			ws.onMessage(adminControllers.getLockController()::onMessage);
			ws.onError(adminControllers.getLockController()::handleError);
		});

		app.ws("/websocket", ws -> {
			ws.onConnect(ctx -> System.out.println("Connected"));
			ws.onMessage(ctx -> {
				ctx.send(ctx.message()); // convert to json and send back
			});
			ws.onBinaryMessage(ctx -> System.out.println("Message"));
			ws.onClose(ctx -> System.out.println("Closed"));
			ws.onError(ctx -> System.out.println("Errored"));
		}, anyoneRole);
		 */

		// Configure the routes, connect the Javalin faces
		app.routes(() -> {

			post("list", faces.getSearch()::executeQuery, allRoles);

			path("l10n", () -> {
				get("__reload__", faces.getL10n()::reloadTranslations, allRoles);
				get("{locale}", faces.getL10n()::getTranslations, allRoles);
				get("{scope}/{locale}", faces.getL10n()::getTranslationsByScope, allRoles);
			});

			path("config", () -> {
				get("base", faces.getConfig()::getBaseConfig, allRoles);
				get("__reload__", faces.getConfig()::reloadConfig, allRoles);
			});

			path("users", () -> {
				before((ctx) -> controllers.getViolations().checkForBan(ctx.ip()));

				post("login", faces.getUsers()::login, allRoles);
				post("register", faces.getUsers()::registerByEmail, allRoles);
			});

			/* TODO Websocket handler for locking. For later use.
			path("locks", () -> {
				ws(ws -> {
					ws.onConnect(adminControllers.getLockController()::onConnect);
					ws.onClose(adminControllers.getLockController()::onClose);
					ws.onMessage(adminControllers.getLockController()::onMessage);
					ws.onError(adminControllers.getLockController()::handleError);
				}, adminAndEditor);
			});
			 */

			path("admin", () -> {
				before(decodeHandler);
				before((context) -> {
					if ("OPTIONS".equals(context.req.getMethod())) {
						// The user ID extraction must not be done for requests of type OPTIONS
						// because no Token will be set in the request's header and therefore it would fail.
						return;
					}
					// Extract the user ID from the JWT set on the CurrentUser instance
					try {
						DecodedJWT decodedJWT = JavalinJWT.getDecodedFromContext(context);
						CurrentUser.INSTANCE.logIn(decodedJWT.getClaim("id").asInt());
						if (LOG.isDebugEnabled()) {
							LOG.debug("Call by user with id: " + CurrentUser.INSTANCE.get());
						}
					} catch (InternalServerErrorResponse e) {
						LOG.error("Error in before-handler of path '/admin': {}", e.getMessage());
					}
				});
				// TODO Add second before-Handler to check roles and permissions and see what happens

				path("uiLanguage", () -> {
					post(adminFaces.getUiLanguagesFace()::create, adminRole);
					get(adminFaces.getUiLanguagesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getUiLanguagesFace()::get, adminAndEditor);
						put(adminFaces.getUiLanguagesFace()::update, adminRole);
						delete(adminFaces.getUiLanguagesFace()::delete, adminRole);
					});
				});

				path("uiScope", () -> {
					post(adminFaces.getUiScopesFace()::create, adminRole);
					get(adminFaces.getUiScopesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getUiScopesFace()::get, adminAndEditor);
						put(adminFaces.getUiScopesFace()::update, adminRole);
						delete(adminFaces.getUiScopesFace()::delete, adminRole);
					});
				});

				path("uiTranslation", () -> {
					post(adminFaces.getUiTranslationsFace()::create, adminRole);
					get(adminFaces.getUiTranslationsFace()::list, adminAndEditor);
					path("{scope}/{uitID}", () -> {
						get(adminFaces.getUiTranslationsFace()::get, adminAndEditor);
						put(adminFaces.getUiTranslationsFace()::update, adminRole);
						delete(adminFaces.getUiTranslationsFace()::delete, adminRole);
					});
				});

				path("uiResultCategory", () -> {
					post(adminFaces.getUiResultCategoriesFace()::create, adminRole);
					get(adminFaces.getUiResultCategoriesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getUiResultCategoriesFace()::get, adminAndEditor);
						put(adminFaces.getUiResultCategoriesFace()::update, adminRole);
						delete(adminFaces.getUiResultCategoriesFace()::delete, adminRole);
					});
				});

				path("orthography", () -> {
					post(adminFaces.getOrthographiesFace()::create, adminRole);
					get(adminFaces.getOrthographiesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getOrthographiesFace()::get, adminAndEditor);
						put(adminFaces.getOrthographiesFace()::update, adminRole);
						delete(adminFaces.getOrthographiesFace()::delete, adminRole);
					});
				});

				path("loMapping", () -> {
					post(adminFaces.getLoMappingsFace()::create, adminRole);
					get(adminFaces.getLoMappingsFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLoMappingsFace()::get, adminAndEditor);
						put(adminFaces.getLoMappingsFace()::update, adminRole);
						delete(adminFaces.getLoMappingsFace()::delete, adminRole);
					});
				});

				path("lang", () -> {
					post(adminFaces.getLanguagesFace()::create, adminRole);
					get(adminFaces.getLanguagesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLanguagesFace()::get, adminAndEditor);
						put(adminFaces.getLanguagesFace()::update, adminRole);
						delete(adminFaces.getLanguagesFace()::delete, adminRole);
					});
				});

				path("langPair", () -> {
					post(adminFaces.getLangPairsFace()::create, adminRole);
					get(adminFaces.getLangPairsFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLangPairsFace()::get, adminAndEditor);
						put(adminFaces.getLangPairsFace()::update, adminRole);
						delete(adminFaces.getLangPairsFace()::delete, adminRole);
					});
				});

				path("lexemeType", () -> {
					post(adminFaces.getLexemeTypesFace()::create, adminRole);
					get(adminFaces.getLexemeTypesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLexemeTypesFace()::get, adminAndEditor);
						put(adminFaces.getLexemeTypesFace()::update, adminRole);
						delete(adminFaces.getLexemeTypesFace()::delete, adminRole);
					});
				});

				path("lexemeFormType", () -> {
					post(adminFaces.getLexemeFormTypesFace()::create, adminRole);
					get(adminFaces.getLexemeFormTypesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLexemeFormTypesFace()::get, adminAndEditor);
						put(adminFaces.getLexemeFormTypesFace()::update, adminRole);
						delete(adminFaces.getLexemeFormTypesFace()::delete, adminRole);
					});
				});

				path("tlConfigs", () -> {
					post(adminFaces.getTlConfigsFace()::create, adminRole);
					get(adminFaces.getTlConfigsFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getTlConfigsFace()::get, adminAndEditor);
						put(adminFaces.getTlConfigsFace()::update, adminRole);
						delete(adminFaces.getTlConfigsFace()::delete, adminRole);
					});
				});

				path("lemmaTemplates", () -> {
					post(adminFaces.getLemmaTemplatesFace()::create, adminRole);
					get(adminFaces.getLemmaTemplatesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLemmaTemplatesFace()::get, adminAndEditor);
						put(adminFaces.getLemmaTemplatesFace()::update, adminRole);
						delete(adminFaces.getLemmaTemplatesFace()::delete, adminRole);
					});
				});

				path("categories", () -> {
					post(adminFaces.getCategoriesFace()::create, adminRole);
					get(adminFaces.getCategoriesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getCategoriesFace()::get, adminAndEditor);
						put(adminFaces.getCategoriesFace()::update, adminRole);
						delete(adminFaces.getCategoriesFace()::delete, adminRole);
					});
				});

				path("unitLevels", () -> {
					post(adminFaces.getUnitLevelsFace()::create, adminRole);
					get(adminFaces.getUnitLevelsFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getUnitLevelsFace()::get, adminAndEditor);
						put(adminFaces.getUnitLevelsFace()::update, adminRole);
						delete(adminFaces.getUnitLevelsFace()::delete, adminRole);
					});
				});

				path("linkTypes", () -> {
					post(adminFaces.getLinkTypesFace()::create, adminRole);
					get(adminFaces.getLinkTypesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLinkTypesFace()::get, adminAndEditor);
						put(adminFaces.getLinkTypesFace()::update, adminRole);
						delete(adminFaces.getLinkTypesFace()::delete, adminRole);
					});
				});

				path("tags", () -> {
					post(adminFaces.getTagsFace()::create, adminRole);
					get(adminFaces.getTagsFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getTagsFace()::get, adminAndEditor);
						put(adminFaces.getTagsFace()::update, adminRole);
						delete(adminFaces.getTagsFace()::delete, adminRole);
					});
				});

				path("synGroups", () -> {
					post(adminFaces.getSynGroupsFace()::create, adminRole);
					get(adminFaces.getSynGroupsFace()::list, adminAndEditor);
					// attention: patch here will do a find for a given SGSearchRequest
					patch(adminFaces.getSynGroupsFace()::find, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getSynGroupsFace()::get, adminAndEditor);
						put(adminFaces.getSynGroupsFace()::update, adminRole);
						delete(adminFaces.getSynGroupsFace()::delete, adminRole);
					});
				});

				path("sememes", () -> {
					// attention: patch here will do a find for a given SSearchRequest
					patch(adminFaces.getSememesFace()::find, adminAndEditor);
					path("slim/{id}", () -> get(adminFaces.getSememesFace()::getSlim, adminAndEditor));
				});

				path("lexemes", () -> {
					post(adminFaces.getLexemesFace()::create, adminAndEditor);
					// attention: 'patch' instead of 'get' is used for the search because we want to submit
					// search data in the request's body
					patch(adminFaces.getLexemesFace()::list, adminAndEditor);
					path("{id}", () -> {
						get(adminFaces.getLexemesFace()::get, adminAndEditor);
						put(adminFaces.getLexemesFace()::update, adminAndEditor);
						delete(adminFaces.getLexemesFace()::delete, adminAndEditor);
					});
					path("slim/{id}", () -> get(adminFaces.getLexemesFace()::getSlim, adminAndEditor));
				});

				path("__import", () -> {
					post(ctx -> {
						final ImportConfig cfg = ctx.bodyAsClass(ImportConfig.class);
						final FileImporter importer = new FileImporter(cfg, adminControllers);
						ImportResult result = importer.run();
						ctx.json(result);
					}, adminRole);
					path("sheet", () -> post(ctx -> {
						final SheetConfig cfg = ctx.bodyAsClass(SheetConfig.class);
						final SheetFileImporter importer = new SheetFileImporter(cfg, adminControllers);
						SheetResult result = importer.run();
						ctx.json(result);
					}, adminRole));
				});
			});

			after("*", (ctx) -> {
				HibernateUtil.closeSession();
				CurrentUser.INSTANCE.logOut();
			});
		});

		try {
			controllers.getL10n().loadTranslations();
			controllers.getConfig().loadConfig();
		} catch (Exception e) {
			LOG.error("Could not preload L10Ns and/or config", e);
		} finally {
			HibernateUtil.closeSession();
		}
		LOG.debug("End of run()");
	}

	static JWTProvider createHMAC512(final String secret) {
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