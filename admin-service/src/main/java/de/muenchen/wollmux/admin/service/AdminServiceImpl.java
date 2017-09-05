package de.muenchen.wollmux.admin.service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import de.muenchen.wollmux.conf.service.AdminService;
import de.muenchen.wollmux.conf.service.FileObject;
import de.muenchen.wollmux.conf.service.core.beans.Config;
import de.muenchen.wollmux.conf.service.io.camel.ConfRouteBuilder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

@ApplicationScoped
public class AdminServiceImpl implements AdminService
{
  @Inject
  Logger log;

  @Inject
  private ProducerTemplate producerTemplate;

  @Inject
  @Config("path")
  private String basePath;

  @Inject
  @Config("unit")
  private String unit;

  @Override
  public void getFile(String file,
      Handler<AsyncResult<JsonObject>> resultHandler)
  {
    try
    {
      String path = StringUtils.appendIfMissing(basePath, "/") + file;
      String ext = FilenameUtils.getExtension(file);

      UrlValidator validator = new UrlValidator(
          new String[] { "http", "https", "file" });
      if (!validator.isValid(path))
      {
        resultHandler.handle(
            Future.failedFuture(String.format("Url %s is invalid.", path)));
        return;
      }

      URI url = URI.create(path);
      String protocol = url.getScheme();

      Map<String, Object> headers = new HashMap<>();
      headers.put("url", path);
      headers.put("protocol", protocol);
      String contents = producerTemplate.requestBodyAndHeaders(
          ConfRouteBuilder.ROUTE_READ_FILE, null, headers, String.class);

      TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".html");
      Handlebars handlebars = new Handlebars(loader);
      Template template = handlebars.compile("form");
      Context data = Context.newBuilder(new Object()).combine("unit", unit)
          .combine("content", contents).combine("file", file).build();

      FileObject result = new FileObject(template.apply(data), ext);

      if (result.getContent() != null && result.getContent().length() > 0)
      {
        resultHandler.handle(Future.succeededFuture(result.toJson()));
        return;
      }

      resultHandler.handle(Future.failedFuture("File couldn't be read."));
    } catch (Exception e)
    {
      resultHandler.handle(Future.failedFuture(e));
      log.error("getFile failed.", e);
    }
  }

  @Override
  public void writeFile(String file, String content,
      Handler<AsyncResult<JsonObject>> resultHandler)
  {
    try
    {
      String path = StringUtils.appendIfMissing(basePath, "/") + file;

      UrlValidator validator = new UrlValidator(
          new String[] { "http", "https", "file" });
      if (!validator.isValid(path))
      {
        resultHandler.handle(
            Future.failedFuture(String.format("Url %s is invalid.", path)));
        return;
      }

      Map<String, Object> headers = new HashMap<>();
      headers.put("url", path);

      boolean answer = producerTemplate.requestBodyAndHeaders(
          ConfRouteBuilder.ROUTE_WRITE_FILE, content, headers, Boolean.class);

      if (answer)
      {
        resultHandler.handle(Future.succeededFuture(
            new JsonObject().put("result", "File successfully saved.")));
        return;
      }

      resultHandler.handle(Future.failedFuture("File couldn't be saved."));
    } catch (Exception e)
    {
      resultHandler.handle(Future.failedFuture(e));
      log.error("writeFile failed.", e);
    }
  }

}
