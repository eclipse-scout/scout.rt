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
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.CompareUtility;
import org.eclipse.scout.commons.FinalValue;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.server.services.common.security.LogoutService;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 *
 */
@Bean
public class ClientNotificationNodeQueue {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogoutService.class);

  private final FinalValue<String> m_nodeId = new FinalValue<>();
  private final FinalValue<Integer> m_capacity = new FinalValue<>();

  private final List<ClientNotificationMessage> m_notifications = new LinkedList<ClientNotificationMessage>();
  private final Object m_sessionCacheLock = new Object();
  private final Map<String /*sessionId*/, String /*userId*/> m_sessionsToUser = new HashMap<>();
  private final Map<String /*userId*/, Set<String /*sessionId*/>> m_userToSessions = new HashMap<>();

  @PostConstruct
  protected void initConfig() {
    // TODO aho make configurable
    m_capacity.setValue(200);
  }

  public void setNodeId(String nodeId) {
    m_nodeId.setValue(nodeId);
  }

  public String getNodeId() {
    return m_nodeId.getValue();
  }

  public int getCapacity() {
    return m_capacity.getValue();
  }

  /**
   * @param userId
   */
  public void registerSession(String sessionId, String userId) {
    Assertions.assertNotNull(sessionId);
    Assertions.assertNotNull(userId);
    synchronized (m_sessionCacheLock) {
      m_sessionsToUser.put(sessionId, userId);
      Set<String> userSessions = m_userToSessions.get(sessionId);
      if (userSessions == null) {
        userSessions = new HashSet<String>();
        m_userToSessions.put(userId, userSessions);
      }
      userSessions.add(sessionId);
    }
  }

  public void put(ClientNotificationMessage notification) {
    put(CollectionUtility.arrayList(notification));
  }

  public void put(Collection<? extends ClientNotificationMessage> notificationInput) {
    List<ClientNotificationMessage> notifications = new ArrayList<ClientNotificationMessage>(notificationInput);
    Iterator<ClientNotificationMessage> it = notifications.iterator();
    while (it.hasNext()) {
      if (!isRelevant(it.next())) {
        it.remove();
      }
    }
    if (notifications.isEmpty()) {
      return;
    }
    synchronized (m_notifications) {
      int newSize = m_notifications.size() + notifications.size();
      if (newSize > getCapacity()) {
        LOG.warn(String.format("Notification queue capacity reached. Remove oldest %s notification messages.", newSize - getCapacity()));
        for (; newSize > getCapacity(); newSize--) {
          m_notifications.remove(0);
        }
      }
      m_notifications.addAll(notifications);
      m_notifications.notify();
    }
  }

  public boolean isRelevant(ClientNotificationMessage message) {
    if (CompareUtility.equals(getNodeId(), message.getAddress().getExcludeNodeId())) {
      return false;
    }
    if (message.getAddress().isNotifyAllSessions()) {
      return true;
    }
    Set<String> messageSessionIds = message.getAddress().getSessionIds();
    if (CollectionUtility.hasElements(messageSessionIds)) {
      // check message session ids registered in this Notificaiton Node
      Set<String> sessionIds = getAllSessionIds();
      sessionIds.retainAll(messageSessionIds);
      if (!sessionIds.isEmpty()) {
        return true;
      }
    }
    Set<String> messageUserIds = message.getAddress().getUserIds();
    if (CollectionUtility.hasElements(messageUserIds)) {
      Set<String> userIds = getAllUserIds();
      userIds.retainAll(messageUserIds);
      if (!userIds.isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public void releaseAllWaitingThreads() {
    synchronized (m_notifications) {
      m_notifications.notifyAll();
    }
  }

  public List<ClientNotificationMessage> consume(int maxAmount, long amount, TimeUnit unit) {
    List<ClientNotificationMessage> result = new ArrayList<ClientNotificationMessage>(maxAmount);
    getNotifications(maxAmount, amount, unit, result, true);
    LOG.debug(String.format("consumed %s notifications.", result.size()));
    return result;
  }

  protected void getNotifications(int maxAmount, long amount, TimeUnit unit, List<ClientNotificationMessage> collector, boolean reschedule) {
    synchronized (m_notifications) {
      if (m_notifications.isEmpty()) {
        try {
          m_notifications.wait(unit.toMillis(amount));
        }
        catch (InterruptedException e) {
          LOG.warn("Consume notification thread waiting for notifications interrupted.");
        }
      }
      Iterator<ClientNotificationMessage> it = m_notifications.iterator();
      int itemCount = collector.size();
      while (it.hasNext() && itemCount < maxAmount) {
        collector.add(it.next());
        it.remove();
        itemCount++;
      }
      // Optimization to not go back with one notification when some are about to pop up.
      if (reschedule && itemCount < maxAmount) {
        getNotifications(maxAmount, 234, TimeUnit.MILLISECONDS, collector, false);
      }
    }
  }

  public Set<String /*sessionId*/> getAllSessionIds() {
    final Set<String> sessionIds;
    synchronized (m_sessionCacheLock) {
      sessionIds = new HashSet<String>(m_sessionsToUser.keySet());
    }
    return sessionIds;
  }

  /**
   * @return
   */
  public Set<String> getAllUserIds() {
    final Set<String> userIds;
    synchronized (m_sessionCacheLock) {
      userIds = new HashSet<String>(m_userToSessions.keySet());
    }
    return userIds;
  }

}
