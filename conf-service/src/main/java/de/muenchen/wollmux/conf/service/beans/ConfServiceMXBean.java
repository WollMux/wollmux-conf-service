package de.muenchen.wollmux.conf.service.beans;

public interface ConfServiceMXBean
{
  public boolean isCamelTrace();
  public void setCamelTrace(boolean value);
  public String getUnit();
  public String getPath();
}
