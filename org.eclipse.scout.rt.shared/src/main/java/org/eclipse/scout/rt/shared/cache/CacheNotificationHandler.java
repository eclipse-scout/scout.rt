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
package org.eclipse.scout.rt.shared.cache;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default handler for {@link InvalidateCacheNotification}. It invalidates cache entries but does not propagate the
 * event any further.
 *
 * @since 5.2
 */
public class CacheNotificationHandler implements INotificationHandler<InvalidateCacheNotification> {
  private static final Logger LOG = LoggerFactory.getLogger(CacheNotificationHandler.class);

  @Override
  public void handleNotification(InvalidateCacheNotification notification) {
    try {
      handleNotificationImpl(notification);
    }
    catch (RuntimeException e) {
      LOG.error("Failed handling client notification " + notification, e);
    }
  }

  @SuppressWarnings("unchecked")
  protected <K, V> void handleNotificationImpl(InvalidateCacheNotification notification) {
    String cacheId = notification.getCacheId();
    for (ICache<K, V> cache : BEANS.all(ICache.class)) {
      if (cache.getCacheId().equals(cacheId)) {
        cache.invalidate((ICacheEntryFilter<K, V>) notification.getFilter(), false);
        return;
      }
    }
  }
}
