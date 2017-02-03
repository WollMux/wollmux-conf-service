package de.muenchen.wollmux.conf.service.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cache.CacheConstants;

@ApplicationScoped
public class ConfRouteBuilder extends RouteBuilder
{
  public static final String ROUTE_GETCONF = "direct:in";
  public static final String ROUTE_INVALIDATE_CACHE = "direct:invalidateCache";
  
  @Inject
  private IncludeProcessor includeProcessor;

  @Override
  public void configure() throws Exception
  {
    from(ROUTE_GETCONF).id("getConf")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
      .setHeader(CacheConstants.CACHE_KEY, body())
      .to("cache://ConfCache")
      .choice().when(header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNull())
        .process(includeProcessor)
        .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
        .setHeader(CacheConstants.CACHE_KEY, body())
        .to("cache://ConfCache")
      .end();
    
    from(ROUTE_INVALIDATE_CACHE).id("invalidateCache")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_DELETEALL))
      .to("cache://ConfCache");
  }
}
