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
package org.eclipse.scout.rt.server.cache;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.cache.AbstractCacheWrapper;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;

/**
 * Cache wrapper used to distribute invalidate operations within a server cluster.
 * 
 * @since 5.2
 */
public class ClusterNotificationCacheWrapper<K, V> extends AbstractCacheWrapper<K, V> {

  public ClusterNotificationCacheWrapper(ICache<K, V> delegate) {
    super(delegate);
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    super.invalidate(filter, propagate);
    if (propagate) {
      InvalidateCacheNotification notification = new InvalidateCacheNotification(getCacheId(), filter);
      if (ITransaction.CURRENT.get() != null) {
        BEANS.get(IClusterSynchronizationService.class).publishTransactional(notification);
      }
      else {
        BEANS.get(IClusterSynchronizationService.class).publish(notification);
      }
    }
  }
}
