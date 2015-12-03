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
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;

public class ClientNotificationMessage implements Serializable {
  private static final long serialVersionUID = 1L;

  private final ClientNotificationAddress m_address;
  private final Serializable m_notification;
  private final boolean m_distributeOverCluster;

  public ClientNotificationMessage(ClientNotificationAddress address, Serializable notification, boolean distributeOverCluster) {
    m_address = address;
    m_notification = notification;
    m_distributeOverCluster = distributeOverCluster;
  }

  public ClientNotificationAddress getAddress() {
    return m_address;
  }

  public boolean isDistributeOverCluster() {
    return m_distributeOverCluster;
  }

  public Serializable getNotification() {
    return m_notification;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Adress={").append(getAddress()).append("}").append(", ");
    builder.append("notification=").append(getNotification());
    return builder.toString();
  }
}
