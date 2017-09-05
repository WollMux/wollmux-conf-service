package de.muenchen.wollmux.conf.service.io.camel;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;

@ApplicationScoped
public class FileWriteProcessor implements Processor
{

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String url = exchange.getIn().getHeader("url").toString();
    String content = exchange.getIn().getBody(String.class);

    File f = new File(URI.create(url));

    if (f.exists() && f.isFile())
    {
      FileUtils.writeStringToFile(f, content, StandardCharsets.UTF_8);
      exchange.getOut().setBody(Boolean.TRUE);
    } else
    {
      exchange.getOut().setBody(Boolean.FALSE);
    }
  }

}
