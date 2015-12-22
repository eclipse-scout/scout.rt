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
package org.eclipse.scout.rt.shared.cache;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic handler for {@link InvalidateCacheNotification}. It invalidates cache entries but does not propagate the event
 * any further.
 * <p>
 * This class has an {@link IgnoreBean} annotation by design. This is done for extension in client and server.
 *
 * @since 5.2
 */
@IgnoreBean
public class CacheNotificationHandler implements INotificationHandler<InvalidateCacheNotification> {
  private static final Logger LOG = LoggerFactory.getLogger(CacheNotificationHandler.class);

  @Override
  public void handleNotification(InvalidateCacheNotification notification) {
    handleNotificationImpl(notification);
  }

  @SuppressWarnings("unchecked")
  protected <K, V> void handleNotificationImpl(InvalidateCacheNotification notification) {
    String cacheId = notification.getCacheId();
    ICache<K, V> cache = BEANS.get(ICacheRegistryService.class).opt(cacheId);
    if (cache != null) {
      cache.invalidate((ICacheEntryFilter<K, V>) notification.getFilter(), false);
    }
    else {
      LOG.error("Notification received for unknown cache {} ", cacheId);
    }
  }
}
