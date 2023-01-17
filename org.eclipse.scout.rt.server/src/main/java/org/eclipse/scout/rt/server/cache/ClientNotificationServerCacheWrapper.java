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
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;

/**
 * Cache wrapper used to notify clients about invalidate operations.
 * <p>
 * <b>Important: </b>Client notification is not cluster distributed. Therefore this delegate should be an inner delegate
 * of {@link ClusterNotificationCacheWrapper} if a cluster is used.
 *
 * @since 5.2
 */
public final class ClientNotificationServerCacheWrapper<K, V> extends AbstractCacheWrapper<K, V> {

  public ClientNotificationServerCacheWrapper(ICache<K, V> delegate) {
    super(delegate);
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    super.invalidate(filter, propagate);
    // always send invalidate operations from a server to clients and do not check on the propagate property
    InvalidateCacheNotification notification = new InvalidateCacheNotification(getCacheId(), filter);
    BEANS.get(ClientNotificationRegistry.class).putTransactionalForAllNodes(notification, false);
  }
}
