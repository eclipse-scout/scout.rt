/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.cache;

import java.util.Map;

import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.shared.cache.CacheBuilder;
import org.eclipse.scout.rt.shared.cache.ICache;

/**
 * @since 5.2
 */
@Order(2)
public class ServerCacheBuilder<K, V> extends CacheBuilder<K, V> {

  @Override
  protected Map<K, V> createCacheMap() {
    if (isTransactional()) {
      if (isSingleton() || !isTransactionalFastForward()) {
        return new CopyOnWriteTransactionalMap<K, V>(getCacheId(), isTransactionalFastForward());
      }
      else {
        return new ConcurrentTransactionalMap<K, V>(getCacheId(), isTransactionalFastForward());
      }
    }
    else {
      return super.createCacheMap();
    }
  }

  @Override
  protected ICache<K, V> addBeforeCustomWrappers(ICache<K, V> cache) {
    cache = super.addBeforeCustomWrappers(cache);
    if (isShared()) {
      cache = new ClientNotificationServerCacheWrapper<K, V>(cache);
      addCacheInstance(cache);
    }
    // it is important, that the ClusterNotificationCacheWrapper is added after the client notification cache wrapper
    IClusterSynchronizationService service = BEANS.opt(IClusterSynchronizationService.class);
    if (isClusterEnabled() && service != null && service.isEnabled()) {
      cache = new ClusterNotificationCacheWrapper<K, V>(cache);
      addCacheInstance(cache);
    }
    return cache;
  }
}
