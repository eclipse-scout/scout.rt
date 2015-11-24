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
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * TODO [jgu] rename<br>
 * Collector for client notifications issued during processing of a service request, and which are to be included in the
 * request's response upon successful commit (piggyback). The collector is not active anymore when it is
 *
 * @see ClientNotificationTransactionMember
 */
public class TransactionalClientNotificationCollector {

  /**
   * The {@link TransactionalClientNotificationCollector} which is currently associated with the current thread.
   */
  public static final ThreadLocal<TransactionalClientNotificationCollector> CURRENT = new ThreadLocal<>();

  private final List<ClientNotificationMessage> m_notifications = new ArrayList<>();
  private final ReadWriteLock m_lock = new ReentrantReadWriteLock();
  private boolean m_active = true;

  /**
   * @return <code>true</code>, if the collector is active and it is possible to add notifications
   */
  public boolean isActive() {
    m_lock.readLock().lock();
    try {
      return m_active;
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Messages are only added, if the collector is active
   *
   * @param messages
   * @return <code>true</code>, if the messages was added
   */
  public boolean addAll(Collection<ClientNotificationMessage> messages) {
    m_lock.readLock().lock();
    try {
      if (!isActive()) {
        return false;
      }
      return m_notifications.addAll(messages);
    }
    finally {
      m_lock.readLock().unlock();
    }
  }

  /**
   * Consumes the values and deactivates the collector
   *
   * @return the collected {@link ClientNotificationMessage}s
   */
  public List<ClientNotificationMessage> values() {
    m_lock.writeLock().lock();
    try {
      m_active = false;
      return new ArrayList<>(m_notifications);
    }
    finally {
      m_lock.writeLock().unlock();
    }

  }
}
