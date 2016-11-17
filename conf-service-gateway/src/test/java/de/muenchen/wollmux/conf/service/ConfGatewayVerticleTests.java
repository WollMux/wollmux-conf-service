package de.muenchen.wollmux.conf.service;

import org.jboss.weld.vertx.WeldVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class ConfGatewayVerticleTests
{
  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  private Vertx vertx;

  @Before
  public void setup(TestContext ctx)
  {
    vertx = rule.vertx();
    vertx.deployVerticle(new WeldVerticle(), res -> {
      
    });
  }

  @After
  public void close(TestContext context)
  {
    vertx.close(context.asyncAssertSuccess());
  }
}
