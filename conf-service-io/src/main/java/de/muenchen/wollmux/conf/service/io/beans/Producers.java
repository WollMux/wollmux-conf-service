package de.muenchen.wollmux.conf.service.io.beans;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;

import de.muenchen.wollmux.conf.service.io.camel.ConfRouteBuilder;
import io.vertx.core.logging.Logger;

@ApplicationScoped
public class Producers
{
  @Inject
  Logger log;

  @Produces
  @ApplicationScoped
  public Main getCamel() throws Exception
  {
    try
    {
      Main main = new Main();
      main.start();
      return main;
    } catch (Exception e)
    {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @Produces
  @ApplicationScoped
  public CamelContext getContext(ConfRouteBuilder routeBuilder) throws Exception
  {
    CamelContext ctx = new DefaultCamelContext();
    ctx.addRoutes(routeBuilder);
    ctx.start();
    return ctx;
  }

  @Produces
  ProducerTemplate getProducerTemplate(CamelContext ctx)
  {
    return ctx.createProducerTemplate();
  }

  @Produces WatchService getWatchService() throws IOException
  {
    return FileSystems.getDefault().newWatchService();
  }
}
