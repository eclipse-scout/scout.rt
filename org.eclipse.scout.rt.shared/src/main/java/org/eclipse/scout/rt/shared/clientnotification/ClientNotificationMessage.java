/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.clientnotification;

import java.io.Serializable;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

public class ClientNotificationMessage implements Serializable {
  private static final long serialVersionUID = 1L;

  private final IClientNotificationAddress m_address;
  private final Serializable m_notification;
  private final boolean m_distributeOverCluster;
  private final String m_correlationId;

  public ClientNotificationMessage(final IClientNotificationAddress address, final Serializable notification, final boolean distributeOverCluster, final String correlationId) {
    m_address = address;
    m_notification = notification;
    m_distributeOverCluster = distributeOverCluster;
    m_correlationId = correlationId;
  }

  public IClientNotificationAddress getAddress() {
    return m_address;
  }

  public boolean isDistributeOverCluster() {
    return m_distributeOverCluster;
  }

  public Serializable getNotification() {
    return m_notification;
  }

  public String getCorrelationId() {
    return m_correlationId;
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("address", getAddress());
    builder.attr("notification", getNotification());
    builder.attr("cid", getCorrelationId());
    return builder.toString();
  }
}
