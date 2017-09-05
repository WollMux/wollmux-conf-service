package de.muenchen.wollmux.conf.service.beans;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

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
}
