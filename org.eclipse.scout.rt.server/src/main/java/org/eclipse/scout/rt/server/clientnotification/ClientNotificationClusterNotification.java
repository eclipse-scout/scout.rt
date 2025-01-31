/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import java.io.Serializable;
import java.util.Collection;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

public class ClientNotificationClusterNotification implements Serializable {
  private static final long serialVersionUID = -8513131031858145786L;
  private final Collection<? extends ClientNotificationMessage> m_cnMessages;

  public ClientNotificationClusterNotification(Collection<? extends ClientNotificationMessage> messages) {
    m_cnMessages = messages;
  }

  public Collection<? extends ClientNotificationMessage> getClientNotificationMessages() {
    return m_cnMessages;
  }

  @Override
  public String toString() {
    ToStringBuilder tsb = new ToStringBuilder(this);
    tsb.attr("ClientNotificationMessage", m_cnMessages);
    return tsb.toString();
  }
}
