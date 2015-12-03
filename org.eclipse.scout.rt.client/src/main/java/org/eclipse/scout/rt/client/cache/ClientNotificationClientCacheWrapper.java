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
package org.eclipse.scout.rt.client.cache;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.cache.AbstractCacheWrapper;
import org.eclipse.scout.rt.shared.cache.ICache;
import org.eclipse.scout.rt.shared.cache.ICacheEntryFilter;
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
