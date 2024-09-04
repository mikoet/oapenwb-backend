// SPDX-FileCopyrightText: © 2022 Michael Köther <mkoether38@gmail.com>
// SPDX-License-Identifier: AGPL-3.0-only

package dk.ule.oapenwb;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import dk.ule.oapenwb.base.AppConfig;
import dk.ule.oapenwb.base.RunMode;
import dk.ule.oapenwb.base.error.CodeException;
import dk.ule.oapenwb.data.DataInitializer;
import dk.ule.oapenwb.data.importer.FileImporter;
import dk.ule.oapenwb.data.importer.ImportConfig;
import dk.ule.oapenwb.data.importer.ImportResult;
import dk.ule.oapenwb.data.importer.csv.CheckType;
import dk.ule.oapenwb.data.importer.csv.CsvImporterConfig;
import dk.ule.oapenwb.data.importer.csv.CsvRowBasedImporter;
import dk.ule.oapenwb.data.importer.csv.setting.SaxonFirstImportSetting;
import dk.ule.oapenwb.entity.basis.RoleType;
import dk.ule.oapenwb.logic.users.LoginToken;
import dk.ule.oapenwb.persistency.entity.Views;
import dk.ule.oapenwb.rpc.DictSpring;
import dk.ule.oapenwb.util.CurrentUser;
import dk.ule.oapenwb.util.EmailUtil;
import dk.ule.oapenwb.util.HibernateUtil;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.json.JavalinJackson;
import io.javalin.security.RouteRole;
import javalinjwt.JWTAccessManager;
import javalinjwt.JWTProvider;
import javalinjwt.JavalinJWT;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static io.javalin.apibuilder.ApiBuilder.*;

/**
 * This is oapen wöördebook's main class.
 */
public class Dict
{
	private static final Logger LOG = LoggerFactory.getLogger(Dict.class);

	// 8 character version info, e.g. 01.03.05, or SNAP00.01 for 00.01 snapshots on development branch
	public static final String VERSION = "SNAP00.01";

	private Integer alive = 0;

	// oapenwb's entry point
	public static void main(String[] args) throws IOException, CodeException
	{
		RunMode runMode = RunMode.Normal;

		boolean error = false;
		if (args.length > 0) {
			switch (args[0]) {
				case "dev" -> runMode = RunMode.Development;
				case "test" -> runMode = RunMode.Testing;
				default -> error = true;
			}
		}

		if (error) {
			System.out.println("Supported first argument values are:");
			System.out.println("  'dev' for using development configuration (config.dev.json)");
			System.out.println("  'test' for using testing configuration (config.test.json)");
			System.out.println("If no value is given the normal (productive) configuration will be used " +
								   "(config.json).");
			System.exit(1);
		}

		Dict dict = new Dict();
		dict.setRunMode(runMode);
		dict.run();
	}

	@Getter @Setter
	private RunMode runMode = RunMode.Normal;

