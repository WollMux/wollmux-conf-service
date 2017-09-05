package de.muenchen.wollmux.admin.service;

import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.weld.vertx.web.WeldWebVerticle;

import de.muenchen.wollmux.conf.service.AdminService;
import de.muenchen.wollmux.conf.service.core.beans.Config;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import io.vertx.serviceproxy.ProxyHelper;

/**
 * Registriert den eigentlichen Konfigurationsservice, der für eine
 * Konfiguration per Organisationseinheit verantwortlich ist.
 * Zusätzlich wird ein Ping-Event (conf-service-<unit>-ping) registriert,
 * über den der Status des Service geprüft werden kann. Das Event gibt auf jede
 * Anfrage 'ping' zurück.
 *
 *  Der Name des Service wird über die Environmentvariable oder das System-
 *  Property mit dem Namen ENV_CONFSERVICE_UNIT gesetzt.
 *
 * @author andor.ertsey
 *
 */
@Dependent
public class AdminServiceVerticle extends AbstractVerticle
{
  private MessageConsumer<JsonObject> messageConsumer;
  private MessageConsumer<String> pingConsumer;

  @Inject
  private Logger log;

  @Config("unit")
  @Inject
  private String unit;

  @Inject
  private AdminService adminService;

  @PostConstruct
  public void init()
  {
  }

  @Override
  public void start(Future<Void> startFuture) throws Exception
  {
    String address = AdminService.ADMIN_SERVICE_ADDRESS + unit;
    String serviceName = AdminService.ADMIN_SERVICE_BASE_NAME + unit;

    pingConsumer = vertx.eventBus().consumer(serviceName + "-ping");
    pingConsumer.handler(msg -> msg.reply("ping"));

    messageConsumer = ProxyHelper.registerService(AdminService.class, vertx, adminService, address, 5000);
    ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);

    Record confServiceRecord = EventBusService.createRecord(serviceName, address, AdminService.class);
    serviceDiscovery.publish(confServiceRecord, res ->
    {
      if (res.succeeded())
      {
        log.info("Service AdminService " + unit + " published.");
        startFuture.complete();
      } else
      {
        log.error("Publishing AdminService failed.", res.cause());
        startFuture.fail(res.cause());
      }
    });

    serviceDiscovery.close();
  }

  @Override
  public void stop() throws Exception
  {
    log.info("AdminServiceVerticle stopping.");
    pingConsumer.unregister();
    ProxyHelper.unregisterService(messageConsumer);
  }

  public static String getIP()
  {
    String value = System.getenv("ENV_CONFSERVICE_IP");
    if (value == null)
    {
      value = System.getProperty("ENV_CONFSERVICE_IP");
    }
    if (value == null)
    {
      value = VertxOptions.DEFAULT_CLUSTER_HOST;
    }
    return value;
  }

  public static void main(String[] args)
  {
    System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
    System.setProperty("hazelcast.logging.type", "slf4j");

    Logger log = LoggerFactory.getLogger(AdminServiceVerticle.class);

    VertxOptions options = new VertxOptions();
    options.setClustered(true);
    options.setClusterHost(getIP());
    Vertx.clusteredVertx(options, res ->
    {
      if (res.succeeded())
      {
        final WeldWebVerticle weldVerticle = new WeldWebVerticle();
        Vertx vertx = res.result();

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
          @Override
          public void run()
          {
            vertx.close();
          }
        });

        vertx.deployVerticle(weldVerticle, result ->
        {
          if (result.succeeded())
          {
            vertx.deployVerticle(weldVerticle.container().select(AdminServiceVerticle.class).get(), res1 ->
            {
              if (res1.succeeded())
              {
                log.info("AdminServiceVerticle deployed.");
              } else
              {
                log.error("Deployment of AdminServiceVerticle failed.", res1.cause());
              }
            });
          } else
          {
            log.error("Deployment of WeldWebVerticle failed.", result.cause());
          }
        });
      }
    });
  }
}
