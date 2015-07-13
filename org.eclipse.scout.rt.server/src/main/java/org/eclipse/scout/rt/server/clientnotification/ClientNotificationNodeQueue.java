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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.FinalValue;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.services.common.security.LogoutService;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * A queue for a client node, that keeps track of notifications for that node.
 */
@Bean
public class ClientNotificationNodeQueue {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogoutService.class);

  private final FinalValue<String> m_nodeId = new FinalValue<>();

  private final Integer m_capacity;
  private final BlockingDeque<ClientNotificationMessage> m_notifications;

  //TODO [aho] only keyset of m_sessionsToUser, m_userToSessions is used. Remove values?
  private final ReentrantReadWriteLock m_sessionUserCacheLock = new ReentrantReadWriteLock();
  private final Map<String /*sessionId*/, String /*userId*/> m_sessionsToUser = new HashMap<>();
  private final Map<String /*userId*/, Set<String /*sessionId*/>> m_userToSessions = new HashMap<>();

  public ClientNotificationNodeQueue() {
    this(CONFIG.getPropertyValue(ClientNotificationProperties.NodeQueueCapacity.class));
  }

  public ClientNotificationNodeQueue(int capacity) {
    m_capacity = capacity;
    m_notifications = new LinkedBlockingDeque<>(capacity);
  }

  public void setNodeId(String nodeId) {
    m_nodeId.setValue(nodeId);
  }

  public String getNodeId() {
    return m_nodeId.getValue();
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
      m_sessionsToUser.put(sessionId, userId);
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

  public void put(ClientNotificationMessage notification) {
    put(CollectionUtility.arrayList(notification));
  }

  public void put(Collection<? extends ClientNotificationMessage> notificationInput) {
    List<ClientNotificationMessage> notifications = getFilteredNotifications(notificationInput);
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
      LOG.warn(String.format("Notification queue capacity reached. Remove oldest %s notification messages.", dropCount));
    }
  }

  public List<ClientNotificationMessage> consume(int maxAmount, long maxWaitTime, TimeUnit unit) {
    List<ClientNotificationMessage> result = getNotifications(maxAmount, maxWaitTime, unit);
    if (LOG.isDebugEnabled()) {
      LOG.debug(String.format("consumed %s notifications.", result.size()));
    }
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
    catch (InterruptedException e1) {
      LOG.warn("Consume notification thread waiting for notifications interrupted.");
    }
    return collected;
  }

  private List<ClientNotificationMessage> getFilteredNotifications(Collection<? extends ClientNotificationMessage> notificationInput) {
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
    if (CompareUtility.equals(getNodeId(), address.getExcludedNodeId())) {
      return false;
    }
    return address.isNotifyAllSessions()
        || address.isNotifyAllNodes()
        || CollectionUtility.containsAny(getAllSessionIds(), address.getSessionIds())
        || CollectionUtility.containsAny(getAllUserIds(), address.getUserIds());
  }

  public Set<String /*sessionId*/> getAllSessionIds() {
    m_sessionUserCacheLock.readLock().lock();
    try {
      return new HashSet<String>(m_sessionsToUser.keySet());
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
