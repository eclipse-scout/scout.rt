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
package org.eclipse.scout.rt.client.services.common.useractivity;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.shared.services.common.useractivity.IUserActivityProvider;
import org.eclipse.scout.rt.shared.services.common.useractivity.IUserActivityStateService;
import org.eclipse.scout.rt.shared.services.common.useractivity.UserActivityClientNotification;
import org.eclipse.scout.rt.shared.services.common.useractivity.UserStatusMap;
import org.eclipse.scout.service.SERVICES;

public class UserActivityManager {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UserActivityManager.class);

  private final IClientSession m_clientSession;
  private final PropertyChangeListener m_userActivityProviderListener;
  private final IClientNotificationConsumerListener m_clientNotificationConsumerListener;
  //
  private IUserActivityProvider m_provider;
  private final EventListenerList m_listenerList;
  private boolean m_started;
  private int m_currentState = IUserActivityStateService.STATUS_OFFLINE;
  private UserStatusMap m_statusMap;

  public UserActivityManager(IClientSession clientSession) {
    m_clientSession = clientSession;
    m_clientNotificationConsumerListener = new IClientNotificationConsumerListener() {
      @Override
      public void handleEvent(ClientNotificationConsumerEvent e, boolean sync) {
        if (e.getClientNotification() instanceof UserActivityClientNotification) {
          updateCache(((UserActivityClientNotification) e.getClientNotification()).getUserStatusMap());
        }
      }
    };
    m_userActivityProviderListener = new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent e) {
        providerStateChanged((Boolean) e.getNewValue() ? IUserActivityStateService.STATUS_ONLINE : IUserActivityStateService.STATUS_IDLE);
      }
    };
    m_listenerList = new EventListenerList();
  }

  public void addUserActivityListener(UserActivityListener listener) {
    m_listenerList.add(UserActivityListener.class, listener);
  }

  public void removeUserActivityListener(UserActivityListener listener) {
    m_listenerList.remove(UserActivityListener.class, listener);
  }

  public void start() {
    if (m_started) {
      return;
    }
    m_started = true;
    // initial state
    for (IUserActivityStateService s : SERVICES.getServices(IUserActivityStateService.class)) {
      try {
        updateCache(s.getUserStatusMap());
      }
      catch (Throwable t) {
        LOG.error("service " + s.getClass().getName(), t);
      }
    }
    // add client notification listener
    SERVICES.getService(IClientNotificationConsumerService.class).addClientNotificationConsumerListener(m_clientSession, m_clientNotificationConsumerListener);
    // add provider listener
    try {
      m_provider = SERVICES.getService(IUserActivityProvider.class);
      if (m_provider != null) {
        m_provider.addPropertyChangeListener(IUserActivityProvider.PROP_ACTIVE, m_userActivityProviderListener);
      }
    }
    catch (Throwable t) {
      LOG.error("provider " + m_provider, t);
    }
    // switch to online
    providerStateChanged(IUserActivityStateService.STATUS_ONLINE);
  }

  public void stop() {
    if (!m_started) {
      return;
    }
    m_started = false;
    // detach
    SERVICES.getService(IClientNotificationConsumerService.class).removeClientNotificationConsumerListener(m_clientSession, m_clientNotificationConsumerListener);
    if (m_provider != null) {
      m_provider.removePropertyChangeListener(IUserActivityProvider.PROP_ACTIVE, m_userActivityProviderListener);
    }
    m_provider = null;
    providerStateChanged(IUserActivityStateService.STATUS_OFFLINE);
  }

  public UserStatusMap getUserStatusMap() {
    return m_statusMap;
  }

  private void updateCache(UserStatusMap statusMap) {
    m_statusMap = statusMap;
    fireUserActivityEvent(new UserActivityEvent(this, m_statusMap));
  }

  private void fireUserActivityEvent(UserActivityEvent e) {
    for (UserActivityListener listener : m_listenerList.getListeners(UserActivityListener.class)) {
      try {
        listener.stateChanged(e);
      }
      catch (Throwable t) {
        LOG.error("listener " + listener.getClass().getName(), t);
      }
    }
  }

  private void providerStateChanged(final int newStatus) {
    if (newStatus != m_currentState) {
      m_currentState = newStatus;
      new ClientAsyncJob("user activity " + newStatus, m_clientSession) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          for (IUserActivityStateService s : SERVICES.getServices(IUserActivityStateService.class)) {
            try {
              s.setStatus(newStatus);
            }
            catch (Throwable t) {
              LOG.error("service " + s.getClass().getName(), t);
            }
          }
        }
      }.schedule();
    }
  }

}
