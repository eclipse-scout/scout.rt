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
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.shared.cache.AbstractCacheWrapper;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;

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
