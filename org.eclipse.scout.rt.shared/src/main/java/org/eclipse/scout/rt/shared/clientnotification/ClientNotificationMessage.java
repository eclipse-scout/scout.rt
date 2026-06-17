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

import java.io.Serial;
import java.io.Serializable;

import org.eclipse.scout.rt.platform.util.ToStringBuilder;

/**
 * TODO Cleanup implementation and remove {@link ClientNotificationMessage#isDistributeOverCluster()} flag.
 * This requires an updated client notification dispatching to ensure all Scout backends (cluster nodes) are able to dispatch
 * any client notification for an arbitrary client node.
 */
public class ClientNotificationMessage implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  private final IClientNotificationAddress m_address;
  private final Serializable m_notification;
  /**
   * @deprecated This flag is deprecated and will be removed in a future release.
   * Publishing client notifications without distributing the notification to all backend cluster nodes will no longer be supported in the future (e.g. m_distributeOverCluster will always be true).
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  private final boolean m_distributeOverCluster;
  private final String m_correlationId;

  @Deprecated
  public ClientNotificationMessage(final IClientNotificationAddress address, final Serializable notification, final boolean distributeOverCluster, final String correlationId) {
    m_address = address;
    m_notification = notification;
    m_distributeOverCluster = distributeOverCluster;
    m_correlationId = correlationId;
  }

  public ClientNotificationMessage(final IClientNotificationAddress address, final Serializable notification, final String correlationId) {
    this(address, notification, true, correlationId);
  }

  public IClientNotificationAddress getAddress() {
    return m_address;
  }

  /**
   * @deprecated This flag is deprecated and will be removed in a future release.
   * Publishing client notifications without distributing the notification to all backend cluster nodes will no longer be supported in the future (e.g. m_distributeOverCluster will always be true).
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
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
