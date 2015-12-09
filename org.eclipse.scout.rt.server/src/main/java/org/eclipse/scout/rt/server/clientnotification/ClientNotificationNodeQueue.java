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
package org.eclipse.scout.rt.server.clientnotification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.FinalValue;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A queue for a client node, that keeps track of notifications for that node.
 */
@Bean
public class ClientNotificationNodeQueue {
  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationNodeQueue.class);

  private final FinalValue<String> m_nodeId = new FinalValue<>();

  private final Integer m_capacity;
  private final BlockingDeque<ClientNotificationMessage> m_notifications;

  private final ReentrantReadWriteLock m_sessionUserCacheLock = new ReentrantReadWriteLock();
  private final Set<String /*sessionId*/> m_sessions = new HashSet<>();
  private final Map<String /*userId*/, Set<String /*sessionId*/>> m_userToSessions = new HashMap<>();
  private final AtomicLong m_lastConsumeAccess;

  public ClientNotificationNodeQueue() {
    this(CONFIG.getPropertyValue(ClientNotificationProperties.NodeQueueCapacity.class));
  }

  public ClientNotificationNodeQueue(int capacity) {
    m_capacity = capacity;
    m_notifications = new LinkedBlockingDeque<>(capacity);
    m_lastConsumeAccess = new AtomicLong(System.currentTimeMillis());
  }

  public void setNodeId(String nodeId) {
    m_nodeId.set(nodeId);
  }

  public String getNodeId() {
    return m_nodeId.get();
  }

  /**
   * @return capacity of queue. If maximum capacity is reached, messages are dropped.
   */
  public int getCapacity() {
    return m_capacity;
  }

  public void registerSession(String sessionId, String userId) {
    Assertions.assertNotNull(sessionId);
    Assertions.assertNotNull(userId);
    m_sessionUserCacheLock.writeLock().lock();
    try {
      m_sessions.add(sessionId);
      Set<String> userSessions = m_userToSessions.get(sessionId);
      if (userSessions == null) {
        userSessions = new HashSet<String>();
        m_userToSessions.put(userId, userSessions);
      }
      userSessions.add(sessionId);
    }
    finally {
      m_sessionUserCacheLock.writeLock().unlock();
    }
  }

  public void unregisterSession(String sessionId, String userId) {
    Assertions.assertNotNull(sessionId);
    Assertions.assertNotNull(userId);
    m_sessionUserCacheLock.writeLock().lock();
    try {
      m_sessions.remove(sessionId);
      Iterator<Entry<String, Set<String>>> iterator = m_userToSessions.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<String, Set<String>> entry = iterator.next();
        Set<String> sessions = entry.getValue();
        if (sessions.contains(sessionId)) {
          sessions.remove(sessionId);
        }
        if (sessions.isEmpty()) {
          iterator.remove();
        }
      }
    }
    finally {
      m_sessionUserCacheLock.writeLock().unlock();
    }
  }

  public void put(ClientNotificationMessage notification) {
    put(CollectionUtility.arrayList(notification));
  }

  public void put(Collection<? extends ClientNotificationMessage> notificationInput) {
    List<ClientNotificationMessage> notifications = getRelevantNotifications(notificationInput);
    putDroppingOld(notifications);
  }

  /**
   * Put notifications into queue and drop oldest ones, if capacity is reached.
   */
  private void putDroppingOld(Collection<? extends ClientNotificationMessage> notifications) {
    int dropCount = 0;
    for (ClientNotificationMessage message : notifications) {
      boolean inserted = m_notifications.offer(message);
      while (!inserted) {
        ClientNotificationMessage removed = m_notifications.poll();
        if (removed != null) {
          dropCount++;
        }
        inserted = m_notifications.offer(message);
      }
    }
    if (dropCount > 0) {
      LOG.warn("Notification queue capacity reached. Remove oldest {} notification messages.", dropCount);
    }
  }

  /**
   * @return time since messages have last been consumed
   */
  public long getLastConsumeAccess() {
    return m_lastConsumeAccess.get();
  }

  public List<ClientNotificationMessage> consume(int maxAmount, long maxWaitTime, TimeUnit unit) {
    m_lastConsumeAccess.set(System.currentTimeMillis());

    List<ClientNotificationMessage> result = getNotifications(maxAmount, maxWaitTime, unit);
    LOG.debug("consumed {} notifications.", result.size());
    return result;
  }

  protected List<ClientNotificationMessage> getNotifications(int maxAmount, long maxWaitTime, TimeUnit unit) {
    List<ClientNotificationMessage> collected = new LinkedList<>();
    try {
      //blocking wait to get first message
      ClientNotificationMessage next = m_notifications.poll(maxWaitTime, unit);
      if (next != null) {
        collected.add(next);
      }

      //add more available notifications
      //with short wait timeout to not go back with one notification when some are about to pop up.
      int timeout = 234; // 0 for no reschedule
      while (next != null && collected.size() < maxAmount) {
        next = m_notifications.poll(timeout, TimeUnit.MILLISECONDS);
        if (next != null) {
          collected.add(next);
        }
      }
    }
    catch (InterruptedException e) {
      LOG.warn("Interrupted while waiting for client notification messages", e);
    }
    return collected;
  }

  private List<ClientNotificationMessage> getRelevantNotifications(Collection<? extends ClientNotificationMessage> notificationInput) {
    List<ClientNotificationMessage> notifications = new ArrayList<ClientNotificationMessage>(notificationInput);
    Iterator<ClientNotificationMessage> it = notifications.iterator();
    while (it.hasNext()) {
      if (!isRelevant(it.next().getAddress())) {
        it.remove();
      }
    }
    return notifications;
  }

  public boolean isRelevant(ClientNotificationAddress address) {
    return address.isNotifyAllSessions() || address.isNotifyAllNodes() || CollectionUtility.containsAny(getAllSessionIds(), address.getSessionIds()) || CollectionUtility.containsAny(getAllUserIds(), address.getUserIds());
  }

  public Set<String /*sessionId*/> getAllSessionIds() {
    m_sessionUserCacheLock.readLock().lock();
    try {
      return new HashSet<String>(m_sessions);
    }
    finally {
      m_sessionUserCacheLock.readLock().unlock();
    }
  }

  public Set<String> getAllUserIds() {
    m_sessionUserCacheLock.readLock().lock();
    try {
      return new HashSet<String>(m_userToSessions.keySet());
    }
    finally {
      m_sessionUserCacheLock.readLock().unlock();
    }

  }

}