	public void run() throws IOException, CodeException
	{
		LOG.info("Starting service oapenwb-javalin.");

		// Create a customized ObjectMapper for Jackson
		final ObjectMapper objMapper =  new ObjectMapper(); // JavalinJackson.Companion.defaultMapper();
		objMapper.setConfig(objMapper.getSerializationConfig().withView(Views.REST.class));
		objMapper.registerModule(new JavaTimeModule());
		objMapper.registerModule(new Jdk8Module());
		objMapper.configure(MapperFeature.PROPAGATE_TRANSIENT_MARKER, true);
		//objMapper.disable(MapperFeature.DEFAULT_VIEW_INCLUSION);
		
		// Setup dependency injection
		final Injector injector = Guice.createInjector(new DictModule());

		// Load the application's configuration depending on the RunMode via the just created ObjectMapper
		final AppConfig appConfig = injector.getInstance(AppConfig.class);
		final String configFile =
			  getRunMode() == RunMode.Normal ? "config.json"
			: getRunMode() == RunMode.Development ? "config.dev.json"
			: "config.testing.json";
		// Update appConfig instance with file content
		objMapper.readerForUpdating(appConfig).readValue(Files.readString(Paths.get(configFile)));

		// Check for RPC host (oapenwb-spring) health before continuing
		final DictSpring dictSpring = injector.getInstance(DictSpring.class);
		if (!dictSpring.isHealthy()) {
			int checkCount = 0;
			do {
				if (checkCount % 300 == 0) {
					LOG.info("RPC host (oapenwb-spring) is not healthy.");
				}
				checkCount++;

				try {
					Thread.sleep(1_000);
				} catch (InterruptedException e) {
					LOG.info("Couldn't sleep ;).", e);
				}
			} while (!dictSpring.isHealthy());
		}
		LOG.info("RPC host (oapenwb-spring) is healthy.");

		// Initialize email configuration if needed
		if (appConfig.isSendEmails()) {
			EmailUtil.configureEmail(appConfig.getEmailConfig());
		}

		// TODO Make DataInitializer disengageable, or move data initialization into oapenwb-spring.
		// Initializes database if necessary as well as Hibernate
		final DataInitializer dataInit = new DataInitializer(getRunMode(), appConfig);
		dataInit.run();

		// For more info on Javalin-JWT see: https://github.com/kmehrunes/javalin-jwt
		// Initialize Javalin JWT and the AccessManager
		final JWTProvider<LoginToken> provider = injector.getInstance(DictJwtProvider.class).getProvider();
		final Map<String, RouteRole> rolesMapping = new HashMap<>() {{
			// Add new roles here!
			put(RoleType.Anyone.toString(), RoleType.Anyone);
			put(RoleType.User.toString(), RoleType.User);
			put(RoleType.Moderator.toString(), RoleType.Moderator);
			put(RoleType.Editor.toString(), RoleType.Editor);
			put(RoleType.Admin.toString(), RoleType.Admin);
		}};

		// Create predefined role sets
		final RoleType[] anyoneRole = { RoleType.Anyone };
		final RoleType[] adminRole = { RoleType.Admin };
		final RoleType[] adminAndModeratorRole = { RoleType.Moderator, RoleType.Admin };
		final RoleType[] adminAndEditor = { RoleType.Editor, RoleType.Admin };
		final RoleType[] allRoles = { RoleType.Anyone, RoleType.User, RoleType.Moderator, RoleType.Editor, RoleType.Admin };

		// TODO ENHANCE / TODO SECURITY
		//   JWT tokens do not expire yet, maybe should be stored in cookie
		//   https://gist.github.com/soulmachine/b368ce7292ddd7f91c15accccc02b8df
		final JWTAccessManager accessManager = new JWTAccessManager("role", rolesMapping, RoleType.Anyone);

		/*
		 * A decode handler which captures the value of a JWT from an
		 * authorization header in the form of "Bearer {jwt}". The handler
		 * decodes and verifies the JWT then puts the decoded object as
		 * a context attribute for future handlers to access directly.
		 */
		Handler decodeHandler = JavalinJWT.createHeaderDecodeHandler(provider);

		// Create the controllers for the API
		final DictControllers dictControllers = injector.getInstance(DictControllers.class);

		// Create the Javalin faces to the controllers
		final DictFaces faces = injector.getInstance(DictFaces.class);

		// Create the controllers for the admin section
		final AdminControllers adminControllers = injector.getInstance(AdminControllers.class);

		// Create the Javalin faces for the admin section controllers
		final AdminFaces adminFaces = injector.getInstance(AdminFaces.class);

		// Create and setup the Javalin instance
		final Javalin app = Javalin.create(config -> {
			config.showJavalinBanner = false;
			config.useVirtualThreads = true;

			config.jetty.threadPool = new QueuedThreadPool(appConfig.getMaxThreads(), appConfig.getMinThreads(),
				appConfig.getTimeOutMillis());

			// TODO ENHANCE: En heavel in de konfig anleggen?
			//   config.enableDevLogging();

			config.bundledPlugins.enableCors(cors -> {
				cors.addRule(corsConfig -> {
					final var allowedOrigins = appConfig.getAllowedOrigins();
					if (allowedOrigins.length == 1) {
						corsConfig.allowHost(
							appConfig.getAllowedOrigins()[0]
						);
					} else if (allowedOrigins.length > 1) {
						corsConfig.allowHost(
							appConfig.getAllowedOrigins()[0],
							Arrays.copyOfRange(appConfig.getAllowedOrigins(), 1, appConfig.getAllowedOrigins().length)
						);
					}
				});
			});

			// Pass the customly configured object mapper over to Javalin, and enable use of virtual threads
			config.jsonMapper(new JavalinJackson(objMapper, true));

			// Configure the routes, connect the Javalin faces
			config.router.apiBuilder(() -> {
				get("alive", (@NotNull Context ctx) -> {
					synchronized (this) {
						ctx.result("" + this.alive);
					}
				}, allRoles);

				post("searchResults", faces.getSearch()::executeQuery, allRoles);

				post("autocompletions", faces.getAutocomplete()::executeQuery, allRoles);

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
					before((ctx) -> dictControllers.getViolations().checkForBan(ctx.ip()));

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
						if ("OPTIONS".equals(context.req().getMethod())) {
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

						path("saxonFI", () -> get(ctx -> {
							SaxonFirstImportSetting setting = new SaxonFirstImportSetting(adminControllers);
							CsvImporterConfig cfg = setting.getConfig();

							//cfg.setFilename("220619_1245_importliste_test.tsv");
							cfg.setFilename("220716_2215_importliste_test.tsv");
							cfg.setCheckType(CheckType.EverythingBeforeImport);
							//cfg.setSimulate(true);

							CsvRowBasedImporter importer = new CsvRowBasedImporter(appConfig, adminControllers, cfg);
							importer.run();

						}, adminRole));
					});
				});

				after("*", (ctx) -> {
					HibernateUtil.closeSession();
					CurrentUser.INSTANCE.logOut();
				});
			});
		});

		// Set a shutdown hook to know when the application was terminated
		Thread shutdownHook = new Thread(() -> shutdownHook(app, dictSpring));
		Runtime.getRuntime().addShutdownHook(shutdownHook);

		// Set the JWTAccessManager
		app.beforeMatched(accessManager);

		// Set 10s timer for alive route
		Timer timer = new Timer();
		timer.schedule(wrap(() -> {
			synchronized (this) {
				this.alive = 1;
			}
		}), 10_000L);

		// Start the Javalin server
		app.start(appConfig.getPort());

		try {
			dictControllers.getL10n().loadTranslations();
			dictControllers.getConfig().loadConfig();
		} catch (Exception e) {
			LOG.error("Could not preload L10Ns and/or config", e);
		} finally {
			HibernateUtil.closeSession();
		}
		LOG.debug("End of run()");
	}

	/**
	 * Shutdown hook to know (e.g. on log files) when the app was shut down.
	 * 
	 * @param app the Javalin app instance
	 */
	private void shutdownHook(final Javalin app, final DictSpring dictSpring)
	{
		try {
			LOG.info("Shutting down application…");
			app.stop();
			Thread.sleep(250);
			dictSpring.shutdown();
			Thread.sleep(1750);
			LOG.info("Done.");
		} catch (InterruptedException e) {
			LOG.error("Failed", e);
		}
	}

	private static TimerTask wrap(Runnable r)
	{
		return new TimerTask() {
			@Override
			public void run() {
				r.run();
			}
		};
	}
}
