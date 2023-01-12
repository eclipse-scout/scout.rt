/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.serverbridge.cache;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.cache.CacheBuilder;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.server.cache.ClusterNotificationCacheWrapper;

/**
 * Cache with cluster notifications, but without client notifications
 */
@Order(4000)
public class BridgeCacheBuilder<K, V> extends CacheBuilder<K, V> {

  @Override
  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    cache = super.addBeforeCustomWrappers(cache);
    if (isClusterEnabled()) {
      cache = new ClusterNotificationCacheWrapper<>(cache);
    }
    return cache;
  }
}
