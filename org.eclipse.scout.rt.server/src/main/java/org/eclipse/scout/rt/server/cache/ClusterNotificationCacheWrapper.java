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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.cache.AbstractCacheWrapper;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.platform.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache wrapper used to distribute invalidate operations within a server cluster.
 *
 * @since 5.2
 */
public class ClusterNotificationCacheWrapper<K, V> extends AbstractCacheWrapper<K, V> {

  private static final Logger LOG = LoggerFactory.getLogger(ClusterNotificationCacheWrapper.class);

  public ClusterNotificationCacheWrapper(ICache<K, V> delegate) {
    super(delegate);
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    super.invalidate(filter, propagate);
    if (propagate) {
      InvalidateCacheNotification notification = new InvalidateCacheNotification(getCacheId(), filter);
      if (ITransaction.CURRENT.get() != null) {
        LOG.debug("Publish InvalidateCacheNotification for cache '{}' transactional to other backend nodes", getCacheId());
        BEANS.get(IClusterSynchronizationService.class).publishTransactional(notification);
      }
      else {
        LOG.debug("Publish InvalidateCacheNotification for cache '{}' to other backend nodes", getCacheId());
        BEANS.get(IClusterSynchronizationService.class).publish(notification);
      }
    }
  }
}
