package de.muenchen.wollmux.conf.service.camel;

import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

@ApplicationScoped
public class FileReadProcessor implements Processor
{

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String url = exchange.getIn().getHeader("url").toString();
    
    File f = new File(URI.create(url));
    
    if (f.exists() && f.isFile())
    {
      String content = FileUtils.readFileToString(f, StandardCharsets.UTF_8);
      exchange.getOut().setBody(content);
      exchange.getOut().setHeader("path", FilenameUtils.getFullPath(f.getAbsolutePath()));
      exchange.getOut().setHeader("url", url);
    }
  }

}
