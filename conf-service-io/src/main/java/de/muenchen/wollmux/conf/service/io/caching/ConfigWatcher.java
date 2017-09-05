package de.muenchen.wollmux.conf.service.io.caching;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.ProducerTemplate;

import de.muenchen.wollmux.conf.service.core.beans.Config;
import de.muenchen.wollmux.conf.service.io.camel.ConfRouteBuilder;
import io.vertx.core.logging.Logger;

/**
 * Ein Service, der den Ordner mit der Konfiguration auf Änderungen untersucht
 * und darauf reagiert.
 *
 * @author daniel.sikeler
 *
 */
@ApplicationScoped
public class ConfigWatcher
{
  @Inject
  Logger log;

  @Inject
  @Config("path")
  private String path;

  private WatchService watcher;

  @Inject
  private ProducerTemplate producerTemplate;

  @Inject
  public ConfigWatcher(WatchService watcher) {
    this.watcher = watcher;
  }

  @PostConstruct
  private void init()
  {
    try
    {
      URI uri = URI.create(path);
      if (uri.getScheme().equals("file"))
      {
        Paths.get(uri).register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY);
      }
    } catch (IOException ex)
    {
      log.error("Watcher wurde nicht registriert.", ex);
    }
  }

  @PreDestroy
  private void destroy()
  {
    try
    {
      watcher.close();
    } catch (IOException ex)
    {
      log.error("Watcher wurde nicht geschlossen.", ex);
    }
  }

  /**
   * Invalidiert den Cache wenn es eine Änderung im Ordner gab. Ansonsten wird
   * nichts gemacht.
   */
  public void processEvent()
  {
    WatchKey key = watcher.poll();
    if (key != null)
    {
      producerTemplate.requestBody(ConfRouteBuilder.ROUTE_INVALIDATE_CACHE, new Object());
      /*
       * Alle Events vom Key entfernen, sonst wird er jedes Mal wieder in die
       * Queue des WatchService gelegt. Sobald es neue Events gibt, wird der Key
       * der Queue wieder hinzugefügt.
       */
      key.pollEvents();
      key.reset();
    }
  }
}
