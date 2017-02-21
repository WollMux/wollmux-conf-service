package de.muenchen.wollmux.conf.service.camel;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;

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
  private ProducerTemplate producerTemplate;

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String message = exchange.getIn().getBody(String.class);
    Pattern pattern = Pattern.compile("^[^#\\v]*%include \\\"(.*)\\\"",
        Pattern.UNICODE_CASE | Pattern.MULTILINE);
    Matcher matcher = pattern.matcher(message);
    while (matcher.find())
    {
      // URL bauen und encoden
      String file = matcher.group(1);
      URL base = URI.create(exchange.getIn().getHeader("url", String.class))
          .toURL();
      URL url = new URL(base, file);
      url = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
          url.getPort(), url.getPath(), url.getQuery(), url.getRef()).toURL();
      String protocol = url.getProtocol();

      Map<String, Object> headers = new HashMap<>();
      headers.put("url", url.toString());
      headers.put("protocol", protocol);
      String contents = producerTemplate.requestBodyAndHeaders(
          ConfRouteBuilder.ROUTE_GET_FILE, null, headers, String.class);

      message = matcher.replaceFirst(Matcher.quoteReplacement(contents));
      matcher.reset(message);
    }

    exchange.getOut().setBody(message);
    exchange.getOut().setHeader("url", exchange.getIn().getHeader("url"));
  }

}
