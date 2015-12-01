/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.security.auth.Subject;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.ConfigUtility;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.EventListenerList;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.ServerConfigProperties.ClusterSyncNodeIdProperty;
import org.eclipse.scout.rt.server.ServerConfigProperties.ClusterSyncUserProperty;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationProperties;
import org.eclipse.scout.rt.server.session.ServerSessionProviderWithCache;
import org.eclipse.scout.rt.server.transaction.AbstractTransactionMember;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.notification.NotificationHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClusterSynchronizationService implements IClusterSynchronizationService, IPublishSubscribeMessageListener {
  private static final Logger LOG = LoggerFactory.getLogger(ClusterSynchronizationService.class);

  private static final String TRANSACTION_MEMBER_ID = ClusterSynchronizationService.class.getName();

  private final EventListenerList m_listenerList = new EventListenerList();
  private final ClusterNodeStatusInfo m_statusInfo = new ClusterNodeStatusInfo();
  private final ConcurrentMap<Class<? extends Serializable>, ClusterNodeStatusInfo> m_messageStatusMap = new ConcurrentHashMap<>();

  private volatile String m_nodeId;
  private final Subject m_subject;

  private volatile boolean m_enabled;
  private volatile IPublishSubscribeMessageService m_messageService;

  public ClusterSynchronizationService() {
    m_subject = new Subject();
    m_subject.getPrincipals().add(new SimplePrincipal(CONFIG.getPropertyValue(ClusterSyncUserProperty.class)));
    m_subject.setReadOnly();
  }

  @PostConstruct
  public void initializeService() {
    m_nodeId = createNodeId();
  }

  protected String createNodeId() {
    // system property defined node id
    String nodeId = CONFIG.getPropertyValue(ClusterSyncNodeIdProperty.class);

    // weblogic name as node id
    if (!StringUtility.hasText(nodeId)) {
      nodeId = System.getProperty("weblogic.Name");
    }

    // jboss node name as node id
    if (!StringUtility.hasText(nodeId)) {
      nodeId = System.getProperty("jboss.node.name");
    }

    // use host name
    if (!StringUtility.hasText(nodeId)) {
      String hostname;
      try {
        hostname = InetAddress.getLocalHost().getHostName();
      }
      catch (UnknownHostException e) {
        hostname = null;
      }
      // might result in a hostname 'localhost'
      if (StringUtility.isNullOrEmpty(hostname) || "localhost".equalsIgnoreCase(hostname)) {
        // use random number
        nodeId = UUID.randomUUID().toString();
      }
      else {
        // in development on same machine there might run multiple instances on different ports (usecase when testing cluster sync)
        // therefore we use in this case the jetty port too
        String port = ConfigUtility.getProperty("scout.jetty.port"); // see org.eclipse.scout.dev.jetty.JettyServer.SERVER_PORT_KEY
        if (StringUtility.hasText(port)) {
          nodeId = StringUtility.join(":", hostname, port);
        }
        else {
          nodeId = StringUtility.join(":", hostname, 8080);
        }
      }
    }
    return nodeId;
  }

  protected EventListenerList getListenerList() {
    return m_listenerList;
  }

  /**
   * @deprecated use {@link INotificationHandler}
   */
  @Deprecated
  @Override
  public void addListener(IClusterNotificationListener listener) {
    m_listenerList.add(IClusterNotificationListener.class, listener);
  }

  /**
   * @deprecated use {@link INotificationHandler}
   */
  @Deprecated
  @Override
  public void removeListener(IClusterNotificationListener listener) {
    m_listenerList.remove(IClusterNotificationListener.class, listener);
  }

  /**
   * @deprecated use {@link INotificationHandler}
   */
  @Deprecated
  protected IClusterNotificationListener[] getListeners() {
    return getListenerList().getListeners(IClusterNotificationListener.class);
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

  @Override
  public String getNodeId() {
    return m_nodeId;
  }

  protected IPublishSubscribeMessageService getMessageService() {
    return m_messageService;
  }

  protected void setMessageService(IPublishSubscribeMessageService messageService) {
    m_messageService = messageService;
  }

  protected void setEnabled(boolean enabled) {
    m_enabled = enabled;
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public boolean enable() {
    if (isEnabled()) {
      return true;
    }
    if (getNodeId() == null) {
      LOG.error("Clustersync could not be enabled. No cluster nodeId could be determined.");
      return false;
    }
    IPublishSubscribeMessageService messageService = BEANS.get(IPublishSubscribeMessageService.class);
    if (messageService == null) {
      LOG.error("Clustersync could not be enabled. No MessageService found.");
      return false;
    }
    try {
      messageService.setListener(this);
      messageService.subscribe();
      setMessageService(messageService);
    }
    catch (Exception e) {
      LOG.error(e.getMessage(), e);
      return false;
    }
    setEnabled(true);
    return true;
  }

  @Override
  public boolean disable() {
    if (!isEnabled()) {
      return true;
    }
    setEnabled(false);
    IPublishSubscribeMessageService messageService = getMessageService();
    if (messageService != null) {
      try {
        messageService.unsubsribe();
      }
      catch (Exception e) {
        LOG.error(e.getMessage(), e);
        return false;
      }
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
    m_messageService.publishNotifications(messages);
    for (IClusterNotificationMessage im : messages) {
      getStatusInfoInternal().updateSentStatus(im);
      getStatusInfoInternal(im.getNotification().getClass()).updateReceiveStatus(im);
    }
  }

  @Override
  public IClusterNotificationProperties getNotificationProperties() {
    ISession curentSession = ISession.CURRENT.get();
    String userid = curentSession != null ? curentSession.getUserId() : "";
    return new ClusterNotificationProperties(getNodeId(), userid);
  }

  @Override
  public void onMessage(final IClusterNotificationMessage message) {
    if (isEnabled()) {
      //Do not progress notifications sent by node itself
      String originNode = message.getProperties().getOriginNode();
      if (getNodeId().equals(originNode)) {
        return;
      }

      getStatusInfoInternal().updateReceiveStatus(message);
      getStatusInfoInternal(message.getNotification().getClass()).updateReceiveStatus(message);

      ServerRunContext serverRunContext = ServerRunContexts.empty();
      serverRunContext.withSubject(m_subject);
      serverRunContext.withSession(BEANS.get(ServerSessionProviderWithCache.class).provide(serverRunContext.copy()));
      serverRunContext.run(new IRunnable() {

        @Override
        public void run() throws Exception {
          NotificationHandlerRegistry reg = BEANS.get(NotificationHandlerRegistry.class);
          reg.notifyHandlers(message.getNotification());
        }
      });
    }
  }

  @PreDestroy
  public void disposeServices() {
    disable();
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
    public boolean commitPhase1() {
      return true;
    }

    @Override
    public synchronized void commitPhase2() {
      publishInternal(m_messageQueue);
    }

    @Override
    public synchronized boolean needsCommit() {
      return !m_messageQueue.isEmpty();
    }

    @Override
    public void release() {
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

}
