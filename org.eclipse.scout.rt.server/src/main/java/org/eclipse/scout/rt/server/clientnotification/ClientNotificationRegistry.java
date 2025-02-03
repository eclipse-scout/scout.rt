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

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.NotificationQueueExpireTime;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
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
  private final Map<NodeId, ClientNotificationNodeQueue> m_notificationQueues = new HashMap<>();

  /**
   * If no message is consumed for a certain amount of time [ms], queues are removed to avoid overflows. This may
   * happen, if a node does not properly unregister (e.g. due to a crash).
   */
  private final int m_queueExpireTime;

  public ClientNotificationRegistry() {
    this(Assertions.assertNotNull(CONFIG.getPropertyValue(NotificationQueueExpireTime.class)));
  }

  public ClientNotificationRegistry(int queueRemoveTimeout) {
    m_queueExpireTime = queueRemoveTimeout;
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   */
  protected void registerNode(NodeId nodeId) {
    getOrCreateQueue(nodeId);
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   */
  protected void unregisterNode(NodeId nodeId) {
    synchronized (m_notificationQueues) {
      LOG.info("Removing queue of unregistered node [clientNodeId={}]", nodeId);
      m_notificationQueues.remove(nodeId);
    }
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   *
   * @param maxAmount
   *     maximum number of notifications to be consumed
   * @param maxWaitTime
   *     maximum waiting time for new notifications
   * @param unit
   *     time unit for maxWaitTime
   */
  protected List<ClientNotificationMessage> consume(NodeId notificationNodeId, int maxAmount, int maxWaitTime, TimeUnit unit) {
    ClientNotificationNodeQueue queue = getOrCreateQueue(notificationNodeId);
    return queue.consume(maxAmount, maxWaitTime, unit);
  }

  protected ClientNotificationNodeQueue getOrCreateQueue(NodeId nodeId) {
    Assertions.assertNotNull(nodeId);
    synchronized (m_notificationQueues) {
      return m_notificationQueues.computeIfAbsent(nodeId, this::createNewQueue);
    }
  }

  protected ClientNotificationNodeQueue createNewQueue(NodeId nodeId) {
    ClientNotificationNodeQueue queue = BEANS.get(ClientNotificationNodeQueue.class);
    queue.setNodeId(nodeId);
    return queue;
  }

  /**
   * Nodes that have been registered with {@link #registerNode(NodeId)}
   */
  public Set<NodeId> getRegisteredNodeIds() {
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
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   *     the addressed session
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
   */
  public void putForAllNodes(Serializable notification, boolean distributeOverCluster) {
    publish(ClientNotificationAddress.createAllNodesAddress(), notification, distributeOverCluster);
  }

  public void publish(ClientNotificationAddress address, Serializable notification) {
    publish(address, notification, true);
  }

  public void publish(ClientNotificationAddress address, Serializable notification, boolean distributeOverCluster) {
    publish(Collections.singleton(new ClientNotificationMessage(address, notification, distributeOverCluster, CorrelationId.CURRENT.get())));
  }

  public void publish(Collection<? extends ClientNotificationMessage> messages) {
    publishWithoutClusterNotification(messages, null);
    publishClusterInternal(messages);
  }

  public void publish(Collection<? extends ClientNotificationMessage> messages, NodeId excludedUiNodeId) {
    publishWithoutClusterNotification(messages, excludedUiNodeId);
    publishClusterInternal(messages);
  }

  /**
   * Publish without triggering cluster notification
   */
  public void publishWithoutClusterNotification(Collection<? extends ClientNotificationMessage> messages) {
    publishWithoutClusterNotification(messages, null);
  }

  /**
   * Publish without triggering cluster notification
   *
   * @param excludedUiNodeId
   *     may be <code>null</code>
   */
  public void publishWithoutClusterNotification(Collection<? extends ClientNotificationMessage> messages, NodeId excludedUiNodeId) {
    synchronized (m_notificationQueues) {
      Iterator<ClientNotificationNodeQueue> iter = m_notificationQueues.values().iterator();
      while (iter.hasNext()) {
        ClientNotificationNodeQueue queue = iter.next();
        if (!queue.getNodeId().equals(excludedUiNodeId)) {
          queue.put(messages);
          if (isQueueExpired(queue)) {
            LOG.info("Removing expired queue [clientNodeId={}, lastConsumeAccess={}]", queue.getNodeId(), queue.getLastConsumeAccessFormatted());
            iter.remove();
          }
        }
      }
    }
  }

  protected boolean isQueueExpired(ClientNotificationNodeQueue queue) {
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
   *     the addressed user
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   * userIds.
   *
   * @param userIds
   *     the addressed user
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   *     the addressed session
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
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
   * @param distributeOverCluster
   *     flag to distribute notification over whole cluster
   */
  public void putTransactionalForAllNodes(Serializable notification, boolean distributeOverCluster) {
    putTransactional(ClientNotificationAddress.createAllNodesAddress(), notification, distributeOverCluster);
  }

  public void putTransactional(ClientNotificationAddress address, Serializable notification) {
    putTransactional(address, notification, true);
  }

  public void putTransactional(ClientNotificationAddress address, Serializable notification, boolean distributeOverCluster) {
    putTransactional(new ClientNotificationMessage(address, notification, distributeOverCluster, CorrelationId.CURRENT.get()));
  }

  public void putTransactional(ClientNotificationMessage message) {
    ITransaction transaction = Assertions.assertNotNull(ITransaction.CURRENT.get(), "No transaction found on current calling context to register transactional client notification {}", message);
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
  protected void publishClusterInternal(Collection<? extends ClientNotificationMessage> messages) {
    Collection<ClientNotificationMessage> filteredMessages = new LinkedList<>();
    for (ClientNotificationMessage message : messages) {
      if (message.isDistributeOverCluster()) {
        filteredMessages.add(message);
      }
    }
    // do not publish empty messages
    if (filteredMessages.isEmpty()) {
      return;
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
