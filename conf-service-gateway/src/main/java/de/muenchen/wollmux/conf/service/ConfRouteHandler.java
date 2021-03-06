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

    LinkedList<String> parts = new LinkedList<>(Arrays.asList(StringUtils.strip(path, "/").split("/")));

    if (parts.size() > 1)
    {
      String referat = parts.pop();
      String file = String.join("/", parts);

      String serviceName = ConfService.CONF_SERVICE_BASE_NAME + referat;
      ConfService cs = confServices.getService(serviceName);
      if (cs != null)
      {
        cs.getFile(file, res2 ->
        {
          if (res2.succeeded())
          {
            r.response().setChunked(true);
            r.response().putHeader("Cache-Control", "no-cache, no-store, must-revalidate");

            FileObject fo = new FileObject(res2.result());
            String contentType = "Content-Type";

            if (fo.getType().equals("conf"))
            {
              r.response().putHeader(contentType, "text/plain; charset=utf-8");
              stream(fo.getContent(), r.response());
            }
            else
            {
              if (fo.getType().equals("class"))
              {
                r.response().putHeader(contentType, "application/java");
              }
              else
              {
                r.response().putHeader(contentType, "application/octet-stream");
              }
              stream(fo.getContentAsBytes(), r.response());
            }
            r.response().setStatusCode(200);
            r.response().end();
          } else
          {
            log.error("Calling getFile failed.", res2.cause());

            confServices.validateProxy(serviceName);

            r.fail(404);
          }
        });
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
