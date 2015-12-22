package org.eclipse.scout.rt.shared.cache;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;

/**
 * A registry allowing to register and retrieving caches with a given id.
 */
@ApplicationScoped
public interface ICacheRegistryService {

  /**
   * Register a cache. Use {@link #get(String)} to query the registry for a cache with a given id.
   */
  <K, V> void register(ICache<K, V> cache);

  /**
   * Returns a previously registered cache
   *
   * @throws AssertionException
   *           if not found
   */
  <K, V> ICache<K, V> get(String cacheId);

  /**
   * @return a previously registered cache or <code>null</code>, if not found.
   */
  <K, V> ICache<K, V> opt(String cacheId);

}
