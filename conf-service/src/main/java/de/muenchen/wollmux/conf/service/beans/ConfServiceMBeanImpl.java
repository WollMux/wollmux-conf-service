package de.muenchen.wollmux.conf.service.beans;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import de.muenchen.wollmux.conf.service.core.beans.Config;
@Dependent
public class ConfServiceMBeanImpl implements ConfServiceMXBean
{

  @Config("unit")
  @Inject
  private String unit;

  @Inject
  @Config("path")
  private String basePath;

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
