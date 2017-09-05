package de.muenchen.wollmux.conf.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * Interface des Administrationssservices. Wird in admin-service implementiert
 * und als Service registriert.
 *
 * @author daniel.sikeler
 *
 */
@ProxyGen
@VertxGen
public interface AdminService
{
  public static final String ADMIN_SERVICE = "admin";
  public static final String ADMIN_SERVICE_ADDRESS = "admin.service.";
  public static final String ADMIN_SERVICE_BASE_NAME = "admin-service-";

  /**
   * Liefert eine Datei der Konfiguration.
   */
  public void getFile(String file, Handler<AsyncResult<JsonObject>> resultHandler);

  /**
   * Schreibt einen Datei der Konfiguration auf die Platte.
   * @param file Der Dateiname relativ zur PATH Variable.
   * @param content Der Dateiinhalt.
   * @param resultHandler Der Actionhandler, der das Ergebnis entgegen nimmt.
   */
  public void writeFile(String file, String content, Handler<AsyncResult<JsonObject>> resultHandler);
}
