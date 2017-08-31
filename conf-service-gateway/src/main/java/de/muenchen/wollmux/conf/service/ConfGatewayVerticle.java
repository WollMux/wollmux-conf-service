package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.weld.vertx.web.WeldWebVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.LDAPProviderConstants;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;

/**
 * Startet einen HTTP-Server, über den die Kommunikation mit dem Konfigurations-
 * service abläuft. Beim Start des Servers werden auch die Routen für den
 * REST-Service registriert.
 *
 * Standardport 8080
 *
 * @author andor.ertsey
 *
 */
@Dependent
public class ConfGatewayVerticle extends AbstractVerticle
{
  static final String BASE_PATH = "/api/v1";

  @Inject
  private Logger log;

  @Override
  public void start(Future<Void> startFuture) throws Exception
  {
    log.info("ConfGatewayVerticle starting.");
  }

  @Override
  public void stop() throws Exception
  {
    log.info("ConfGatewayVerticle stopping.");
  }

  public static int getPort()
  {
    String value = System.getenv("ENV_CONFSERVICE_PORT");
    if (value == null)
    {
      value = System.getProperty("ENV_CONFSERVICE_PORT");
    }
    if (value == null)
    {
      value = "8080";
    }
    return Integer.parseInt(value);
  }

  public static String getIP()
  {
    String value = System.getenv("ENV_CONFSERVICE_IP");
    if (value == null)
    {
      value = System.getProperty("ENV_CONFSERVICE_IP");
    }
    if (value == null)
    {
      value = VertxOptions.DEFAULT_CLUSTER_HOST;
    }
    return value;
  }

  public static void main(String[] args)
  {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    System.setProperty("hazelcast.logging.type", "slf4j");

    Logger log = LoggerFactory.getLogger(ConfGatewayVerticle.class);
    VertxOptions vertxOptions = new VertxOptions();
    vertxOptions.setClustered(true);
    vertxOptions.setClusterHost(getIP());
    Vertx.clusteredVertx(vertxOptions, res ->
    {
      if (res.succeeded())
      {
        final Vertx vertx = res.result();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
          @Override
          public void run()
          {
            vertx.close();
          }
        });

        final WeldWebVerticle weldVerticle = new WeldWebVerticle();

        vertx.deployVerticle(weldVerticle, res1 ->
        {
          if (res1.succeeded())
          {
            JsonObject ldapConfig = new JsonObject();
            ldapConfig.put(LDAPProviderConstants.LDAP_USER_DN_TEMPLATE_FIELD,
                "uid={0},ou=users,ou=dir,o=lhm,c=de");
            ldapConfig.put(LDAPProviderConstants.LDAP_URL,
                "ldap://ldaptng.muenchen.de:389");
            ldapConfig.put(LDAPProviderConstants.LDAP_AUTHENTICATION_MECHANISM, "simple");
            ShiroAuthOptions authOptions = new ShiroAuthOptions()
                .setType(ShiroAuthRealmType.LDAP).setConfig(ldapConfig);
            AuthProvider authProvider = ShiroAuth.create(vertx, authOptions);

            Router router = Router.router(vertx);
            router.route().handler(CookieHandler.create());
            router.route().handler(BodyHandler.create());
            router.route()
                .handler(SessionHandler.create(LocalSessionStore.create(vertx))
                    .setCookieHttpOnlyFlag(true).setCookieSecureFlag(true));
            router.route().handler(UserSessionHandler.create(authProvider));
            router.route(BASE_PATH + "/" + AdminService.ADMIN_SERVICE + "/*")
              .handler(RedirectAuthHandler.create(authProvider, BASE_PATH + "/login/login.html"));
            router.route(BASE_PATH + "/login/loginhandler").handler(FormLoginHandler.create(authProvider));
            router.route(BASE_PATH + "/login/*").handler(StaticHandler.create());
            weldVerticle.registerRoutes(router);
            vertx.deployVerticle(weldVerticle.container().select(ConfGatewayVerticle.class).get());
            HttpServer server = vertx.createHttpServer();
            server.requestHandler(router::accept).listen(getPort(), res2 ->
            {
              if (res2.succeeded())
              {
                log.info("ConfGateway is listening on port " + res2.result().actualPort());
              } else
              {
                log.error("ConfGateway: Server could not be started.");
              }
            });
          } else
          {
            log.error("WeldVerticle not deployed.", res1.cause());
          }
        });
      }
    });
  }
}
