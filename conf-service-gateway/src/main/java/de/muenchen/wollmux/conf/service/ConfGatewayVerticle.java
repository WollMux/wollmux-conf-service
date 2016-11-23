package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.weld.vertx.web.WeldWebVerticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

@Dependent
public class ConfGatewayVerticle extends AbstractVerticle
{
  private static final int PORT = 8080;

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

  public static void main(String[] args)
  {
    Logger log = LoggerFactory.getLogger(ConfGatewayVerticle.class);
    Vertx.clusteredVertx(new VertxOptions(), res ->
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
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());
            weldVerticle.registerRoutes(router);
            vertx.deployVerticle(weldVerticle.container().select(ConfGatewayVerticle.class).get());
            HttpServer server = vertx.createHttpServer();
            server.requestHandler(router::accept).listen(PORT, res2 ->
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
