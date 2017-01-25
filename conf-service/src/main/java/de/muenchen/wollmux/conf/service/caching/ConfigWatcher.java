package de.muenchen.wollmux.conf.service.caching;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import de.muenchen.wollmux.conf.service.core.beans.Config;

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
  @Config("path")
  String path;

  @Inject
  ConfigCache cache;

  private WatchService watcher;

  @PostConstruct
  private void init() throws IOException
  {
    watcher = FileSystems.getDefault().newWatchService();
    Paths.get(path).register(watcher, StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY);
  }

  @PreDestroy
  private void destory() throws IOException
  {
    watcher.close();
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
      cache.invalidate();
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
