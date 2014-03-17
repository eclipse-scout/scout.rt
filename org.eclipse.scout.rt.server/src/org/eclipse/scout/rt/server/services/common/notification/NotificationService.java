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
package org.eclipse.scout.rt.server.services.common.notification;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.clientnotification.DistributedClientNotificationListener;
import org.eclipse.scout.rt.server.services.common.node.IBackendService;
import org.eclipse.scout.rt.server.services.common.node.NodeSynchronizationProcessService;
import org.eclipse.scout.rt.server.services.common.node.RequestServerCacheStatusNotificationListener;
import org.eclipse.scout.rt.server.services.common.node.RequestServerStatusNotificationListener;
import org.eclipse.scout.rt.server.services.common.node.ServerStatusNotificationListener;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class NotificationService extends AbstractService implements INotificationService {

  private List<IDistributedNotificationListener> m_distributedNotificationListener = new ArrayList<IDistributedNotificationListener>();
  private final static String QUEUE_NAME = "scoutNotificationQueue";
  private IPubSubMessageService m_pubSubMessageService;
  private String m_nodeId;
  private boolean m_enabled;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(NotificationService.class);

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    m_pubSubMessageService = SERVICES.getService(IPubSubMessageService.class);
    m_nodeId = SERVICES.getService(NodeSynchronizationProcessService.class).getClusterNodeId();

    m_distributedNotificationListener.add(new DistributedClientNotificationListener());
    m_distributedNotificationListener.add(new ServerStatusNotificationListener());
    m_distributedNotificationListener.add(new RequestServerStatusNotificationListener());
    m_distributedNotificationListener.add(new RequestServerCacheStatusNotificationListener());
  }

  @Override
  public boolean register() {

    m_enabled = m_pubSubMessageService.subscribe(QUEUE_NAME);
    if (m_enabled) {
      Activator.getDefault().getNodeSynchronizationInfo().setClusterSyncService(this);
    }

    return m_enabled;
  }

  @Override
  public boolean unregister() {
    boolean result = m_pubSubMessageService.unsubsribe(QUEUE_NAME);

    if (Activator.getDefault().getNodeSynchronizationInfo().getClusterSyncService() == m_pubSubMessageService) {
      Activator.getDefault().getNodeSynchronizationInfo().setClusterSyncService(null);
    }

    return result;
  }

  @Override
  public void publishNotification(INotification notification) {
    IDistributedNotification distributedNotification = CreateNotificationFactory.createNewNotification(notification);
    if (m_pubSubMessageService.publishNotification(distributedNotification)) {
      Activator.getDefault().getNodeSynchronizationInfo().incrementSentMessageCount();
    }
  }

  @Override
  public void updateNotification(INotification notification) {
    IDistributedNotification distributedNotification = CreateNotificationFactory.createUpdateNotification(notification);
    m_pubSubMessageService.publishNotification(distributedNotification);
  }

  @Override
  public void removeNotification(INotification notification) {
    IDistributedNotification distributedNotification = CreateNotificationFactory.createRemoveNotification(notification);
    m_pubSubMessageService.publishNotification(distributedNotification);
  }

  @Override
  public void processNotification(IDistributedNotification notification) {

    //Don't progress notifications send by itself
    if (notification.getOriginNode() != null && !notification.getOriginNode().equals(m_nodeId)) {
      Activator.getDefault().getNodeSynchronizationInfo().incrementReceivedMessageCount();
      Activator.getDefault().getNodeSynchronizationInfo().setLastChangedDate(new Date());
      Activator.getDefault().getNodeSynchronizationInfo().setLastChangedUserId(notification.getOriginUser());
      Activator.getDefault().getNodeSynchronizationInfo().setLastChangedClusterNodeId(notification.getOriginNode());

      IServerSession session = SERVICES.getService(IBackendService.class).getBackendServerSession();
      P_NotificationProcessinJob notificationProcessJob = new P_NotificationProcessinJob("NotificationProcessingJob", session, notification, m_distributedNotificationListener);
      notificationProcessJob.runNow(new NullProgressMonitor());
    }

  }

  private class P_NotificationProcessinJob extends ServerJob {

    IDistributedNotification m_distributedNotification;
    List<IDistributedNotificationListener> m_distributedNotificationListener;

    public P_NotificationProcessinJob(String name, IServerSession serverSession, IDistributedNotification notification, List<IDistributedNotificationListener> listener) {
      super(name, serverSession);
      m_distributedNotification = notification;
      m_distributedNotificationListener = listener;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      for (IDistributedNotificationListener listener : m_distributedNotificationListener) {
        if (m_distributedNotification.isNew()) {
          listener.onNewNotification(m_distributedNotification);
        }
        if (m_distributedNotification.isUpdate()) {
          listener.onUpdateNotification(m_distributedNotification);
        }
        if (m_distributedNotification.isRemove()) {
          listener.onRemoveNotification(m_distributedNotification);
        }
      }
      return Status.OK_STATUS;
    }
  }

  private class NotificationProcessor implements Runnable {

    IDistributedNotification m_distributedNotification;
    List<IDistributedNotificationListener> m_distributedNotificationListener;

    public NotificationProcessor(IDistributedNotification notification, List<IDistributedNotificationListener> listener) {
      m_distributedNotification = notification;
      m_distributedNotificationListener = listener;
    }

    @Override
    public void run() {

      for (IDistributedNotificationListener listener : m_distributedNotificationListener) {
        if (m_distributedNotification.isNew()) {
          listener.onNewNotification(m_distributedNotification);
        }
        if (m_distributedNotification.isUpdate()) {
          listener.onUpdateNotification(m_distributedNotification);
        }
        if (m_distributedNotification.isRemove()) {
          listener.onRemoveNotification(m_distributedNotification);
        }
      }
    }
  }

  @Override
  public void addDistributedNotificationListener(IDistributedNotificationListener listener) {
    m_distributedNotificationListener.add(listener);
  }

  @Override
  public void removeDistributedNotificationListener(IDistributedNotificationListener listener) {
    m_distributedNotificationListener.remove(listener);
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }
}
