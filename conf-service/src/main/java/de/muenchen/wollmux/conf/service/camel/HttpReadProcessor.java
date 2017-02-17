package de.muenchen.wollmux.conf.service.camel;

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
  }

}
