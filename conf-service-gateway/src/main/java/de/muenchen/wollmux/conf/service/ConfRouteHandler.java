package de.muenchen.wollmux.conf.service;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedList;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.weld.vertx.web.WebRoute;

import de.muenchen.wollmux.conf.service.core.beans.Config;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
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
  private ConfServiceCache confServices;

  @Inject
  private AdminServiceCache adminServices;

  private int chunkSize;

  @Inject
  public ConfRouteHandler(@Config("chunk") int chunkSize)
  {
    this.chunkSize = chunkSize != 0 ? chunkSize : DEFAULT_CHUNK_SIZE;
  }

  @Override
  public void handle(RoutingContext context)
  {
    log.info("Request from " + context.request().remoteAddress() + ": " + context.request()
    	.rawMethod() + " " + context.request().uri());

    context.response().setChunked(true);
    String path = StringUtils.remove(context.request().path(),
        ConfGatewayVerticle.BASE_PATH);

    LinkedList<String> parts = new LinkedList<>(Arrays.asList(StringUtils.strip(path, "/").split("/")));

    if (parts.size() > 2)
    {
      String service = parts.pop();
      String referat = parts.pop();
      String file = String.join("/", parts);

      if(ConfService.CONF_SERVICE.equalsIgnoreCase(service))
      {
        String serviceName = ConfService.CONF_SERVICE_BASE_NAME + referat;
        handleConf(context, serviceName, file);
      } else if (AdminService.ADMIN_SERVICE.equalsIgnoreCase(service))
      {
        String serviceName = AdminService.ADMIN_SERVICE_BASE_NAME + referat;
        handleAdmin(context, serviceName, file);
      } else {
        context.fail(400);
      }
    } else
    {
      context.fail(400);
    }
  }

  private void handleAdmin(RoutingContext context, String serviceName, String file)
  {
    AdminService as = adminServices.getService(serviceName);
    if (as != null)
    {
      context.response().end("OK");
    } else
    {
      context.fail(502);
    }
  }

  private void handleConf(RoutingContext context, String serviceName, String file)
  {
    ConfService cs = confServices.getService(serviceName);
    if (cs != null)
    {
      cs.getFile(file, res2 ->
      {
        if (res2.succeeded())
        {
          context.response().setChunked(true);
          context.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate");

          FileObject fo = new FileObject(res2.result());
          String contentType = "Content-Type";

          if (fo.getType().equals("conf"))
          {
            context.response().putHeader(contentType, "text/plain; charset=utf-8");
            stream(fo.getContent(), context.response());
          }
          else
          {
            if (fo.getType().equals("class"))
            {
              context.response().putHeader(contentType, "application/java");
            }
            else
            {
              context.response().putHeader(contentType, "application/octet-stream");
            }
            stream(fo.getContentAsBytes(), context.response());
          }
          context.response().setStatusCode(200);
          context.response().end();
        } else
        {
          log.error("Calling getFile failed.", res2.cause());

          confServices.validateProxy(serviceName);

          context.fail(404);
        }
      });
    } else
    {
      context.fail(502);
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

    stream(content.getBytes(), response);
  }

  private void stream(byte[] content, HttpServerResponse response)
  {
    response.putHeader("Content-Length", String.valueOf(content.length));

    ByteArrayInputStream is = new ByteArrayInputStream(content);
    byte[] buf = new byte[chunkSize];

    while (is.available() > 0)
    {
      int size = is.read(buf, 0, chunkSize);
      if (size > 0)
      {
        response.write(Buffer.buffer(Arrays.copyOf(buf, size)));
      }
    }

  }
}
