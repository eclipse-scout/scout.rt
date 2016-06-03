package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A server side cache use to store web resources like HTML, CSS and JS. The scope of this cache is servlet-wide (for
 * all sessions).
 */
@ApplicationScoped
public class HttpResourceCache {

  private static final Logger LOG = LoggerFactory.getLogger(HttpResourceCache.class);

  private Map<HttpCacheKey, HttpCacheObject> m_cache = Collections.synchronizedMap(new HashMap<HttpCacheKey, HttpCacheObject>());

  /**
   * Puts an object into the cache if {@link HttpCacheObject#isCachingAllowed()} is true.
   *
   * @param obj
   * @return true if the object was cached or null if it was not cached
   */
  public boolean put(HttpCacheObject obj) {
    if (!obj.isCachingAllowed()) {
      return false;
    }
    m_cache.put(obj.getCacheKey(), obj);
    LOG.debug("Stored object in cache: {}", obj.getCacheKey());
    return true;
  }

  /**
   * Returns an object from the cache.
   *
   * @param cacheKey
   * @return cached object or null
   */
  public HttpCacheObject get(HttpCacheKey cacheKey) {
    HttpCacheObject obj = m_cache.get(cacheKey);
    LOG.debug("Lookup object in cache: {} found={}", cacheKey, (obj != null));
    return obj;
  }

  /**
   * Removes a cached object with the given key.
   *
   * @return removed object or null, if it was not cached
   */
  public HttpCacheObject remove(HttpCacheKey cacheKey) {
    return m_cache.remove(cacheKey);
  }

}
