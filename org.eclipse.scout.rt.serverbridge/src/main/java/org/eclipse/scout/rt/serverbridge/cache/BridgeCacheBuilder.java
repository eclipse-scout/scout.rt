/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
