package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.camel.main.Main;
import org.jboss.weld.vertx.web.WeldWebVerticle;

import de.muenchen.wollmux.conf.service.caching.ConfigWatcher;
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
public class ConfServiceVerticle extends AbstractVerticle
{
  private MessageConsumer<JsonObject> messageConsumer;
  private MessageConsumer<String> pingConsumer;

  @Inject
  Logger log;

  @Config("unit")
  @Inject
  private String unit;

  @Inject
  private ConfService confService;

  @Inject
  private Main camelMain;

  @Inject
  private ConfigWatcher watcher;

  @Override
  public void start(Future<Void> startFuture) throws Exception
  {
    String address = ConfService.CONF_SERVICE_ADDRESS + unit;
    String serviceName = ConfService.CONF_SERVICE_BASE_NAME + unit;

    pingConsumer = vertx.eventBus().consumer(serviceName + "-ping");
    pingConsumer.handler(msg -> msg.reply("ping"));

    vertx.setPeriodic(5000, id -> watcher.processEvent());

    camelMain.start();

    messageConsumer = ProxyHelper.registerService(ConfService.class, vertx, confService, address, 5000);
    ServiceDiscovery serviceDiscovery = ServiceDiscovery.create(vertx);

    Record confServiceRecord = EventBusService.createRecord(serviceName, address, ConfService.class);
    serviceDiscovery.publish(confServiceRecord, res ->
    {
      if (res.succeeded())
      {
        log.info("Service ConfService " + unit + " published.");
        startFuture.complete();
      } else
      {
        log.error("Publishing ConfService failed.", res.cause());
        startFuture.fail(res.cause());
      }
    });

    serviceDiscovery.close();
  }

  @Override
  public void stop() throws Exception
  {
    log.info("ConfServiceVerticle stopping.");
    pingConsumer.unregister();
    ProxyHelper.unregisterService(messageConsumer);
    camelMain.stop();
  }

  public static void main(String[] args)
  {
    Logger log = LoggerFactory.getLogger(ConfServiceVerticle.class);

    Vertx.clusteredVertx(new VertxOptions(), res ->
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
            vertx.deployVerticle(weldVerticle.container().select(ConfServiceVerticle.class).get(), res1 ->
            {
              if (res1.succeeded())
              {
                log.info("ConfServiceVerticle deployed.");
              } else
              {
                log.error("Deployment of ConfServiceVerticle failed.", res1.cause());
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
