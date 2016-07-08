package org.eclipse.scout.rt.serverbridge.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.server.cache.ClusterNotificationCacheWrapper;
import org.eclipse.scout.rt.server.cache.ConcurrentTransactionalMap;
import org.eclipse.scout.rt.server.cache.CopyOnWriteTransactionalMap;
import org.eclipse.scout.rt.shared.cache.CacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICache;

/**
 * Cache with cluster notifications, but without client notifications
 */
@Order(4000)
public class BridgeCacheBuilder<K, V> extends CacheBuilder<K, V> {

  @Override
  protected Map<K, V> createCacheMap() {
    if (!isCreateExpiringMap() && isTransactional() && !isAtomicInsertion() && (isSingleton() || !isTransactionalFastForward())) {
      return new CopyOnWriteTransactionalMap<>(getCacheId(), isTransactionalFastForward());
    }
    else {
      return super.createCacheMap();
    }
  }

  @Override
  protected <KK, VV> ConcurrentMap<KK, VV> createConcurrentMap() {
    if (isTransactional()) {
      return new ConcurrentTransactionalMap<>(getCacheId(), isTransactionalFastForward());
    }
    else {
      return super.createConcurrentMap();
    }
  }

  @Override
  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    cache = super.addBeforeCustomWrappers(cache);
    if (isClusterEnabled()) {
      cache = new ClusterNotificationCacheWrapper<>(cache);
      addCacheInstance(cache);
    }
    return cache;
  }
}
