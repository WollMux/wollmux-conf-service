package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.muenchen.wollmux.conf.service.caching.ConfigCache;
import de.muenchen.wollmux.conf.service.exceptions.UnknownKonfigurationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

/**
 * Konkrete Implementation von {@link ConfService}. Liefert Konfigurationsdaten
 * in verschiedenen Formaten zurück. Inhaltlich sollten die Konfigurationen
 * identisch sein.
 *
 * @author andor.ertsey
 *
 */
@ApplicationScoped
public class ConfServiceImpl implements ConfService
{
  @Inject
  Logger log;

  @Inject
  ConfigCache configCache;

  @Inject
  private ConfigCache cache;

  /**
   * Liefert einen String im WollMux-Conf-Format.
   */
  @Override
  public void getConf(String product, Handler<AsyncResult<String>> resultHandler)
  {
    try
    {
      String type = "conf";
      String filename = getFilename(product, type);
      // TODO Müssen die Handler beendet werden?
      String config = configCache.getConfig(filename, res -> {
        if (res.succeeded())
        {
          resultHandler.handle(Future.succeededFuture(res.result()));
          cache.putConfig(filename, res.result());
        } else
        {
          resultHandler.handle(Future.failedFuture(res.cause()));
        }
      });
      // Konfiguration kommt aus dem Cache
      if (config != null)
      {
        resultHandler.handle(Future.succeededFuture(config));
      }
    } catch (UnknownKonfigurationException e)
    {
      resultHandler.handle(Future.failedFuture(e));
    }
  }

  /**
   * Liefert einen String im JSON-Format.
   */
  @Override
  public void getJSON(String filename,
      Handler<AsyncResult<JsonObject>> resultHandler)
  {
    // TODO Auto-generated method stub

  }

  /**
   * Wählt die Basisdatei für die Konfiguration je nach Produkt aus.
   *
   * @param product
   *          Das Produkt.
   * @param type
   *          Soll der Dateiname für das Conf- oder JSON-Format geliefert
   *          werden.
   * @return Ein String der den include für die Basisdatei enthält.
   * @throws UnknownKonfigurationException
   *           Ein unbekanntes Produkt wird abgefragt.
   */
  private String getFilename(String product, String type)
      throws UnknownKonfigurationException
  {
    String template = "%%include \"%s.%s\"";
    switch (product.toLowerCase())
    {
    case "wollmux":
      return String.format(template, "main", type);
    case "wollmuxbar":
      return String.format(template, "main", type);
    case "seriendruck":
      return String.format(template, "main", type);
    default:
      throw new UnknownKonfigurationException(
          String.format("Für das Produkt %s gibt es keine Konfiguration.", product));
    }
  }
}
