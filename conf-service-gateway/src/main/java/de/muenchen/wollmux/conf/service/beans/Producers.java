package de.muenchen.wollmux.conf.service.beans;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.ServiceDiscovery;

@Dependent
public class Producers
{
  @Produces
  public Logger produceLogger(InjectionPoint injectionPoint)
  {
    return LoggerFactory.getLogger(injectionPoint.getMember()
	.getDeclaringClass());
  }
  
  @Produces
  public ServiceDiscovery getServiceDiscovery(Vertx vertx)
  {
    return ServiceDiscovery.create(vertx);
  }
}
