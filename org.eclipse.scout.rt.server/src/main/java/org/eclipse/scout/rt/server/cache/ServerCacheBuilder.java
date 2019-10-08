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
