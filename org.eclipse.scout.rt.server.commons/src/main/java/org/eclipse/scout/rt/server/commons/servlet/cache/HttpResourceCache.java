package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A server side cache use to store web resources like HTML, CSS and JS.
 */
@Bean
public class HttpResourceCache implements IHttpResourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(HttpResourceCache.class);

  private final Map<HttpCacheKey, HttpCacheObject> m_cache = Collections.synchronizedMap(new HashMap<HttpCacheKey, HttpCacheObject>());

  @Override
  public boolean put(HttpCacheObject obj) {
    if (!obj.isCachingAllowed()) {
      return false;
    }
    m_cache.put(obj.getCacheKey(), obj);
    LOG.debug("Stored object in cache: {}", obj.getCacheKey());
    return true;
  }

  @Override
  public HttpCacheObject get(HttpCacheKey cacheKey) {
    HttpCacheObject obj = m_cache.get(cacheKey);
    LOG.debug("Lookup object in cache: {} found={}", cacheKey, obj != null);
    return obj;
  }

  @Override
  public HttpCacheObject remove(HttpCacheKey cacheKey) {
    HttpCacheObject obj = m_cache.remove(cacheKey);
    LOG.debug("Remove object in cache: {} removed={}", cacheKey, obj != null);
    return obj;
  }

  @Override
  public void clear() {
    LOG.debug("Clear resource cache");
    m_cache.clear();
  }
}
