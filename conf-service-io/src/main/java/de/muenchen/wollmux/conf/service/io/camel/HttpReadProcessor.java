package de.muenchen.wollmux.conf.service.io.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.camel.Exchange;

@Named
@ApplicationScoped
public class HttpReadProcessor extends HttpReadBinaryProcessor
{

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String url = exchange.getIn().getHeader("url").toString();
    byte[] content = read(url);

    if (content != null && content.length > 0)
    {
      exchange.getOut().setBody(new String(content));
      exchange.getOut().setHeader("url", url);
    }
  }

}
