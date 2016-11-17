package de.muenchen.wollmux.conf.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ProxyGen
@VertxGen
public interface ConfService {
	public final static String CONF_SERVICE_ADDRESS = "conf.service.";
	public final static String CONF_SERVICE_BASE_NAME = "conf-service-";

	public void getConf(Handler<AsyncResult<String>> resultHandler);

	public void getJSON(Handler<AsyncResult<JsonObject>> resultHandler);
}
