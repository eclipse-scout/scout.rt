/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.clustersync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.security.auth.Subject;

import org.eclipse.scout.rt.dataobject.id.NodeId;
import org.eclipse.scout.rt.mom.api.ClusterMom;
import org.eclipse.scout.rt.mom.api.IMessage;
import org.eclipse.scout.rt.mom.api.IMessageListener;
import org.eclipse.scout.rt.mom.api.ISubscription;
import org.eclipse.scout.rt.mom.api.MOM;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.PlatformEvent;
import org.eclipse.scout.rt.platform.cache.InvalidateCacheNotification;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.ServerConfigProperties.ClusterSyncUserProperty;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.mom.IClusterMomDestinations;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSynchronizationService implements IClusterSynchronizationService, IMessageListener<IClusterNotificationMessage> {
  private static final Logger LOG = LoggerFactory.getLogger(ClusterSynchronizationService.class);

  private static final String TRANSACTION_MEMBER_ID = ClusterSynchronizationService.class.getName();

  private final ClusterNodeStatusInfo m_statusInfo = new ClusterNodeStatusInfo();
  private final ConcurrentMap<MessageStatusMapKey, ClusterNodeStatusInfo> m_messageStatusMap = new ConcurrentHashMap<>();

  private final Subject m_subject;
  private final LazyValue<User> m_user = new LazyValue<>(this::createUser);

  private volatile ISubscription m_subscription;
  private final Object m_subscriptionLock = new Object();

  private final NodeId m_nodeId = NodeId.current();

  public ClusterSynchronizationService() {
    m_subject = CONFIG.getPropertyValue(ClusterSyncUserProperty.class);
  }

  public User getUser() {
    return m_user.get();
  }

  public Subject getSubject() {
    return m_subject;
  }

  protected User createUser() {
    return BEANS.get(IAccessControlService.class).getUser(getSubject());
  }

  @Override
  public IClusterNodeStatusInfo getStatusInfo() {
    return m_statusInfo.getStatus();
  }

  protected ClusterNodeStatusInfo getStatusInfoInternal() {
    return m_statusInfo;
  }

  protected ClusterNodeStatusInfo getStatusInfoInternal(MessageStatusMapKey key) {
    return m_messageStatusMap.computeIfAbsent(key, k -> new ClusterNodeStatusInfo());
  }

  public NodeId getNodeId() {
    return m_nodeId;
  }

  @Override
  public boolean isEnabled() {
    return m_subscription != null;
  }

  @Override
  public boolean enable() {
    if (isEnabled()) {
      return true;
    }

    if (BEANS.get(ClusterMom.class).isNullTransport()) {
      LOG.info("Cluster synchronization is not enabled.");
      return false;
    }

    synchronized (m_subscriptionLock) {
      if (isEnabled()) {
        return true;
      }

      try {
        m_subscription = MOM.subscribe(ClusterMom.class, IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC, this, null);
      }
      catch (RuntimeException e) {
        LOG.error("Failed to subscribe to {}", IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC, e);
        return false;
      }
    }

    return true;
  }

  @Override
  public boolean disable() {
    if (!isEnabled()) {
      return true;
    }

    final ISubscription subscription;
    synchronized (m_subscriptionLock) {
      subscription = m_subscription;
      m_subscription = null;
    }

    try {
      if (subscription != null) {
        subscription.dispose();
      }
    }
    catch (RuntimeException e) {
      LOG.error("Failed to unsubscribe from {}", IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC, e);
    }
    return true;
  }

  @Override
  public void publishTransactional(Serializable notification) {
    if (isEnabled()) {
      LOG.trace("Adding {} to transaction", notification);
      getTransaction().addMessage(new ClusterNotificationMessage(notification, getNotificationProperties()));
    }
  }

  @Override
  public void publish(Serializable notification) {
    publishAll(CollectionUtility.arrayList(notification));
  }

  private void publishAll(Collection<Serializable> notifications) {
    if (isEnabled()) {
      List<IClusterNotificationMessage> internalMessages = new ArrayList<>();
      for (Serializable n : notifications) {
        internalMessages.add(new ClusterNotificationMessage(n, getNotificationProperties()));
      }
      publishInternal(internalMessages);
    }
  }

  /**
   * Publish and update status.
   */
  private void publishInternal(List<IClusterNotificationMessage> messages) {
    for (IClusterNotificationMessage message : messages) {
      LOG.trace("Publishing {}", message);
      MOM.publish(ClusterMom.class, IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC, message);
      updateSentStatusInfo(message);
    }
  }

  protected void updateSentStatusInfo(IClusterNotificationMessage message) {
    getStatusInfoInternal().updateSentStatus(message);
    getStatusInfoInternal(createStatusInfoMapKey(message)).updateSentStatus(message);
  }

  protected void updateReceiveStatusInfo(IClusterNotificationMessage message) {
    getStatusInfoInternal().updateReceiveStatus(message);
    getStatusInfoInternal(createStatusInfoMapKey(message)).updateReceiveStatus(message);
  }

  protected MessageStatusMapKey createStatusInfoMapKey(IClusterNotificationMessage message) {
    Serializable notification = message.getNotification();
    String identifier = null;
    if (notification instanceof InvalidateCacheNotification invalidateCacheNotification) {
      identifier = invalidateCacheNotification.getCacheId();
    }
    return new MessageStatusMapKey(notification.getClass(), identifier);
  }

  @Override
  public IClusterNotificationProperties getNotificationProperties() {
    String userId = StringUtility.emptyIfNull(User.currentUserId());
    return new ClusterNotificationProperties(m_nodeId, userId);
  }

  @Override
  public void onMessage(IMessage<IClusterNotificationMessage> message) {
    final IClusterNotificationMessage notificationMessage = message.getTransferObject();
    if (isEnabled()) {
      //Do not progress notifications sent by node itself
      NodeId originNode = notificationMessage.getProperties().getOriginNode();

      if (m_nodeId.equals(originNode)) {
        LOG.trace("Ignoring {} (reason: own node)", notificationMessage);
        return;
      }

      LOG.trace("Handling {}", notificationMessage);
      handleMessage(notificationMessage);
    }
  }

  protected void handleMessage(IClusterNotificationMessage notificationMessage) {
    updateReceiveStatusInfo(notificationMessage);

    createRunContext().run(() -> {
      NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
      reg.notifyNotificationHandlers(notificationMessage.getNotification());
    });
  }

  protected ServerRunContext createRunContext() {
    return ServerRunContexts.empty()
        .withSubject(getSubject())
        .withUser(getUser());
  }

  /**
   * @return transaction member for publishing messages within a transaction
   */
  protected ClusterSyncTransactionMember getTransaction() {
    ITransaction tx = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required");
    ClusterSyncTransactionMember m = (ClusterSyncTransactionMember) tx.getMember(TRANSACTION_MEMBER_ID);
    if (m == null) {
      m = new ClusterSyncTransactionMember(TRANSACTION_MEMBER_ID);
      tx.registerMember(m);
    }
    return m;
  }

  /**
   * Transaction member that notifies other cluster nodes after the causing Scout transaction has been committed. This
   * ensures that other cluster nodes are not informed too early.
   */
  protected class ClusterSyncTransactionMember extends AbstractTransactionMember {
    private List<IClusterNotificationMessage> m_messageQueue;

    ClusterSyncTransactionMember(String transactionId) {
      super(transactionId);
      m_messageQueue = new LinkedList<>();
    }

    public synchronized void addMessage(IClusterNotificationMessage m) {
      m_messageQueue.add(m);
      m_messageQueue = BEANS.get(ClusterNotificationMessageCoalescer.class).coalesce(m_messageQueue);
    }

    @Override
    public synchronized boolean needsCommit() {
      return !m_messageQueue.isEmpty();
    }

    @Override
    public synchronized void commitPhase2() {
      publishInternal(m_messageQueue);
    }

    @Override
    public synchronized void rollback() {
      m_messageQueue.clear();
    }
  }

  @Override
  public IClusterNodeStatusInfo getStatusInfo(Class<? extends Serializable> messageType) {
    return getStatusInfoInternal(new MessageStatusMapKey(messageType)).getStatus();
  }

  @Override
  public IClusterNodeStatusInfo getCacheInvalidationStatusInfo(String cacheId) {
    return getStatusInfoInternal(new MessageStatusMapKey(InvalidateCacheNotification.class, cacheId)).getStatus();
  }

  protected record MessageStatusMapKey(Class<? extends Serializable> messageType, String identifier) {

    public MessageStatusMapKey {
      if (identifier == null && InvalidateCacheNotification.class.isAssignableFrom(messageType)) {
        //noinspection LoggingPlaceholderCountMatchesArgumentCount
        LOG.warn("For {} the cacheId is required as identifier", InvalidateCacheNotification.class.getSimpleName(), LOG.isDebugEnabled() ? new Exception("stacktrace") : null);
      }
    }

    public MessageStatusMapKey(Class<? extends Serializable> messageType) {
      this(messageType, null);
    }
  }

  /**
   * {@link IPlatformListener} to shutdown this cluster synchronization service upon platform shutdown.
   */
  @Order(IClusterSynchronizationService.DESTROY_ORDER)
  public static class PlatformListener implements IPlatformListener {

    @Override
    public void stateChanged(final PlatformEvent event) {
      if (event.getState() == State.PlatformStopping) {
        for (final ClusterSynchronizationService service : BEANS.all(ClusterSynchronizationService.class)) {
          service.disable();
        }
      }
    }
  }
}
