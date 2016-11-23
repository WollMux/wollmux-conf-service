package de.muenchen.wollmux.conf.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Interface des Konfigurationsservice. Wird in conf-service implementiert
 * und als Service registriert.
 * 
 * @author andor.ertsey
 *
 */
@ProxyGen
@VertxGen
public interface ConfService
{
  public final static String CONF_SERVICE_ADDRESS = "conf.service.";
  public final static String CONF_SERVICE_BASE_NAME = "conf-service-";

  /**
   * Liefert einen String im WollMux-Conf-Format.
   */
  public void getConf(Handler<AsyncResult<String>> resultHandler);

  /**
   * Liefert einen String im JSON-Format.
   */
  public void getJSON(Handler<AsyncResult<JsonObject>> resultHandler);
}
