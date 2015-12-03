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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClientNotificationRegistry} is the registry for all notifications. It keeps a
 * {@link ClientNotificationNodeQueue} for each notification node (usually a client node). The
 * {@link ClientNotificationService} consumes the notifications per node. The consumption of the notifications waits for
 * a given timeout for notifications. If no notifications are scheduled within this timeout the lock will be released
 * and returns without any notifications. In case a notification gets scheduled during this timeout the request will be
 * released immediately.
 */
@ApplicationScoped
public class ClientNotificationRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationRegistry.class);
  private final Map<String /*notificationNodeId*/, ClientNotificationNodeQueue> m_notificationQueues = new HashMap<>();

  /**
   * If no message is consumed for a certain amount of time [ms], queues are removed to avoid overflows. This may
   * happen, if a node does not properly unregister (e.g. due to a crash).
   */
  private final int m_queueExpireTime;

  public ClientNotificationRegistry() {
    this(Assertions.assertNotNull(CONFIG.getPropertyValue(ClientNotificationProperties.NotificationQueueExpireTime.class)));
  }

  public ClientNotificationRegistry(int queueRemoveTimeout) {
    m_queueExpireTime = queueRemoveTimeout;
  }

  /**
   * Register a session with corresponding user for a given node
   */
  void registerSession(String nodeId, String sessionId, String userId) {
    synchronized (m_notificationQueues) {
      getQueue(nodeId).registerSession(sessionId, userId);
    }
  }

  /**
   * Unregister a session with the corresponding user for a specific node. No notifications are consumed anymore for
   * this session.
   */
  void unregisterSession(String nodeId, String sessionId, String userId) {
    synchronized (m_notificationQueues) {
      ClientNotificationNodeQueue queue = getQueue(nodeId);
      queue.unregisterSession(sessionId, userId);
      if (queue.getAllSessionIds().isEmpty()) {
        m_notificationQueues.remove(nodeId);
      }
    }
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   *
   * @param nodeId
   */
  void unregisterNode(String nodeId) {
    synchronized (m_notificationQueues) {
      m_notificationQueues.remove(nodeId);
    }
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   *
   * @param notificationNodeId
   * @param maxAmount
   *          maximum number of notifications to be consumed
   * @param maxWaitTime
   *          maximum waiting time for new notifications
   * @param unit
   *          time unit for maxWaitTime
   * @return
   */
  List<ClientNotificationMessage> consume(String notificationNodeId, int maxAmount, int maxWaitTime, TimeUnit unit) {
    ClientNotificationNodeQueue queue = getQueue(notificationNodeId);
    return queue.consume(maxAmount, maxWaitTime, unit);
  }

  private ClientNotificationNodeQueue getQueue(String nodeId) {
    Assertions.assertNotNull(nodeId);
    synchronized (m_notificationQueues) {
      ClientNotificationNodeQueue queue = m_notificationQueues.get(nodeId);
      if (queue == null) {
        // create new
        queue = BEANS.get(ClientNotificationNodeQueue.class);
        queue.setNodeId(nodeId);
        m_notificationQueues.put(nodeId, queue);
      }
      return queue;
    }
  }

  /**
   * To access all session id's having to whom notifications will be provided by this server node.
   *
   * @return
   */
  public Set<String> getRegisteredSessionIds() {
    Set<String> allSessionIds = new HashSet<>();
    synchronized (m_notificationQueues) {
      for (ClientNotificationNodeQueue queue : m_notificationQueues.values()) {

        allSessionIds.addAll(queue.getAllSessionIds());
      }
    }
    return allSessionIds;
  }

  /**
   * Nodes that have been registered with {@link #registerSession(String, String, String)}
   */
  public Set<String> getRegisteredNodeIds() {
    synchronized (m_notificationQueues) {
      return new HashSet<>(m_notificationQueues.keySet());
    }
  }

  // put methods
  public void putForUser(String userId, Serializable notification) {
    putForUser(userId, notification, true);
  }

  /**
   * The notification will be distributed to all sessions of the given userId.
   *
   * @param userId
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putForUser(String userId, Serializable notification, boolean distributeOverCluster) {
    putForUsers(Collections.singleton(userId), notification, distributeOverCluster);
  }

  public void putForUsers(Set<String> userIds, Serializable notification) {
    putForUsers(userIds, notification, true);
  }

  /**
   * The notification will be distributed to all sessions of the given userIds.
   *
   * @param userIds
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putForUsers(Set<String> userIds, Serializable notification, boolean distributeOverCluster) {
    publish(ClientNotificationAddress.createUserAddress(userIds), notification, distributeOverCluster);
  }

  public void putForSession(String sessionId, Serializable notification) {
    putForSession(sessionId, notification, true);
  }

  /**
   * The notification will be distributed to the session addressed with the unique sessionId.
   *
   * @param sessionId
   *          the addressed session
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putForSession(String sessionId, Serializable notification, boolean distributeOverCluster) {
    publish(ClientNotificationAddress.createSessionAddress(Collections.singleton(sessionId)), notification, distributeOverCluster);
  }

  public void putForAllSessions(Serializable notification) {
    putForAllSessions(notification, true);
  }

  /**
   * This notification will be distributed to all sessions.
   *
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putForAllSessions(Serializable notification, boolean distributeOverCluster) {
    publish(ClientNotificationAddress.createAllSessionsAddress(), notification, distributeOverCluster);
  }

  public void putForAllNodes(Serializable notification) {
    putForAllNodes(notification, true);
  }

  /**
   * This notification will be distributed to client nodes.
   *
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putForAllNodes(Serializable notification, boolean distributeOverCluster) {
    publish(ClientNotificationAddress.createAllNodesAddress(), notification, distributeOverCluster);
  }

  public void publish(ClientNotificationAddress address, Serializable notification) {
    publish(address, notification, true);
  }

  public void publish(ClientNotificationAddress address, Serializable notification, boolean distributeOverCluster) {
    publish(Collections.singleton(new ClientNotificationMessage(address, notification, distributeOverCluster)));
  }

  public void publish(Collection<? extends ClientNotificationMessage> messages) {
    putWithoutClusterNotification(messages, null);
    publishClusterInternal(messages);
  }

  public void publish(Collection<? extends ClientNotificationMessage> messages, String excludedUiNodeId) {
    putWithoutClusterNotification(messages, excludedUiNodeId);
    publishClusterInternal(messages);
  }

  private void putWithoutClusterNotification(Collection<? extends ClientNotificationMessage> messages, String excludedUiNodeId) {
    synchronized (m_notificationQueues) {
      final Iterator<ClientNotificationNodeQueue> iter = m_notificationQueues.values().iterator();
      while (iter.hasNext()) {
        final ClientNotificationNodeQueue queue = iter.next();
        if (!queue.getNodeId().equals(excludedUiNodeId)) {
          queue.put(messages);
          if (isQueueExpired(queue)) {
            LOG.debug("Removing expired queue " + queue.getNodeId());
            iter.remove();
          }
        }
      }
    }
  }

  private boolean isQueueExpired(ClientNotificationNodeQueue queue) {
    long now = System.currentTimeMillis();
    long lastAccess = queue.getLastConsumeAccess();
    return (now - lastAccess) > m_queueExpireTime;
  }

  public void putTransactionalForUser(String userId, Serializable notification) {
    putTransactionalForUser(userId, notification, true);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. The notification will be distributed to all sessions of the given
   * userId.
   *
   * @param userId
   *          the addressed user
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putTransactionalForUser(String userId, Serializable notification, boolean distributeOverCluster) {
    putTransactionalForUsers(Collections.singleton(userId), notification, distributeOverCluster);
  }

  public void putTransactionalForUsers(Set<String> userIds, Serializable notification) {
    putTransactionalForUsers(userIds, notification, true);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. The notification will be distributed to all sessions of the given
   * userids.
   *
   * @param userIds
   *          the addressed user
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putTransactionalForUsers(Set<String> userIds, Serializable notification, boolean distributeOverCluster) {
    putTransactional(ClientNotificationAddress.createUserAddress(userIds), notification, distributeOverCluster);
  }

  public void putTransactionalForSession(String sessionId, Serializable notification) {
    putTransactionalForSession(sessionId, notification, true);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. The notification will be distributed to the session addressed
   * with the unique sessionId.
   *
   * @param sessionId
   *          the addressed session
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putTransactionalForSession(String sessionId, Serializable notification, boolean distributeOverCluster) {
    putTransactional(ClientNotificationAddress.createSessionAddress(Collections.singleton(sessionId)), notification, distributeOverCluster);
  }

  public void putTransactionalForAllSessions(Serializable notification) {
    putTransactionalForAllSessions(notification, true);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. This notification will be distributed to all sessions.
   *
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putTransactionalForAllSessions(Serializable notification, boolean distributeOverCluster) {
    putTransactional(ClientNotificationAddress.createAllSessionsAddress(), notification, distributeOverCluster);
  }

  public void putTransactionalForAllNodes(Serializable notification) {
    putTransactionalForAllNodes(notification, true);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. This notification will be distributed to all client nodes.
   *
   * @param notification
   * @param distributeOverCluster
   *          flag to distribute notification over whole cluster
   */
  public void putTransactionalForAllNodes(Serializable notification, boolean distributeOverCluster) {
    putTransactional(ClientNotificationAddress.createAllNodesAddress(), notification, distributeOverCluster);
  }

  public void putTransactional(ClientNotificationAddress address, Serializable notification) {
    putTransactional(address, notification, true);
  }

  public void putTransactional(ClientNotificationAddress address, Serializable notification, boolean distributeOverCluster) {
    putTransactional(new ClientNotificationMessage(address, notification, distributeOverCluster));
  }

  public void putTransactional(ClientNotificationMessage message) {
    ITransaction transaction = Assertions.assertNotNull(ITransaction.CURRENT.get(), "No transaction found on current calling context to register transactional client notification %s", message);
    try {
      ClientNotificationTransactionMember txMember = (ClientNotificationTransactionMember) transaction.getMember(ClientNotificationTransactionMember.TRANSACTION_MEMBER_ID);
      if (txMember == null) {
        txMember = new ClientNotificationTransactionMember(this);
        transaction.registerMember(txMember);
      }
      txMember.addNotification(message);
    }
    catch (RuntimeException e) {
      LOG.warn("Could not register transaction member. The notification will be processed immediately", e);
      publish(Collections.singleton(message));
    }
  }

  /**
   * Publish messages to other cluster nodes. Message not foreseen for cluster distributions are filtered.
   */
  private void publishClusterInternal(Collection<? extends ClientNotificationMessage> messages) {
    Collection<ClientNotificationMessage> filteredMessages = new LinkedList<ClientNotificationMessage>();
    for (ClientNotificationMessage message : messages) {
      if (message.isDistributeOverCluster()) {
        filteredMessages.add(message);
      }
    }
    try {
      IClusterSynchronizationService service = BEANS.get(IClusterSynchronizationService.class);
      service.publish(new ClientNotificationClusterNotification(filteredMessages));
    }
    catch (RuntimeException e) {
      LOG.error("Failed to publish client notification", e);
    }
  }

}
