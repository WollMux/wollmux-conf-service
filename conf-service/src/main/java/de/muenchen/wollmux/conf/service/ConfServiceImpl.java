package de.muenchen.wollmux.conf.service;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import de.muenchen.wollmux.conf.service.core.beans.Config;
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

        if (config.length() > 0)
        {
          FileObject result = new FileObject(config, "conf");
          resultHandler.handle(Future.succeededFuture(result.toJson()));
        }
      }
      if (ext.equals("json"))
      {
      }
      else
      {
        byte[] contents = producerTemplate
            .requestBodyAndHeader("direct:readBinaryFile", "", "url", path, byte[].class);


        if (contents.length > 0)
        {
          FileObject result = new FileObject(contents, ext);
          resultHandler.handle(Future.succeededFuture(result.toJson()));
        }

      }
      resultHandler.handle(Future.failedFuture("File couldn't be read."));
    } catch (Exception e)
    {
      resultHandler.handle(Future.failedFuture(e));
      log.error("getFile failed.", e);
    }
  }
}
