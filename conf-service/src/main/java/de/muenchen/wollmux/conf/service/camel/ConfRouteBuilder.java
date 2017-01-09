package de.muenchen.wollmux.conf.service.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;

@ApplicationScoped
public class ConfRouteBuilder extends RouteBuilder
{

  @Inject
  private IncludeProcessor includeProcessor;

  @Override
  public void configure() throws Exception
  {
    from("direct:in").process(includeProcessor);
  }

}
