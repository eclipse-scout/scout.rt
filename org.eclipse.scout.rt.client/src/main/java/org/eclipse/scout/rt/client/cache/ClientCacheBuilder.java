/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.cache;

import java.util.Map;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.shared.cache.BasicCache;
import org.eclipse.scout.rt.shared.cache.CacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheValueResolver;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;

/**
 * @since 5.2
 */
@Order(5050)
public class ClientCacheBuilder<K, V> extends CacheBuilder<K, V> {

  @Override
  protected ICache<K, V> createBasicCache(Map<K, V> cacheMap) {
    ICacheValueResolver<K, V> valueResolver = getValueResolver();
    if (isSharedAndRemoteAvailable()) {
      valueResolver = new RemoteCacheValueResolver<>(getCacheId());
    }
    BasicCache<K, V> cache = new BasicCache<>(getCacheId(), valueResolver, cacheMap, isAtomicInsertion());
    addCacheInstance(cache);
    return cache;
  }

  @Override
  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    cache = super.addBeforeCustomWrappers(cache);
    if (isSharedAndRemoteAvailable()) {
      cache = new ClientNotificationClientCacheWrapper<>(cache);
      addCacheInstance(cache);
    }
    return cache;
  }

  /**
   * @return true if configured as shared and {@link IRemoteCacheService} is available
   */
  protected boolean isSharedAndRemoteAvailable() {
    return isShared() && BEANS.opt(IRemoteCacheService.class) != null;
  }
}
