package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.ApplicationScoped;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

@ApplicationScoped
public class AdminServiceImpl implements AdminService
{

  @Override
  public void getFile(String file,
      Handler<AsyncResult<JsonObject>> resultHandler)
  {
    // TODO Auto-generated method stub

  }

}
