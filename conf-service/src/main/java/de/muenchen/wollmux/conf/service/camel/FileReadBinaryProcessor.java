package de.muenchen.wollmux.conf.service.camel;

import java.io.File;
import java.net.URI;

import javax.enterprise.context.ApplicationScoped;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

@ApplicationScoped
public class FileReadBinaryProcessor implements Processor
{

  @Override
  public void process(Exchange exchange) throws Exception
  {
    String url = exchange.getIn().getHeader("url").toString();
    
    File f = new File(URI.create(url));
    
    if (f.exists() && f.isFile())
    {
      byte[] content = FileUtils.readFileToByteArray(f);
      exchange.getOut().setBody(content);
      exchange.getOut().setHeader("path", FilenameUtils.getFullPath(f.getAbsolutePath()));
    }
  }

}
