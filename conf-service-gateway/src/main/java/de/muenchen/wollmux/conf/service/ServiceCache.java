package de.muenchen.wollmux.conf.service;

import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.jboss.weld.vertx.VertxConsumer;
import org.jboss.weld.vertx.VertxEvent;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;

/**
 * Ein Cache für Service-Proxies von registrierten ConfServices.
 * 
 * @author andor.ertsey
 *
 */
@ApplicationScoped
public class ServiceCache
{
  @Inject
  private Logger log;

  @Inject
  private Vertx vertx;

  private ConcurrentHashMap<String, ConfService> serviceCache = new ConcurrentHashMap<>();

  private ServiceDiscovery serviceDiscovery;

  @PostConstruct
  protected void init()
  {
    serviceDiscovery = ServiceDiscovery.create(vertx);
    locateServices();
  }

  @PreDestroy
  protected void destroy()
  {
    for (ConfService cs : serviceCache.values())
    {
      ServiceDiscovery.releaseServiceObject(serviceDiscovery, cs);
    }
    serviceCache.clear();
    serviceDiscovery.close();
  }

  /**
   * Gibt einen vorher gecachten Service-Proxy zurück.
   * 
   * @param name Name des Service zu dem der Proxy gehört.
   * @return ConfService-Proxy oder null
   */
  public ConfService getService(String name)
  {
    if (serviceCache.containsKey(name))
    {
      return serviceCache.get(name);
    }
    return null;
  }

  private void locateServices()
  {
    serviceDiscovery.getRecord(record ->
    {
      return record.getName().startsWith(ConfService.CONF_SERVICE_BASE_NAME);
    }, res ->
    {
      if (res.succeeded() && res.result() != null)
      {
        String serviceName = res.result().getName();
        if (!serviceCache.containsKey(serviceName))
        {
          log.info("New service found: " + serviceName);
          EventBusService.getProxy(serviceDiscovery, ConfService.class, res1 ->
          {
            if (res1.succeeded())
            {
              ConfService confService = res1.result();
              serviceCache.put(serviceName, confService);
            } else
            {
              log.error("Proxy for service " + serviceName + 
        	  " could not be created.", res1.cause());
            }
          });
        }
      }
    });
  }

  protected void removeService(String name)
  {
    log.debug("Removing service " + name);
    
    if (serviceCache.containsKey(name))
    {
      ConfService cs = serviceCache.get(name);
      ServiceDiscovery.releaseServiceObject(serviceDiscovery, cs);
      serviceCache.remove(name);
    }
  }

  
  protected void consumeDiscoveryAnnounce(
      @Observes @VertxConsumer("vertx.discovery.announce") VertxEvent event)
  {
    JsonObject json = (JsonObject) event.getMessageBody();
    String status = json.getString("status");
    if (status.equals("UP"))
    {
      ping(json.getString("name")).setHandler(res -> {
        if (res.succeeded())
        {
          locateServices();
        }
      });
    } else if (status.equals("DOWN") || status.equals("OUT_OF_SERVICE"))
    {
      String serviceName = json.getString("name");
      log.info("Service was removed: " + serviceName);
      removeService(serviceName);
    }
  }
  
  /**
   * Pingt einen Service. Wenn der Service nicht antwortet, wird er aus der 
   * Liste der gecachten Services gelöscht.
   * 
   * @param name Vollständiger Name des Service.
   */
  public void validateProxy(String name)
  {
    ping(name).setHandler(res -> {
      if (res.failed())
      {
        log.debug("Removing invalid service " + name);
        removeService(name);
      }
    });
  }
  
  /**
   * Hilfsmethode zum Pingen eines ConfService-Services.
   * 
   * @param name
   * @return
   */
  public Future<Void> ping(String name)
  {
    Future<Void> future = Future.future();
    DeliveryOptions dop = new DeliveryOptions();
    dop.setSendTimeout(5000);
    vertx.eventBus().send(name + "-ping", "ping", dop, res -> {
      if (res.succeeded() && res.result().body().equals("ping"))
      {
        future.complete();
      } else
      {
        future.fail("Ping failed.");
      }
    });
    
    return future;
  }

}
