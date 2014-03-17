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

public class ClientNotificationQueueEvent implements Serializable {
  private static final long serialVersionUID = 1L;

  public enum EventType {
    NEW, UPDATE, REMOVE
  }

  private EventType m_type;
  private ClientNotificationNotification m_notification;

  public ClientNotificationQueueEvent(ClientNotificationNotification notification, EventType type) {
    m_notification = notification;
    m_type = type;
  }

  public EventType getType() {
    return m_type;
  }

  public ClientNotificationNotification getNotification() {
    return m_notification;
  }
}
