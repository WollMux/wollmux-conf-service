package de.muenchen.wollmux.conf.service.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cache.CacheConstants;

@ApplicationScoped
public class ConfRouteBuilder extends RouteBuilder
{
  public static final String ROUTE_GET_FILE = "direct:getFile";
  private static final String ROUTE_CACHE = "direct:cache";
  private static final String ROUTE_INVALIDATE_CACHE = "direct:invalidateCache";

  @Inject
  private IncludeProcessor includeProcessor;

  @Inject
  private FileReadProcessor fileReadProcessor;

  @Inject
  private FileReadBinaryProcessor fileReadBinaryProcessor;

  @Inject @Named("httpReadProcessor")
  private HttpReadProcessor httpReadProcessor;

  @Inject @Named("httpReadBinaryProcessor")
  private HttpReadBinaryProcessor httpReadBinaryProcessor;

  @Override
  public void configure() throws Exception
  {
    getContext().setTracing(false);
    
    from(ROUTE_GET_FILE).id("getFile")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
      .setHeader(CacheConstants.CACHE_KEY, header("url"))
      .to("cache://ConfCache")
      .choice().when(header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNull())
        .choice().when(header("protocol").isEqualToIgnoreCase("file"))
          .to("direct:readLocal")
        .otherwise()
          .to("direct:readHttp")
        .end()
      .end();

    from("direct:readLocal").id("readLocal")
      .choice().when(header("url").endsWith("conf"))
        .to("direct:readConfFile")
      .otherwise()
        .to("direct:readBinaryFile")
      .end();

    from("direct:readHttp").id("readHttp")
     .choice().when(header("url").endsWith("conf"))
      .to("direct:readConfHttp")
    .otherwise()
      .to("direct:readBinaryHttp")
    .end();

    from("direct:readConfFile")
      .process(fileReadProcessor)
      .process(includeProcessor)
      .to(ROUTE_CACHE);

    from("direct:readBinaryFile")
      .process(fileReadBinaryProcessor)
      .to(ROUTE_CACHE);

    from("direct:readConfHttp").id("readConfHttp")
      .process(httpReadProcessor)
      .process(includeProcessor)
      .to(ROUTE_CACHE);

    from("direct:readBinaryHttp").id("readBinaryHttp")
      .process(httpReadBinaryProcessor)
      .to(ROUTE_CACHE);

    from(ROUTE_CACHE).id("cache")
      .choice().when().simple("${body} != null")
        .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
        .setHeader(CacheConstants.CACHE_KEY, header("url"))
        .to("cache://ConfCache")
      .end();

    from(ROUTE_INVALIDATE_CACHE).id("invalidateCache")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_DELETEALL))
      .to("cache://ConfCache");
  }
}
