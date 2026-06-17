/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.clientnotification;

import java.io.Serial;
import java.io.Serializable;

import org.eclipse.scout.rt.dataobject.id.NodeId;

/**
 * Cluster notification indicating that a client node registered or unregistered for client notifications in a Scout backend.
 */
public class ClientNotificationRegistryClusterNotification implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  public enum Event {
    NODE_REGISTERED,
    NODE_UNREGISTERED,
  }

  private final Event m_event;
  private final NodeId m_clientNodeId;

  public ClientNotificationRegistryClusterNotification(Event event, NodeId clientNodeId) {
    m_event = event;
    m_clientNodeId = clientNodeId;
  }

  public Event getEvent() {
    return m_event;
  }

  public NodeId getClientNodeId() {
    return m_clientNodeId;
  }
}
