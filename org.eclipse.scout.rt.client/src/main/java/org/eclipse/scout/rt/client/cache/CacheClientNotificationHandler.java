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

import org.eclipse.scout.rt.client.clientnotification.AbstractObservableNotificationHandler;
import org.eclipse.scout.rt.shared.cache.CacheNotificationHandler;
import org.eclipse.scout.rt.shared.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;

/**
 * Client {@link CacheNotificationHandler} that additionally informs any session listeners.
 *
 * @since 5.2
 */
public class CacheClientNotificationHandler extends AbstractObservableNotificationHandler<InvalidateCacheNotification> {

  private final INotificationHandler<InvalidateCacheNotification> m_basicHandler;

  public CacheClientNotificationHandler() {
    this(new CacheNotificationHandler());
  }

  protected CacheClientNotificationHandler(INotificationHandler<InvalidateCacheNotification> basicHandler) {
    m_basicHandler = basicHandler;
  }

  @Override
  public void handleNotification(final InvalidateCacheNotification notification) {
    // first invalidate caches
    m_basicHandler.handleNotification(notification);
    // then notify any session listeners
    notifyListeners(notification);
  }
}
