package org.eclipse.scout.rt.server.commons.servlet.cache;

/**
 * <h3>{@link IHttpResourceCache}</h3> Interface for HTTP resource caches.
 */
public interface IHttpResourceCache {

  /**
   * Puts an object into the cache if {@link HttpCacheObject#isCachingAllowed()} is true.
   *
   * @param obj
   * @return true if the object was cached or null if it was not cached
   */
  boolean put(HttpCacheObject obj);

  /**
   * Returns an object from the cache.
   *
   * @param cacheKey
   * @return cached object or null
   */
  HttpCacheObject get(HttpCacheKey cacheKey);

  /**
   * Removes a cached object with the given key.
   * 
   * @param cacheKey
   *          The key of the object to return.
   * @return removed object or null, if it was not cached
   */
  HttpCacheObject remove(HttpCacheKey cacheKey);

  /**
   * Removes all entries from the cache.
   */
  void clear();

}
