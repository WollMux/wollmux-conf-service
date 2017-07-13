package de.muenchen.wollmux.conf.service;

import io.vertx.servicediscovery.Record;

public class ConfServiceCache extends ServiceCache<ConfService>
{

  @Override
  boolean validateService(Record record)
  {
    return record.getName().startsWith(ConfService.CONF_SERVICE_BASE_NAME);
  }

  @Override
  Class<?> getServiceClass()
  {
    return ConfService.class;
  }
}
