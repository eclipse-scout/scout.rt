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
package org.eclipse.scout.rt.server.services.common.clustersync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.security.auth.Subject;

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
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.NodeIdentifier;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.platform.transaction.ITransaction;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.ServerConfigProperties.ClusterSyncUserProperty;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.mom.IClusterMomDestinations;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSynchronizationService implements IClusterSynchronizationService, IMessageListener<IClusterNotificationMessage> {
  private static final Logger LOG = LoggerFactory.getLogger(ClusterSynchronizationService.class);

  private static final String TRANSACTION_MEMBER_ID = ClusterSynchronizationService.class.getName();

  private final EventListenerList m_listenerList = new EventListenerList();
  private final ClusterNodeStatusInfo m_statusInfo = new ClusterNodeStatusInfo();
  private final ConcurrentMap<Class<? extends Serializable>, ClusterNodeStatusInfo> m_messageStatusMap = new ConcurrentHashMap<>();

  private final Subject m_subject;

  private volatile ISubscription m_subscription;
  private final Object m_subscriptionLock = new Object();

  private final String m_nodeId = BEANS.get(NodeIdentifier.class).get();

  public ClusterSynchronizationService() {
    m_subject = new Subject();
    m_subject.getPrincipals().add(new SimplePrincipal(CONFIG.getPropertyValue(ClusterSyncUserProperty.class)));
    m_subject.setReadOnly();
  }

  protected EventListenerList getListenerList() {
    return m_listenerList;
  }

  @Override
  public IClusterNodeStatusInfo getStatusInfo() {
    return m_statusInfo.getStatus();
  }

  protected ClusterNodeStatusInfo getStatusInfoInternal() {
    return m_statusInfo;
  }

  protected ClusterNodeStatusInfo getStatusInfoInternal(Class<? extends Serializable> messageType) {
    m_messageStatusMap.putIfAbsent(messageType, new ClusterNodeStatusInfo());
    return m_messageStatusMap.get(messageType);
  }

  public String getNodeId() {
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
      getTransaction().addMessage(new ClusterNotificationMessage(notification, getNotificationProperties()));
    }
  }

  @Override
  public void publish(Serializable notification) {
    publishAll(CollectionUtility.arrayList(notification));
  }

  private void publishAll(Collection<Serializable> notifications) {
    if (isEnabled()) {
      List<IClusterNotificationMessage> internalMessages = new ArrayList<IClusterNotificationMessage>();
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
      MOM.publish(ClusterMom.class, IClusterMomDestinations.CLUSTER_NOTIFICATION_TOPIC, message);
    }
    for (IClusterNotificationMessage im : messages) {
      getStatusInfoInternal().updateSentStatus(im);
      getStatusInfoInternal(im.getNotification().getClass()).updateReceiveStatus(im);
    }
  }

  @Override
  public IClusterNotificationProperties getNotificationProperties() {
    ISession curentSession = ISession.CURRENT.get();
    String userid = curentSession != null ? curentSession.getUserId() : "";
    return new ClusterNotificationProperties(m_nodeId, userid);
  }

  @Override
  public void onMessage(IMessage<IClusterNotificationMessage> message) {
    final IClusterNotificationMessage notificationMessage = message.getTransferObject();
    if (isEnabled()) {
      //Do not progress notifications sent by node itself
      String originNode = notificationMessage.getProperties().getOriginNode();

      if (m_nodeId.equals(originNode)) {
        return;
      }

      getStatusInfoInternal().updateReceiveStatus(notificationMessage);
      getStatusInfoInternal(notificationMessage.getNotification().getClass()).updateReceiveStatus(notificationMessage);

      ServerRunContext serverRunContext = ServerRunContexts.empty();
      serverRunContext.withSubject(m_subject);
      serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
      serverRunContext.run(new IRunnable() {

        @Override
        public void run() throws Exception {
          NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
          reg.notifyNotificationHandlers(notificationMessage.getNotification());
        }
      });
    }
  }

  /**
   * @return transaction member for publishing messages within a transaction
   */
  protected ClusterSynchTransactionMember getTransaction() {
    ITransaction tx = Assertions.assertNotNull(ITransaction.CURRENT.get(), "Transaction required");
    ClusterSynchTransactionMember m = (ClusterSynchTransactionMember) tx.getMember(TRANSACTION_MEMBER_ID);
    if (m == null) {
      m = new ClusterSynchTransactionMember(TRANSACTION_MEMBER_ID);
      tx.registerMember(m);
    }
    return m;
  }

  /**
   * Transaction member that notifies other cluster nodes after the causing Scout transaction has been committed. This
   * ensures that other cluster nodes are not informed too early.
   */
  private class ClusterSynchTransactionMember extends AbstractTransactionMember {
    private List<IClusterNotificationMessage> m_messageQueue;

    public ClusterSynchTransactionMember(String transactionId) {
      super(transactionId);
      m_messageQueue = new LinkedList<IClusterNotificationMessage>();
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
    return getStatusInfoInternal(messageType).getStatus();
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
