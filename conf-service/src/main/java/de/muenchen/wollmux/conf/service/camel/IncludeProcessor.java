package de.muenchen.wollmux.conf.service.camel;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.AsyncCallback;
import org.apache.camel.AsyncProcessor;
import org.apache.camel.Exchange;
import org.apache.commons.io.FileUtils;

import de.muenchen.wollmux.conf.service.core.beans.Config;
import io.vertx.core.logging.Logger;

/**
 * Verarbeitet alle include-Anweisungen in einer Message und ersetzt sie durch
 * den Inhalt der entsprechenden Datei. Der Prozessor kann sowohl synchron als
 * auch asynchron arbeiten.
 *
 * Der Basispfad für die Dateien wird über die Environmentvariable oder das
 * System-Property mit dem Namen ENV_CONFSERVICE_PATH gesetzt.
 *
 * @author daniel.sikeler
 *
 */
@ApplicationScoped
public class IncludeProcessor implements AsyncProcessor
{
  @Inject
  Logger log;

  @Config("path")
  @Inject
  private String path;

  @Override
  public boolean process(Exchange exchange, AsyncCallback callback)
  {
    new Thread(() ->
    {
      try
      {
        process(exchange);
      } catch (Exception e)
      {
        exchange.setException(e);
      }
      callback.done(false);
    }).start();
    return false;
  }

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String message = exchange.getIn().getBody(String.class);
    Pattern pattern = Pattern.compile("%include \\\"(.*)\\\"",
        Pattern.UNICODE_CASE | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(message);
    while (matcher.find())
    {
      String filename = matcher.group(1);
      String content = FileUtils.readFileToString(
          Paths.get(path, filename).toFile(), StandardCharsets.UTF_8);
      message = matcher.replaceFirst(Matcher.quoteReplacement(content));
      matcher.reset(message);
    }
    exchange.getOut().setBody(message);
  }

}
