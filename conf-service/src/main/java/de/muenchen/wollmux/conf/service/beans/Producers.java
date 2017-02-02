package de.muenchen.wollmux.conf.service.beans;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.main.Main;

import de.muenchen.wollmux.conf.service.camel.ConfRouteBuilder;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

@ApplicationScoped
public class Producers
{
  @Inject
  Logger log;

  @Produces
  public static Logger produceLogger(InjectionPoint injectionPoint)
  {
    return LoggerFactory.getLogger(injectionPoint.getMember()
        .getDeclaringClass());
  }

  @Produces
  @ApplicationScoped
  public Main getCamel(ConfRouteBuilder routeBuilder) throws Exception
  {
    try
    {
      Main main = new Main();
      main.addRouteBuilder(routeBuilder);
      main.start();
      return main;
    } catch (Exception e)
    {
      log.error(e.getMessage(), e);
      throw e;
    }
  }

  @Produces
  public CamelContext getContext(Main main)
  {
    return main.getCamelContexts().stream().findFirst().get();
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
