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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * Collector for client notifications issued during processing of a service request, and which are to be included in the
 * request's response upon successful commit (piggyback). The collector is not active anymore when it is
 *
 * @see ClientNotificationTransactionMember
 */
public class ClientNotificationCollector {

  /**
   * The {@link ClientNotificationCollector} which is currently associated with the current thread.
   */
  public static final ThreadLocal<ClientNotificationCollector> CURRENT = new ThreadLocal<>();

  private final List<ClientNotificationMessage> m_notifications = new ArrayList<>();
  private boolean m_active = true;

  /**
   * @return <code>true</code>, if the collector is active and it is possible to add notifications
   */
  public synchronized boolean isActive() {
    return m_active;
  }

  /**
   * Messages are only added, if the collector is active
   *
   * @param messages
   * @return <code>true</code>, if the messages was added
   */
  public synchronized boolean addAll(Collection<ClientNotificationMessage> messages) {
    if (!isActive()) {
      return false;
    }
    return m_notifications.addAll(messages);
  }

  /**
   * Consumes the values and deactivates the collector
   *
   * @return the collected {@link ClientNotificationMessage}s
   */
  public synchronized List<ClientNotificationMessage> consume() {
    m_active = false;
    List<ClientNotificationMessage> result = new ArrayList<>(m_notifications);
    m_notifications.clear();
    return result;
  }
}
