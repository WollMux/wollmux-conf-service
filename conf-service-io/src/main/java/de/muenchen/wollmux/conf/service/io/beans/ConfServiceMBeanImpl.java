package de.muenchen.wollmux.conf.service.io.beans;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.camel.CamelContext;

import io.vertx.core.logging.Logger;

@Dependent
public class ConfServiceMBeanImpl implements ConfServiceMXBean
{
  @Inject
  private Logger log;

  @Inject
  private CamelContext camelContext;

  @Override
  public boolean isCamelTrace()
  {
    return camelContext.isTracing();
  }

  @Override
  public void setCamelTrace(boolean value)
  {
    camelContext.setTracing(value);
    log.info("Tracing turned " + ((value) ? "on" : "off") + ".");
  }

}
