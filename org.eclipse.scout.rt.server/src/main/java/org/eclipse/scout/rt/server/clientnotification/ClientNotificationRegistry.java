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

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;

/**
 * The {@link ClientNotificationRegistry} is the registry for all notifications. It keeps a
 * {@link ClientNotificationNodeQueue} for
 * each notification node (usually a client node).
 * The {@link ClientNotificationService} consumes the notifications per node. The consumption of the notifications waits
 * for a given timeout for notifications. If no notifications are scheduled within this timeout the lock will be
 * released and returns without any notifications. In case a notification gets scheduled during this timeout the
 * request will be released immediately.
 */
@ApplicationScoped
public class ClientNotificationRegistry {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationRegistry.class);
  private final Map<String /*notificationNodeId*/, ClientNotificationNodeQueue> m_notificationQueues = new HashMap<>();

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   *
   * @param notificationNodeId
   * @param session
   */
  void registerSession(String notificationNodeId, String sessionId, String userId) {
    synchronized (m_notificationQueues) {
      ClientNotificationNodeQueue queue = getQueue(notificationNodeId);
      queue.registerSession(sessionId, userId);
    }
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService}
   *
   * @param notificationNodeId
   */
  void unregisterNode(String notificationNodeId) {
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

  private ClientNotificationNodeQueue getQueue(String notificationNodeId) {
    Assertions.assertNotNull(notificationNodeId);
    synchronized (m_notificationQueues) {
      ClientNotificationNodeQueue queue = m_notificationQueues.get(notificationNodeId);
      if (queue == null) {
        // create new
        queue = BEANS.get(ClientNotificationNodeQueue.class);
        queue.setNodeId(notificationNodeId);
        m_notificationQueues.put(notificationNodeId, queue);
      }
      return queue;
    }
  }

  /**
   * To access all session id's having to whom notifications will be providen by this server node.
   *
   * @return
   */
  public Set<String> getAllSessionIds() {
    Set<String> allSessionIds = new HashSet<>();
    synchronized (m_notificationQueues) {
      for (ClientNotificationNodeQueue queue : m_notificationQueues.values()) {
        allSessionIds.addAll(queue.getAllSessionIds());
      }
    }
    return allSessionIds;
  }

  // put methods
  /**
   * The notification will be distributed to all sessions of the given userId.
   *
   * @param userId
   * @param notification
   */
  public void putForUser(String userId, Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createUserAddress(CollectionUtility.hashSet(userId)), notification);
    put(message);
  }

  /**
   * The notification will be distributed to the session addressed with the unique sessionId.
   *
   * @param sessionId
   *          the addressed session
   * @param notification
   */
  public void putForSession(String sessionId, Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createSessionAddress(CollectionUtility.hashSet(sessionId)), notification);
    put(message);
  }

  /**
   * This notification will be distributed to all sessions.
   *
   * @param notification
   */
  public void putForAllSessions(Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createAllSessionsAddress(), notification);
    put(message);
  }

  /**
   * This notification will be distributed to client nodes.
   *
   * @param notification
   */
  public void putForAllNodes(Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createAllNodesAddress(), notification);
    put(message);
  }

  public void put(ClientNotificationMessage message) {
    put(CollectionUtility.arrayList(message));
  }

  public void putWithoutClusterNotification(ClientNotificationMessage message) {
    putWithoutClusterNotification(CollectionUtility.arrayList(message));
  }

  public void put(Collection<? extends ClientNotificationMessage> messages) {
    putWithoutClusterNotification(messages);
    publish(messages);
  }

  public void putWithoutClusterNotification(Collection<? extends ClientNotificationMessage> messages) {
    synchronized (m_notificationQueues) {
      for (ClientNotificationNodeQueue queue : m_notificationQueues.values()) {
        queue.put(messages);
      }
    }
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call.
   * The notification will be distributed to all sessions of the given userId.
   *
   * @param userId
   *          the addressed user
   * @param notification
   */
  public void putTransactionalForUser(String userId, Serializable notification) {
    // exclude the node the request comes from
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createUserAddress(CollectionUtility.hashSet(userId), currentNodeIdElseThrow()), notification);
    putTransactional(message);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call.
   * The notification will be distributed to all sessions of the given userids.
   *
   * @param userIds
   *          the addressed user
   * @param notification
   */
  public void putTransactionalForUsers(Set<String> userIds, Serializable notification) {
    // exclude the node the request comes from
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createUserAddress(userIds, currentNodeIdElseThrow()), notification);
    putTransactional(message);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call.
   * The notification will be distributed to the session addressed with the unique sessionId.
   *
   * @param sessionId
   *          the addressed session
   * @param notification
   */
  public void putTransactionalForSession(String sessionId, Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createSessionAddress(CollectionUtility.hashSet(sessionId), currentNodeIdElseThrow()), notification);
    putTransactional(message);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call.
   * This notification will be distributed to all sessions.
   *
   * @param notification
   */
  public void putTransactionalForAllSessions(Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createAllSessionsAddress(currentNodeIdElseThrow()), notification);
    putTransactional(message);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call.
   * This notification will be distributed to all client nodes.
   *
   * @param notification
   */
  public void putTransactionalForAllNodes(Serializable notification) {
    ClientNotificationMessage message = new ClientNotificationMessage(ClientNotificationAddress.createAllNodesAddress(currentNodeIdElseThrow()), notification);
    putTransactional(message);
  }

  public void putTransactional(ClientNotificationMessage message) {
    Assertions.assertNotNull(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get(), "Missing HTTP servlet response to attach transactional client notification (piggyback)");
    ITransaction transaction = Assertions.assertNotNull(ITransaction.CURRENT.get(), "No transaction found on current calling context to register transactional client notification %s", message);
    try {
      ClientNotificationTransactionMember txMember = (ClientNotificationTransactionMember) transaction.getMember(ClientNotificationTransactionMember.TRANSACTION_MEMBER_ID);
      if (txMember == null) {
        txMember = new ClientNotificationTransactionMember();
        transaction.registerMember(txMember);
      }
      txMember.addNotification(message);
    }
    catch (ProcessingException e) {
      LOG.warn("Could not register transaction member. The notification will be processed immediately", e);
      put(message);
    }
  }

  private void publish(Collection<? extends ClientNotificationMessage> messages) {
    try {
      IClusterSynchronizationService service = BEANS.get(IClusterSynchronizationService.class);
      service.publish(new ClientNotificationClusterNotification(messages));
    }
    catch (ProcessingException e) {
      LOG.error("Error distributing cluster notification ", e);
    }
  }


  private String currentNodeIdElseThrow() {
    return Assertions.assertNotNull(ClientNotificationNodeId.CURRENT.get(), "No 'notification node id' found on current calling context");
  }
}
