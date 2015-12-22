package org.eclipse.scout.rt.shared.cache;

import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.util.Assertions;

/**
 * A registry allowing to register and retrieving caches with a given id.
 */
public class CacheRegistryService implements ICacheRegistryService {

  private final ConcurrentHashMap<String, ICache> m_map = new ConcurrentHashMap<>();

  /**
   * Register a cache with a given id
   */
  @Override
  public <K, V> void register(ICache<K, V> cache) {
    getMap().put(cache.getCacheId(), cache);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> ICache<K, V> get(String cacheId) {
    return (ICache<K, V>) Assertions.assertNotNull(opt(cacheId));
  }

  @SuppressWarnings("unchecked")
  @Override
  public <K, V> ICache<K, V> opt(String cacheId) {
    return getMap().get(cacheId);
  }

  protected ConcurrentHashMap<String, ICache> getMap() {
    return m_map;
  }

}
