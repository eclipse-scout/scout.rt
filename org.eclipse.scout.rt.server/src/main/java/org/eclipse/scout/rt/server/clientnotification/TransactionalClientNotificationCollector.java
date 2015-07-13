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
package org.eclipse.scout.rt.server.clientnotification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * Collector for transactional client notifications issued during processing of a service request, and which are to be
 * included in the request's response upon successful commit (piggyback).
 *
 * @see ClientNotificationTransactionMember
 */
public class TransactionalClientNotificationCollector {

  /**
   * The {@link TransactionalClientNotificationCollector} which is currently associated with the current thread.
   */
  public static final ThreadLocal<TransactionalClientNotificationCollector> CURRENT = new ThreadLocal<>();

  private final List<ClientNotificationMessage> m_notifications = new ArrayList<>();

  public boolean add(ClientNotificationMessage message) {
    return m_notifications.add(message);
  }

  public void addAll(Collection<ClientNotificationMessage> messages) {
    m_notifications.addAll(messages);
  }

  public List<ClientNotificationMessage> values() {
    return new ArrayList<>(m_notifications);
  }
}
