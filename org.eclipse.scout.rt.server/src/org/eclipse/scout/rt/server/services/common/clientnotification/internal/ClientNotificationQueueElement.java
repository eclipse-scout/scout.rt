/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.services.common.clientnotification.internal;

import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationFilter;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationQueueElement;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

/**
 *
 */
public class ClientNotificationQueueElement implements IClientNotificationQueueElement {
  private static final long serialVersionUID = 6996861336319521L;

  protected final IClientNotification m_notification;
  protected final IClientNotificationFilter m_filter;
  protected final long m_valid_until;

  public ClientNotificationQueueElement(IClientNotification notification, IClientNotificationFilter filter) {
    m_notification = notification;
    m_filter = filter;
    m_valid_until = System.currentTimeMillis() + notification.getTimeout();
  }

  @Override
  public IClientNotification getNotification() {
    return m_notification;
  }

  @Override
  public IClientNotificationFilter getFilter() {
    return m_filter;
  }

  @Override
  public boolean isActive() {
    return !isExpired() && m_filter.isActive();
  }

  private boolean isExpired() {
    return System.currentTimeMillis() >= m_valid_until;
  }

  public boolean isReplacableBy(IClientNotificationQueueElement newElem) {
    return (m_notification == newElem.getNotification())
        || (
        newElem.getNotification().getClass() == m_notification.getClass()
            && newElem.getFilter().equals(m_filter)
            && newElem.getNotification().coalesce(m_notification));
  }

  @Override
  public String toString() {
    return "ClientNotificationQueueElement [m_notification=" + m_notification + ", m_filter=" + m_filter + ", m_valid_until=" + m_valid_until + "]";
  }

}
