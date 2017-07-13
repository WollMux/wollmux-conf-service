package de.muenchen.wollmux.conf.service;

import io.vertx.servicediscovery.Record;

public class AdminServiceCache extends ServiceCache<AdminService>
{

  @Override
  boolean validateService(Record record)
  {
    return record.getName().startsWith(AdminService.ADMIN_SERVICE_BASE_NAME);
  }

  @Override
  Class<?> getServiceClass()
  {
    return AdminService.class;
  }

}
