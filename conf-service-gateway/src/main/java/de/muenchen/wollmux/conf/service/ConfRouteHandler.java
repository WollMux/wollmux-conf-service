package de.muenchen.wollmux.conf.service;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.weld.vertx.web.WebRoute;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.ext.web.RoutingContext;

@WebRoute(ConfGatewayVerticle.BASE_PATH + "/*")
public class ConfRouteHandler implements Handler<RoutingContext>
{
  @Inject
  private Logger log;

  @Inject
  private ServiceCache confServices;

  @Inject
  public ConfRouteHandler(Vertx vertx)
  {
  }

  @Override
  public void handle(RoutingContext r)
  {
    log.info("Request from " + r.request().remoteAddress() + ": " + r.request().rawMethod() + " " + r.request().uri());

    r.response().setChunked(true);

    String path = StringUtils.remove(r.request().uri(), ConfGatewayVerticle.BASE_PATH);

    String[] parts = StringUtils.strip(path, "/").split("/");

    if (parts.length == 2)
    {
      String referat = parts[0];
      String method = parts[1];

      String serviceName = ConfService.CONF_SERVICE_BASE_NAME + referat;
      ConfService cs = confServices.getService(serviceName);
      if (cs != null)
      {
        if (method.equals("conf"))
        {
          cs.getConf(res2 ->
          {
            if (res2.succeeded())
            {
              // TODO: Antwort muss gestreamt werden.
              r.response().setStatusCode(200).end(res2.result());
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
}
