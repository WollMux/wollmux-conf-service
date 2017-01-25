package de.muenchen.wollmux.conf.service.caching;

import java.lang.annotation.Annotation;

import javax.cache.annotation.CacheInvocationParameter;
import javax.cache.annotation.CacheKeyGenerator;
import javax.cache.annotation.CacheKeyInvocationContext;
import javax.cache.annotation.GeneratedCacheKey;
import javax.enterprise.context.ApplicationScoped;

import org.jsr107.ri.annotations.DefaultGeneratedCacheKey;

@ApplicationScoped
public class KeyGenerator implements CacheKeyGenerator
{

  @Override
  public GeneratedCacheKey generateCacheKey(
      CacheKeyInvocationContext<? extends Annotation> context)
  {
    CacheInvocationParameter[] params = context.getKeyParameters();
    int key = 0;
    for(int i = 0; i < params.length; i++)
    {
      key += params[i].getValue().hashCode();
    }
    return new DefaultGeneratedCacheKey(new Object[] { key });
  }
}
