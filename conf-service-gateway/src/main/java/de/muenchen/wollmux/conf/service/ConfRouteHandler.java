package de.muenchen.wollmux.conf.service;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.weld.vertx.web.WebRoute;

import de.muenchen.wollmux.conf.service.core.beans.Config;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.RoutingContext;

/**
 * Dies ist die Hauptroute zur Kommunikation mit dem Konfigurationsservice.
 * Der Request zum Aufruf der Route enthält die Name der Konfiguration
 * (bzw. des Referats) und das Format in dem die Konfiuration zurückgeliefert
 * werden soll.
 * Die Route ruft den {@link ConfService} aus dem {@link ServiceCache} abhängig
 * vom Namen auf.
 *
 * @author andor.ertsey
 *
 */
@WebRoute(ConfGatewayVerticle.BASE_PATH + "/*")
public class ConfRouteHandler implements Handler<RoutingContext>
{
  private static final int DEFAULT_CHUNK_SIZE = 1000;

  @Inject
  private Logger log;

  @Inject
  private ServiceCache confServices;

  private int chunkSize;

  @Inject
  public ConfRouteHandler(@Config("chunk") int chunkSize)
  {
    this.chunkSize = chunkSize != 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
  }

  @Override
  public void handle(RoutingContext r)
  {
    log.info("Request from " + r.request().remoteAddress() + ": " + r.request()
    	.rawMethod() + " " + r.request().uri());

    r.response().setChunked(true);
    String path = StringUtils.remove(r.request().path(),
        ConfGatewayVerticle.BASE_PATH);
    String product = r.request().getParam("product");

    String[] parts = StringUtils.strip(path, "/").split("/");

    if (parts.length == 2)
    {
      String referat = parts[0];
      String method = parts[1];

      String serviceName = ConfService.CONF_SERVICE_BASE_NAME + referat;
      ConfService cs = confServices.getService(serviceName);
      if (cs != null)
      {
        if ("conf".equals(method))
        {
          cs.getConf(product, res2 ->
          {
            if (res2.succeeded())
            {
              r.response().setChunked(true);
              r.response().putHeader("Content-Type", "text/plain; charset=utf-8");
              stream(res2.result(), r.response());
              r.response().setStatusCode(200);
              r.response().end();
            } else
            {
              log.error("Calling getConf failed.", res2.cause());

              confServices.validateProxy(serviceName);

              r.fail(500);
            }
          });
        } else
        {
          r.fail(400);
        }
      } else
      {
        r.fail(502);
      }
    } else
    {
      r.fail(400);
    }
  }

  /**
   * Schreibt eine Nachricht in kleinen Chunks als HTTP-Response.
   * @param content Die zu unterteilende Nachricht.
   * @param response Die HTTP-Response.
   */
  private void stream(String content, HttpServerResponse response)
  {
    if (chunkSize < 0)
    {
      response.write(content);
      return;
    }
    int start = 0;
    int end = start + chunkSize;
    while (start < content.length())
    {
      if (end > content.length())
      {
        end = content.length();
      }
      response.write(content.substring(start, end));
      start = end + 1;
      end += chunkSize;
    }
  }
}
