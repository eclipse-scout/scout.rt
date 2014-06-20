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

import javax.security.auth.Subject;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.ServerJob;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ClusterNotificationMessageProperties;
import org.eclipse.scout.rt.server.services.common.clustersync.internal.ServerSessionClassFinder;
import org.eclipse.scout.rt.server.services.common.session.IServerSessionRegistryService;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.ServiceRegistration;

/**
 *
 */
public class ClusterSynchronizationService extends AbstractService implements IClusterSynchronizationService, IPubSubMessageListener {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClusterSynchronizationService.class);

  private final List<IClusterNotificationListener> m_listeners = new ArrayList<IClusterNotificationListener>();
  private final static String QUEUE_NAME = "scoutNotificationQueue";

  private IPubSubMessageService m_pubSubMessageService;
  private String m_nodeId;
  private boolean m_enabled;
  private IServerSession m_session;
  private Subject m_subject;

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    m_pubSubMessageService = SERVICES.getService(IPubSubMessageService.class);
    m_nodeId = UUID.randomUUID().toString();
    m_subject = createBackendSubject();
    m_session = createBackendSession();
    enable();
  }

  private Subject createBackendSubject() {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal("server"));
    return subject;
  }

  protected IServerSession createBackendSession() {
    Class<? extends IServerSession> sessionClazz = ServerSessionClassFinder.find();
    IServerSession serverSession = null;
    try {
      serverSession = SERVICES.getService(IServerSessionRegistryService.class).newServerSession(sessionClazz, m_subject);
      serverSession.setIdInternal(UUID.randomUUID().toString());
    }
    catch (ProcessingException e) {
      LOG.error("Error creating backend session for cluster synchronization.", e);
    }
    return serverSession;
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
        m_pubSubMessageService.setListener(this);
      }
    }
    else {
      m_enabled = false;
      LOG.error("Clustersync could not be enabled. No service of type IPubSubMessageService found.");
    }
    return m_enabled;
  }

  /**
   * @return the synchronization status of the current node
   */
  public IClusterNodeStatusInfo getNodeStatus() {
    return Activator.getDefault().getClusterSynchronizationInfo();
  }

  @Override
  public boolean disable() {
    boolean unregisterSuccessful = m_pubSubMessageService.unsubsribe(QUEUE_NAME);
    if (unregisterSuccessful) {
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
      boolean successful = m_pubSubMessageService.publishNotification(message);
      if (successful) {
        Activator.getDefault().getClusterSynchronizationInfo().incrementSentMessageCount();
      }
    }
  }

  protected IClusterNotificationMessageProperties getNotificationProperties() {
    return new ClusterNotificationMessageProperties(getNodeId(), ServerJob.getCurrentSession().getUserId());
  }

  /**
   * @return {@link IServerSession} used to handle incoming notification messages
   */
  public IServerSession getBackendSession() {
    return m_session;
  }

  /**
   * @return {@link Subject} used to handle incoming notification messages
   */
  public Subject getBackendSubject() {
    return m_subject;
  }

  protected void notifyListeners(IClusterNotificationMessage message) {
    for (IClusterNotificationListener listener : getListeners()) {
      listener.onNotification(message);
    }
  }

  private class P_NotificationProcessingJob extends ServerJob {

    private final IClusterNotificationMessage m_distributedNotification;
    private final List<IClusterNotificationListener> m_listeners;

    public P_NotificationProcessingJob(String name, IClusterNotificationMessage notification, List<IClusterNotificationListener> listener) {
      super(name, getBackendSession(), getBackendSubject());
      m_distributedNotification = notification;
      m_listeners = listener;
    }

    @Override
    protected IStatus runTransaction(IProgressMonitor monitor) throws Exception {
      notifyListeners(m_distributedNotification);
      return Status.OK_STATUS;
    }
  }

  @Override
  public void addListener(IClusterNotificationListener listener) {
    m_listeners.add(listener);
  }

  @Override
  public void removeListener(IClusterNotificationListener listener) {
    m_listeners.remove(listener);
  }

  protected List<IClusterNotificationListener> getListeners() {
    return m_listeners;
  }

  @Override
  public String getNodeId() {
    return m_nodeId;
  }

  @Override
  public void onMessage(IClusterNotificationMessage message) {
    //Do not progress notifications sent by node itself
    String originNode = message.getProperties().getOriginNode();
    if (!m_nodeId.equals(originNode)) {
      Activator.getDefault().getClusterSynchronizationInfo().updateReceiveStatus(message);
      P_NotificationProcessingJob j = new P_NotificationProcessingJob("NotificationProcessingJob", message, m_listeners);
      j.runNow(new NullProgressMonitor());
    }
  }
}
