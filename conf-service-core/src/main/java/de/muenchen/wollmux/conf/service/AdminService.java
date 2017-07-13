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
public interface AdminService
{
  public static final String ADMIN_SERVICE = "ADMIN";
  public static final String ADMIN_SERVICE_ADDRESS = "admin.service.";
  public static final String ADMIN_SERVICE_BASE_NAME = "admin-service-";

  /**
   * Liefert eine Datei der Konfiguration.
   */
  public void getFile(String file, Handler<AsyncResult<JsonObject>> resultHandler);
}
