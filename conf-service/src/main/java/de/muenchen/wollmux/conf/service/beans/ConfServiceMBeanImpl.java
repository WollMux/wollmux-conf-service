package de.muenchen.wollmux.conf.service.beans;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.camel.CamelContext;

import de.muenchen.wollmux.conf.service.core.beans.Config;
import io.vertx.core.logging.Logger;

@Dependent
public class ConfServiceMBeanImpl implements ConfServiceMXBean
{
  @Inject
  private Logger log;

  @Inject
  private CamelContext camelContext;

  @Config("unit")
  @Inject
  private String unit;

  @Inject
  @Config("path")
  private String basePath;

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

  @Override
  public String getUnit()
  {
    return unit;
  }

  @Override
  public String getPath()
  {
    return basePath;
  }

}
