package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import de.muenchen.wollmux.conf.service.core.beans.Config;
import de.muenchen.wollmux.conf.service.exceptions.UnknownKonfigurationException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

/**
 * Konkrete Implementation von {@link ConfService}. Liefert Konfigurationsdaten
 * in verschiedenen Formaten zurück. Inhaltlich sollten die Konfigurationen
 * identisch sein.
 *
 * @author andor.ertsey
 *
 */
@ApplicationScoped
public class ConfServiceImpl implements ConfService
{
  @Inject
  Logger log;

  @Inject
  private ProducerTemplate producerTemplate;
  
  @Inject @Config("path")
  private String basePath;

  /**
   * Liefert einen String im WollMux-Conf-Format.
   */
  @Override
  public void getFile(String file,
      Handler<AsyncResult<JsonObject>> resultHandler)
  {
    try
    {
      String path = StringUtils.appendIfMissing(basePath, "/") + file;
      String ext = FilenameUtils.getExtension(file);

      UrlValidator validator = new UrlValidator(new String[]{"http", "https", "file"});
      if (!validator.isValid(path))
      {
        resultHandler.handle(Future.failedFuture(String.format("Url %1 is invalid.", path)));
        return;
      }
      
      if (ext.equals("conf"))
      {
        String config = producerTemplate
            .requestBodyAndHeader("direct:readConfFile", "", "url", path, String.class);
        
        FileObject result = new FileObject(config, "conf");
        
        resultHandler.handle(Future.succeededFuture(result.toJson()));
      }
      if (ext.equals("json"))
      {
      }
      else
      {
        byte[] config = producerTemplate
            .requestBodyAndHeader("direct:readBinaryFile", "", "url", path, byte[].class);
        
        FileObject result = new FileObject(config, ext);
        
        resultHandler.handle(Future.succeededFuture(result.toJson()));
        
      }
      resultHandler.handle(Future.succeededFuture());
    } catch (Exception e)
    {
      resultHandler.handle(Future.failedFuture(e));
      log.error("getFile failed.", e);
    }
  }

  /**
   * Liefert einen String im JSON-Format.
   */
  @Override
  public void getJSON(String filename,
      Handler<AsyncResult<JsonObject>> resultHandler)
  {
    // TODO Auto-generated method stub

  }

  /**
   * Wählt die Basisdatei für die Konfiguration je nach Produkt aus.
   *
   * @param product
   *          Das Produkt.
   * @param type
   *          Soll der Dateiname für das Conf- oder JSON-Format geliefert
   *          werden.
   * @return Ein String der den include für die Basisdatei enthält.
   * @throws UnknownKonfigurationException
   *           Ein unbekanntes Produkt wird abgefragt.
   */
  private String getFilename(String product, String type)
      throws UnknownKonfigurationException
  {
    String template = "%%include \"%s.%s\"";
    switch (product.toLowerCase())
    {
    case "wollmux":
      return String.format(template, "main", type);
    case "wollmuxbar":
      return String.format(template, "main", type);
    case "seriendruck":
      return String.format(template, "main", type);
    default:
      throw new UnknownKonfigurationException(String
          .format("Für das Produkt %s gibt es keine Konfiguration.", product));
    }
  }
}
