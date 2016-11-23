package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Konkrete Implementation von {@link ConfService}. Liefert Konfigurationsdaten
 * in verschiedenen Formaten zurück.
 * Inhaltlich sollten die Konfigurationen identisch sein.
 * 
 * @author andor.ertsey
 *
 */
@ApplicationScoped
public class ConfServiceImpl implements ConfService
{

  /**
   * Liefert einen String im WollMux-Conf-Format.
   */
  @Override
  public void getConf(Handler<AsyncResult<String>> resultHandler)
  {
    resultHandler.handle(Future.succeededFuture("Hello World!"));
  }

  /**
   * Liefert einen String im JSON-Format.
   */
  @Override
  public void getJSON(Handler<AsyncResult<JsonObject>> resultHandler)
  {
    // TODO Auto-generated method stub

  }

}
