/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.cache.BasicCache;
import org.eclipse.scout.rt.platform.cache.CacheBuilder;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;

/**
 * @since 5.2
 */
@Order(5050)
public class ClientCacheBuilder<K, V> extends CacheBuilder<K, V> {

  @Override
  protected ICache<K, V> createBasicCache(Map<K, V> cacheMap) {
    ICacheValueResolver<K, V> valueResolver = getValueResolver();
    if (isSharedAndRemoteAvailable() && isRemoteValueResolverEnabled()) {
      valueResolver = new RemoteCacheValueResolver<>(getCacheId());
    }
    return new BasicCache<>(getCacheId(), getLabelSupplier(), valueResolver, cacheMap);
  }

  @Override
  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    cache = super.addBeforeCustomWrappers(cache);
    if (isSharedAndRemoteAvailable()) {
      cache = new ClientNotificationClientCacheWrapper<>(cache);
    }
    return cache;
  }

  @Override
  protected Map<K, V> createCacheMap() {
    if (isTransactional()) {
      // Caches in the client should not be transactional. The transactional complexity is important in the server, where transactional maps are necessary.
      // Cache notifications to the client are mostly a result of a committed transaction in the server and therefore need not be transactional itself.
      return new ConcurrentHashMap<>();
    }
    return super.createCacheMap();
  }

  /**
   * @return true if configured as shared and {@link IRemoteCacheService} is available
   */
  protected boolean isSharedAndRemoteAvailable() {
    return isShared() && BEANS.opt(IRemoteCacheService.class) != null;
  }
}
