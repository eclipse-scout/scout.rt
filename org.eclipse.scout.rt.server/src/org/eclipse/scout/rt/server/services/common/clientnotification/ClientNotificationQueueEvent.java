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
package org.eclipse.scout.rt.server.services.common.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;

public class ClientNotificationQueueEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  public static final int TYPE_NOTIFICATION_ADDED = 10;

  private int m_type;
  private IClientNotification m_notification;
  private IClientNotificationFilter m_filter;

  public ClientNotificationQueueEvent(IClientNotification notification, IClientNotificationFilter filter, int type) {
    m_notification = notification;
    m_filter = filter;
    m_type = type;
  }

  public int getType() {
    return m_type;
  }

  public IClientNotification getNotification() {
    return m_notification;
  }

  public IClientNotificationFilter getFilter() {
    return m_filter;
  }
}
