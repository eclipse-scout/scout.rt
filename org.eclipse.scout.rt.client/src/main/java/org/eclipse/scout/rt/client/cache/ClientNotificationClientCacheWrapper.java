/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.cache;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.cache.AbstractCacheWrapper;
import org.eclipse.scout.rt.platform.cache.ICache;
import org.eclipse.scout.rt.platform.cache.ICacheEntryFilter;
import org.eclipse.scout.rt.shared.cache.IRemoteCacheService;

/**
 * Cache wrapper used for shared caches where invalidate events from client should be propagated to server.
 *
 * @since 5.2
 */
public class ClientNotificationClientCacheWrapper<K, V> extends AbstractCacheWrapper<K, V> {

  public ClientNotificationClientCacheWrapper(ICache<K, V> delegate) {
    super(delegate);
  }

  @Override
  public void invalidate(ICacheEntryFilter<K, V> filter, boolean propagate) {
    if (propagate) {
      // invalidate the remote server cache. This will in turn invalidate the local client cache
      BEANS.get(IRemoteCacheService.class).invalidate(getCacheId(), filter, true);
    }
    else {
      super.invalidate(filter, propagate);
    }
  }
}
