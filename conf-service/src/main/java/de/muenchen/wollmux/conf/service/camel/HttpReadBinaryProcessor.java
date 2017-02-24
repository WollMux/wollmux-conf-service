package de.muenchen.wollmux.conf.service.camel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import io.vertx.core.logging.Logger;

@Named
@ApplicationScoped
public class HttpReadBinaryProcessor implements Processor
{
  @Inject
  Logger log;

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String url = exchange.getIn().getHeader("url").toString();
    byte[] content = read(url);
    
    if (content != null && content.length > 0)
    {
      exchange.getOut().setBody(content);
      exchange.getOut().setHeader("url", url);
    }

  }

  protected byte[] read(String url) throws MalformedURLException, IOException
  {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    
    if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
    {
      try (InputStream in = conn.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream())
      {
        byte[] buf = new byte[4096];
        int n = -1;
        
        while ((n = in.read(buf)) != -1)
        {
          out.write(buf, 0, n);
        }

        return out.toByteArray();
      }
    } 
    else
    {
      log.error(conn.getResponseMessage());
      return null;
    }

  }
}
