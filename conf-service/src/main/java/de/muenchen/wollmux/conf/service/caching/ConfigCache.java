package de.muenchen.wollmux.conf.service.caching;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.annotation.CacheDefaults;
import javax.cache.annotation.CacheKey;
import javax.cache.annotation.CachePut;
import javax.cache.annotation.CacheRemoveAll;
import javax.cache.annotation.CacheResult;
import javax.cache.annotation.CacheValue;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spi.Synchronization;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;

/**
 * Ist die Schnittstelle zum Caching.
 *
 * @author daniel.sikeler
 *
 */
@ApplicationScoped
@CacheDefaults(cacheName = "config", cacheKeyGenerator = de.muenchen.wollmux.conf.service.caching.KeyGenerator.class)
public class ConfigCache
{

  @Inject
  private CamelContext camelContext;

  @Inject
  private Logger log;

  private CachingProvider provider;

  @PostConstruct
  private void init()
  {
    provider = Caching.getCachingProvider();
    CacheManager cacheManager = provider.getCacheManager();
    cacheManager.createCache("config", new MutableConfiguration<Integer, String>());
  }

  @PreDestroy
  private void destroy()
  {
    provider.close();
  }

  /**
   * Liefert eine Konfiguration aus dem Cache oder generiert sie neu. Der
   * Handler wird informiert, sobald die Konfiguration generiert wurde. Muss
   * keine Konfiguration generiert werden, wird der Handler nicht
   * benachrichtigt.
   *
   * @param filename
   *          Der Key für den Cache und zugleich der Dateiname für die
   *          Basisdatei der Konfiguration.
   * @param handler
   *          Der Handler.
   * @return Die Konfiguration, sofern sie bereits im Cache existiert, sonst
   *         null. Null wird immer returned, wenn die Konfiguration generiert
   *         werden muss.
   */
  @CacheResult
  public String getConfig(@CacheKey String filename, Handler<AsyncResult<String>> handler)
  {
    ProducerTemplate producer = camelContext.createProducerTemplate();
    producer.asyncCallbackRequestBody("direct:in", filename,
        new Synchronization()
        {

          @Override
          public void onComplete(Exchange exchange)
          {
            handler.handle(Future
                .succeededFuture(exchange.getOut().getBody(String.class)));
          }

          @Override
          public void onFailure(Exchange exchange)
          {
            handler.handle(Future.failedFuture(exchange.getException()));
          }
        });
    return null;
  }

  /**
   * Schreibt die Konfiguration in den Cache mittels Annotation.
   *
   * @param filename
   *          Der Schlüssel.
   * @param config
   *          Die Konfiguration.
   */
  @CachePut
  public void putConfig(@CacheKey String filename, @CacheValue String config)
  {
    // Caching wird über Annotation ausgeführt.
    log.info("add new entry to cache");
  }

  @CacheRemoveAll
  public void invalidate()
  {
    // Caching wird über Annotation invalidiert.
    log.info("cache invalidated");
  }
}
