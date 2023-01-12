/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.cache;

import org.eclipse.scout.rt.platform.cache.CacheBuilder;
import org.eclipse.scout.rt.platform.cache.ICache;

/**
 * @since 5.2
 */
public class ServerCacheBuilder<K, V> extends CacheBuilder<K, V> {

  @Override
  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    cache = super.addBeforeCustomWrappers(cache);
    if (isShared()) {
      cache = new ClientNotificationServerCacheWrapper<>(cache);
    }
    // it is important, that the ClusterNotificationCacheWrapper is added after the client notification cache wrapper
    if (isClusterEnabled()) {
      cache = new ClusterNotificationCacheWrapper<>(cache);
    }
    return cache;
  }
}
