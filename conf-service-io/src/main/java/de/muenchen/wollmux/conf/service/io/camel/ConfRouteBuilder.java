package de.muenchen.wollmux.conf.service.io.camel;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.cache.CacheConstants;

@ApplicationScoped
public class ConfRouteBuilder extends RouteBuilder
{
  public static final String ROUTE_GET_FILE = "direct:getFile";
  public static final String ROUTE_INVALIDATE_CACHE = "direct:invalidateCache";
  public static final String ROUTE_READ_FILE = "direct:readFile";
  public static final String ROUTE_WRITE_FILE = "direct:writeFile";
  private static final String ROUTE_READ_BINARY_HTTP = "direct:readBinaryHttp";
  private static final String ROUTE_READ_CONF_HTTP = "direct:readConfHttp";
  private static final String ROUTE_READ_BINARY_FILE = "direct:readBinaryFile";
  private static final String ROUTE_READ_CONF_FILE = "direct:readConfFile";
  private static final String ROUTE_READ_HTTP = "direct:readHttp";
  private static final String ROUTE_READ_LOCAL = "direct:readLocal";
  private static final String ROUTE_CONF_CACHE = "cache://ConfCache";
  private static final String ROUTE_CACHE = "direct:cache";

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

  @Inject
  private FileWriteProcessor fileWriteProcessor;

  @Override
  public void configure() throws Exception
  {
    getContext().setTracing(false);

    from(ROUTE_WRITE_FILE).id("writeFile")
      .process(fileWriteProcessor)
      .end();

    from(ROUTE_READ_FILE).id("readFile")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
      .setHeader(CacheConstants.CACHE_KEY, header("url"))
      .to(ROUTE_CONF_CACHE)
      .choice().when(PredicateBuilder.and(
          header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNull(),
          header("url").endsWith("conf")))
        .choice().when(header("protocol").isEqualToIgnoreCase("file"))
          .process(fileReadProcessor)
          .to(ROUTE_CACHE)
        .otherwise()
          .process(httpReadProcessor)
          .to(ROUTE_CACHE)
        .end()
      .end();

    from(ROUTE_GET_FILE).id("getFile")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_GET))
      .setHeader(CacheConstants.CACHE_KEY, header("url"))
      .to(ROUTE_CONF_CACHE)
      .choice().when(header(CacheConstants.CACHE_ELEMENT_WAS_FOUND).isNull())
        .choice().when(header("protocol").isEqualToIgnoreCase("file"))
          .to(ROUTE_READ_LOCAL)
        .otherwise()
          .to(ROUTE_READ_HTTP)
        .end()
      .end();

    from(ROUTE_READ_LOCAL).id("readLocal")
      .choice().when(header("url").endsWith("conf"))
        .to(ROUTE_READ_CONF_FILE)
      .otherwise()
        .to(ROUTE_READ_BINARY_FILE)
      .end();

    from(ROUTE_READ_HTTP).id("readHttp")
     .choice().when(header("url").endsWith("conf"))
      .to(ROUTE_READ_CONF_HTTP)
    .otherwise()
      .to(ROUTE_READ_BINARY_HTTP)
    .end();

    from(ROUTE_READ_CONF_FILE).id("readConfFile")
      .process(fileReadProcessor)
      .process(includeProcessor)
      .to(ROUTE_CACHE);

    from(ROUTE_READ_BINARY_FILE).id("readBinaryFile")
      .process(fileReadBinaryProcessor)
      .to(ROUTE_CACHE);

    from(ROUTE_READ_CONF_HTTP).id("readConfHttp")
      .process(httpReadProcessor)
      .process(includeProcessor)
      .to(ROUTE_CACHE);

    from(ROUTE_READ_BINARY_HTTP).id("readBinaryHttp")
      .process(httpReadBinaryProcessor)
      .to(ROUTE_CACHE);

    from(ROUTE_CACHE).id("cache")
      .choice().when().simple("${body} != null")
        .log(LoggingLevel.DEBUG, "Caching ${header.url}")
        .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_ADD))
        .setHeader(CacheConstants.CACHE_KEY, header("url"))
        .to(ROUTE_CONF_CACHE)
      .end();

    from(ROUTE_INVALIDATE_CACHE).id("invalidateCache")
      .setHeader(CacheConstants.CACHE_OPERATION, constant(CacheConstants.CACHE_OPERATION_DELETEALL))
      .to(ROUTE_CONF_CACHE);
  }
}
