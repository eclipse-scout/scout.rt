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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.AbstractServerSession;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessageProperties;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class ClusterSynchronizationService extends AbstractService implements IClusterSynchronizationService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClusterSynchronizationService.class);

  private final List<IClusterNotificationListener> m_distributedNotificationListener = new ArrayList<IClusterNotificationListener>();
  private final static String QUEUE_NAME = "scoutNotificationQueue";
  private IPubSubMessageService m_pubSubMessageService;
  private String m_nodeId;
  private boolean m_enabled;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    m_pubSubMessageService = SERVICES.getService(IPubSubMessageService.class);
    m_nodeId = UUID.randomUUID().toString();
    enable();
  }

  @Override
  public void disposeServices() {
    super.disposeServices();
    disable();
  }

  @Override
  public boolean enable() {
    if (m_pubSubMessageService != null) {
      m_enabled = m_pubSubMessageService.subscribe(QUEUE_NAME);
      if (m_enabled) {
//        Activator.getDefault().getNodeSynchronizationInfo().setClusterSyncService(this);
      }
    }
    else {
      m_enabled = false;
      LOG.error("Clustersync could not be enabled. No service of type IPubSubMessageService found.");
    }
    return m_enabled;
  }

  @Override
  public boolean disable() {
    boolean unregisterSuccessful = m_pubSubMessageService.unsubsribe(QUEUE_NAME);

    if (unregisterSuccessful) {
//update info
//      if (Activator.getDefault().getNodeSynchronizationInfo().getClusterSyncService() == m_pubSubMessageService) {
//        Activator.getDefault().getNodeSynchronizationInfo().setClusterSyncService(null);
//      }
      m_enabled = false;
    }
    return unregisterSuccessful;
  }

  @Override
  public boolean isEnabled() {
    return m_enabled;
  }

  @Override
  public void publishNotification(IClusterNotification notification) {
    if (m_enabled) {
      ClusterNotificationMessage message = new ClusterNotificationMessage(notification, getNotificationProperties());
      m_pubSubMessageService.publishNotification(message);
//      if (publishSuccessful) {
      //TODO
//        Activator.getDefault().getNodeSynchronizationInfo().incrementSentMessageCount();
//      }
    }
  }

  protected IClusterNotificationMessageProperties getNotificationProperties() {
    return new ClusterNotificationMessageProperties(getNodeId(), ServerJob.getCurrentSession().getUserId());
  }

  @Override
  public void processNotification(IClusterNotificationMessage notification) {
    //Do not progress notifications sent by node itself
    String originNode = notification.getProperties().getOriginNode();
    if (m_nodeId.equals(originNode)) {
// TODO update info
//      Activator.getDefault().getNodeSynchronizationInfo().incrementReceivedMessageCount();
//      Activator.getDefault().getNodeSynchronizationInfo().setLastChangedDate(new Date());
//      Activator.getDefault().getNodeSynchronizationInfo().setLastChangedUserId(notification.getOriginUser());
//      Activator.getDefault().getNodeSynchronizationInfo().setLastChangedClusterNodeId(notification.getOriginNode());

      IServerSession session = getBackendSession();
      P_NotificationProcessinJob notificationProcessJob = new P_NotificationProcessinJob("NotificationProcessingJob", session, notification, m_distributedNotificationListener);
      notificationProcessJob.runNow(new NullProgressMonitor());
    }

  }

  private AbstractServerSession getBackendSession() {
    //TODO
    return new AbstractServerSession(true) {
      private static final long serialVersionUID = 1L;
    };
  }

  private class P_NotificationProcessinJob extends ServerJob {

    IClusterNotificationMessage m_distributedNotification;
    List<IClusterNotificationListener> m_distributedNotificationListener;

    public P_NotificationProcessinJob(String name, IServerSession serverSession, IClusterNotificationMessage notification, List<IClusterNotificationListener> listener) {
      super(name, serverSession);
      m_distributedNotification = notification;
      m_distributedNotificationListener = listener;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      for (IClusterNotificationListener listener : m_distributedNotificationListener) {
        listener.onNotification(m_distributedNotification);
      }
      return Status.OK_STATUS;
    }
  }

  @Override
  public void addListener(IClusterNotificationListener listener) {
    m_distributedNotificationListener.add(listener);
  }

  @Override
  public void removeListener(IClusterNotificationListener listener) {
    m_distributedNotificationListener.remove(listener);
  }

  @Override
  public String getNodeId() {
    return m_nodeId;
  }
}
