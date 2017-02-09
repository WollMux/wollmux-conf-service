package de.muenchen.wollmux.conf.service.camel;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;

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
public class IncludeProcessor implements Processor
{
  @Inject
  Logger log;

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

      String path = exchange.getIn().getHeader("path").toString();
      
      String content = FileUtils.readFileToString(
          Paths.get(path, filename).toFile(), StandardCharsets.UTF_8);
      message = matcher.replaceFirst(Matcher.quoteReplacement(content));
      matcher.reset(message);
    }
    exchange.getOut().setBody(message);
  }

}
