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

import java.io.Serializable;
import java.util.ArrayList;
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
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.NotificationQueueCleanupTime;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationProperties.NotificationQueueExpireTime;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistryClusterNotification.Event;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationAddress;
import org.eclipse.scout.rt.shared.clientnotification.ClientNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClientNotificationRegistry} is the registry for all notifications. It keeps a
 * {@link IClientNotificationNodeQueue} for each notification node (usually a client node). The
 * {@link ClientNotificationService} consumes the notifications per node. The consumption of the notifications waits for
 * a given timeout for notifications. If no notifications are scheduled within this timeout the lock will be released
 * and returns without any notifications. In case a notification gets scheduled during this timeout the request will be
 * released immediately.
 */
@ApplicationScoped
public class ClientNotificationRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(ClientNotificationRegistry.class);

  /**
   * Indicates the order of the client notification registry's {@link IPlatformListener} to shut down itself upon entering
   * platform state {@link State#PlatformStopping}. Any listener depending on client notification registry facility must be
   * configured with an order less than {@code #DESTROY_ORDER}.
   */
  public static final int DESTROY_ORDER = 5_700;

  /**
   * Map of UI server {@link NodeId} to corresponding {@link IClientNotificationNodeQueue}.
   */
  protected final Map<NodeId, IClientNotificationNodeQueue> m_notificationQueues = new HashMap<>();

  /**
   * If no message is consumed for a certain amount of time [ms], queues are removed to avoid overflows. This may
   * happen, if a node does not properly unregister (e.g. due to a crash).
   */
  protected final int m_queueExpireTime;

  /**
   * Handle to dead node cleanup job.
   */
  protected IFuture<Void> m_cleanupJobFuture;

  public ClientNotificationRegistry() {
    this(Assertions.assertNotNull(CONFIG.getPropertyValue(NotificationQueueExpireTime.class)));
  }

  public ClientNotificationRegistry(int queueRemoveTimeout) {
    m_queueExpireTime = queueRemoveTimeout;
  }

  /**
   * Initialize {@link ClientNotificationRegistry}.
   *
   * @see ClientNotificationRegistryPlatformListener
   */
  protected void init() {
    startCleanupJob();
  }

  /**
   * Shutdown {@link ClientNotificationRegistry}.
   *
   * @see ClientNotificationRegistryPlatformListener
   */
  protected void shutdown() {
    stopCleanupJob();
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService} and {@link ClientNotificationRegistryClusterHandler}.
   */
  protected void registerNode(NodeId clientNodeId, boolean distributeOverCluster) {
    LOG.debug("Register node [clientNodeId={}, distributeOverCluster={}]", clientNodeId, distributeOverCluster);
    getOrCreateQueue(clientNodeId);
    if (distributeOverCluster) {
      // Distribute cluster notification even if no new queue was created on this cluster node to ensure all cluster nodes are kept in sync
      // Publishing cluster message outside lock for m_notificationQueues
      publishClusterMessage(new ClientNotificationRegistryClusterNotification(Event.NODE_REGISTERED, clientNodeId));
    }
  }

  /**
   * This method should only be accessed from {@link ClientNotificationService} and {@link ClientNotificationRegistryClusterHandler}.
   */
  protected void unregisterNode(NodeId clientNodeId, boolean distributeOverCluster) {
    LOG.debug("Unregister node [clientNodeId={}, distributeOverCluster={}]", clientNodeId, distributeOverCluster);
    removeQueue(clientNodeId);
    if (distributeOverCluster) {
      // Distribute cluster notification even if no new queue was created on this cluster node to ensure all cluster nodes are kept in sync
      // Publishing cluster message outside lock for m_notificationQueues
      publishClusterMessage(new ClientNotificationRegistryClusterNotification(Event.NODE_UNREGISTERED, clientNodeId));
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
    IClientNotificationNodeQueue queue = getOrCreateQueue(notificationNodeId);
    return queue.consume(maxAmount, maxWaitTime, unit);
  }

  protected IClientNotificationNodeQueue getOrCreateQueue(NodeId clientNodeId) {
    Assertions.assertNotNull(clientNodeId);
    synchronized (m_notificationQueues) {
      return m_notificationQueues.computeIfAbsent(clientNodeId, this::createNewQueue);
    }
  }

  protected IClientNotificationNodeQueue createNewQueue(NodeId clientNodeId) {
    IClientNotificationNodeQueue queue = BEANS.get(IClientNotificationNodeQueue.class);
    queue.init(clientNodeId);
    LOG.info("Created queue for node [clientNodeId={}]", clientNodeId);
    return queue;
  }

  protected void removeQueue(NodeId clientNodeId) {
    synchronized (m_notificationQueues) {
      LOG.info("Removing queue of unregistered node [clientNodeId={}]", clientNodeId);
      m_notificationQueues.remove(clientNodeId);
    }
  }

  /**
   * Nodes that have been registered with {@link #registerNode(NodeId, boolean)}
   */
  public Set<NodeId> getRegisteredClientNodeIds() {
    synchronized (m_notificationQueues) {
      return new HashSet<>(m_notificationQueues.keySet());
    }
  }

  // -------- non-transactional put methods --------

  /**
   * The notification will be distributed to all sessions of the given {@code userId}.
   */
  public void putForUser(String userId, Serializable notification) {
    putForUsers(Collections.singleton(userId), notification);
  }

  /**
   * The notification will be distributed to all sessions of the given {@code userIds}.
   */
  public void putForUsers(Set<String> userIds, Serializable notification) {
    publish(ClientNotificationAddress.createUserAddress(userIds), notification);
  }

  /**
   * The notification will be distributed to the session addressed with the unique {@code sessionId}.
   */
  public void putForSession(String sessionId, Serializable notification) {
    if (StringUtility.isNullOrEmpty(sessionId)) {
      return;
    }
    publish(ClientNotificationAddress.createSessionAddress(Collections.singleton(sessionId)), notification);
  }

  /**
   * This notification will be distributed to all sessions.
   */
  public void putForAllSessions(Serializable notification) {
    publish(ClientNotificationAddress.createAllSessionsAddress(), notification);
  }

  /**
   * This notification will be distributed to client nodes (e.g. UI server nodes).
   */
  public void putForAllNodes(Serializable notification) {
    publish(ClientNotificationAddress.createAllNodesAddress(), notification);
  }

  /**
   * Publishes given notification to given {@code address}.
   */
  public void publish(ClientNotificationAddress address, Serializable notification) {
    publish(Collections.singleton(new ClientNotificationMessage(address, notification, CorrelationId.CURRENT.get())));
  }

  /**
   * Publishes given notifications for UI servers and as cluster notification for the other backends.
   */
  public void publish(Collection<? extends ClientNotificationMessage> messages) {
    publish(messages, null);
  }

  /**
   * Publishes given notifications for UI servers and as cluster notification for the other backends excluding client node {@code excludedClientNodeId}.
   *
   * @param excludedClientNodeId
   *     may be {@code null}
   */
  public void publish(Collection<? extends ClientNotificationMessage> messages, NodeId excludedClientNodeId) {
    publishWithoutClusterNotification(messages, excludedClientNodeId);
    publishClusterInternal(messages);
  }

  /**
   * Publish given notifications for UI servers excluding client node {@code excludedClientNodeId} and without triggering cluster notification for other backends.
   *
   * @param excludedClientNodeId
   *     may be {@code null}
   */
  protected void publishWithoutClusterNotification(Collection<? extends ClientNotificationMessage> messages, NodeId excludedClientNodeId) {
    synchronized (m_notificationQueues) {
      for (IClientNotificationNodeQueue queue : m_notificationQueues.values()) {
        if (!queue.getClientNodeId().equals(excludedClientNodeId)) {
          queue.put(messages);
        }
      }
    }
  }

  /**
   * Publish client notification messages to other backend cluster nodes. Message not foreseen for cluster distributions are filtered.
   */
  protected void publishClusterInternal(Collection<? extends ClientNotificationMessage> messages) {
    Collection<ClientNotificationMessage> filteredMessages = new LinkedList<>();
    for (ClientNotificationMessage message : messages) {
      //noinspection deprecation
      if (message.isDistributeOverCluster()) {
        filteredMessages.add(message);
      }
    }
    // do not publish empty messages
    if (filteredMessages.isEmpty()) {
      return;
    }
    publishClusterMessage(new ClientNotificationClusterNotification(filteredMessages));
  }

  /**
   * Publish cluster message to other backend cluster nodes.
   */
  protected void publishClusterMessage(Serializable message) {
    try {
      IClusterSynchronizationService service = BEANS.get(IClusterSynchronizationService.class);
      service.publish(message);
    }
    catch (RuntimeException e) {
      LOG.error("Failed to publish cluster notification", e);
    }
  }

  // -------- transactional put methods --------

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. The notification will be distributed to all sessions of the given
   * userId.
   *
   * @param userId
   *     the addressed user
   */
  public void putTransactionalForUser(String userId, Serializable notification) {
    putTransactionalForUsers(Collections.singleton(userId), notification);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. The notification will be distributed to all sessions of the given
   * userIds.
   *
   * @param userIds
   *     the addressed user
   */
  public void putTransactionalForUsers(Set<String> userIds, Serializable notification) {
    putTransactional(ClientNotificationAddress.createUserAddress(userIds), notification);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. The notification will be distributed to the session addressed
   * with the unique sessionId.
   *
   * @param sessionId
   *     the addressed session
   */
  public void putTransactionalForSession(String sessionId, Serializable notification) {
    if (StringUtility.isNullOrEmpty(sessionId)) {
      return;
    }
    putTransactional(ClientNotificationAddress.createSessionAddress(Collections.singleton(sessionId)), notification);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. This notification will be distributed to all sessions.
   */
  public void putTransactionalForAllSessions(Serializable notification) {
    putTransactional(ClientNotificationAddress.createAllSessionsAddress(), notification);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. This notification will be distributed to all client nodes.
   */
  public void putTransactionalForAllNodes(Serializable notification) {
    putTransactional(ClientNotificationAddress.createAllNodesAddress(), notification);
  }

  /**
   * To put a notifications with transactional behavior. The notification will be processed on successful commit of the
   * {@link ITransaction} surrounding the server call. <p>
   * <b>This notification will be distributed to all client nodes known to this backend but will not be distributed within cluster to other backend nodes</b>.
   *
   * @deprecated This method will be removed in a future release. Use {@link #putTransactional(ClientNotificationAddress, Serializable)} instead and publish message to all client nodes.
   */
  @Deprecated
  @SuppressWarnings("DeprecatedIsStillUsed")
  public void putTransactionalForAllNodesWithoutClusterNotification(Serializable notification) {
    putTransactional(new ClientNotificationMessage(ClientNotificationAddress.createAllNodesAddress(), notification, false, CorrelationId.CURRENT.get()));
  }

  /**
   * Publishes the given notification transactional to given {@code address}.
   */
  public void putTransactional(ClientNotificationAddress address, Serializable notification) {
    putTransactional(new ClientNotificationMessage(address, notification, CorrelationId.CURRENT.get()));
  }

  /**
   * Publishes the given notification transactional to given {@code address}.
   */
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

  // -------- Cleanup nodes methods --------

  protected void startCleanupJob() {
    int cleanupInterval = Assertions.assertNotNull(CONFIG.getPropertyValue(NotificationQueueCleanupTime.class));
    m_cleanupJobFuture = Jobs.schedule(this::cleanupDeadNodes, Jobs.newInput()
        .withRunContext(ServerRunContexts.empty())
        .withName(ClientNotificationRegistry.class.getSimpleName() + "-cleanup")
        .withExceptionHandling(BEANS.get(ExceptionHandler.class), true)
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(cleanupInterval, TimeUnit.MILLISECONDS))));
  }

  protected void cleanupDeadNodes() {
    List<IClientNotificationNodeQueue> expiredQueues = new ArrayList<>();
    synchronized (m_notificationQueues) {
      LOG.debug("Running job to cleanup queues for dead nodes. Available queues {}", m_notificationQueues.keySet());
      Iterator<IClientNotificationNodeQueue> iter = m_notificationQueues.values().iterator();
      while (iter.hasNext()) {
        IClientNotificationNodeQueue queue = iter.next();
        long queueLastConsumeAccess = queue.getLastConsumeAccess();
        // use queue creation timestamp if messages were never consumed yet, avoid cleaning up new queues which were never consumed within cleanup interval
        long lastAccess = queueLastConsumeAccess != 0 ? queueLastConsumeAccess : queue.getCreateTimestamp();
        if (isQueueExpired(lastAccess)) {
          LOG.info("Removing expired queue [clientNodeId={}, lastConsumeAccess={}]", queue.getClientNodeId(), queue.getLastConsumeAccessFormatted());
          expiredQueues.add(queue);
          iter.remove();
        }
      }
    }

    for (IClientNotificationNodeQueue queue : expiredQueues) {
      LOG.info("Disposing expired queue [clientNodeId={}, lastConsumeAccess={}]", queue.getClientNodeId(), queue.getLastConsumeAccessFormatted());
      queue.dispose();
    }
  }

  protected boolean isQueueExpired(long lastAccess) {
    long now = System.currentTimeMillis();
    return (now - lastAccess) > m_queueExpireTime;
  }

  protected void stopCleanupJob() {
    if (m_cleanupJobFuture == null) {
      return;
    }
    LOG.info("Stopping ClientNotification cleanup job");
    m_cleanupJobFuture.cancel(true);
    m_cleanupJobFuture = null;
  }

  /**
   * {@link IPlatformListener} to initialize and shutdown this {@link ClientNotificationRegistry} upon platform state change.
   */
  @Order(DESTROY_ORDER)
  public static class ClientNotificationRegistryPlatformListener implements IPlatformListener {
    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStarted) {
        BEANS.get(ClientNotificationRegistry.class).init();
      }

      if (event.getState() == State.PlatformStopping) {
        BEANS.get(ClientNotificationRegistry.class).shutdown();
      }
    }
  }
}
