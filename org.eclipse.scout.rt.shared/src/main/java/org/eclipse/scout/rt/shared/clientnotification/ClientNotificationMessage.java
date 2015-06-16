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
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;

/**
 *
 */
public class ClientNotificationMessage implements Serializable {
  private static final long serialVersionUID = 1L;

  private final ClientNotficationAddress m_address;
  private final Serializable m_notification;

  public ClientNotificationMessage(ClientNotficationAddress address, Serializable notification) {
    m_address = address;
    m_notification = notification;
  }

  public ClientNotficationAddress getAddress() {
    return m_address;
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
