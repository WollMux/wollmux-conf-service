package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class ConfServiceImpl implements ConfService
{

  @Override
  public void getConf(Handler<AsyncResult<String>> resultHandler)
  {
    resultHandler.handle(Future.succeededFuture("Hello World!"));
  }

  @Override
  public void getJSON(Handler<AsyncResult<JsonObject>> resultHandler)
  {
    // TODO Auto-generated method stub

  }

}
