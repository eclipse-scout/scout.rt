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
package org.eclipse.scout.rt.client.services.common.clientnotification.internal;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.scout.commons.EventListenerList;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientAsyncJob;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.service.AbstractService;

@Priority(-3)
@SuppressWarnings("deprecation")
public class ClientNotificationConsumerService extends AbstractService implements IClientNotificationConsumerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationConsumerService.class);
  private static final String SESSION_DATA_KEY = "clientNotificationConsumerServiceState";

  private final EventListenerList m_globalListenerList = new EventListenerList();

  public ClientNotificationConsumerService() {
  }

  private ServiceState getServiceState(IClientSession session) {
    if (session == null) {
      throw new IllegalStateException("session is null");
    }
    ServiceState data = (ServiceState) session.getData(SESSION_DATA_KEY);
    if (data == null) {
      data = new ServiceState();
      session.setData(SESSION_DATA_KEY, data);
    }
    return data;
  }

  @Override
  public void dispatchClientNotifications(final IClientNotification[] notifications, final IClientSession session) {
    if (notifications == null || notifications.length == 0) {
      return;
    }
    if (ClientJob.getCurrentSession() == session) {
      // we are sync
      for (IClientNotification n : notifications) {
        fireEvent(session, n, true);
      }
    }
    else {
      // async
      new ClientAsyncJob("Dispatch client notifications", session) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          for (IClientNotification n : notifications) {
            fireEvent(session, n, false);
          }
        }
      }.schedule();
    }
  }

  @Override
  @Deprecated
  public void addClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    addClientNotificationConsumerListener(ClientJob.getCurrentSession(), listener);
  }

  @Override
  @Deprecated
  public void removeClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    removeClientNotificationConsumerListener(ClientJob.getCurrentSession(), listener);
  }

  @Override
  public void addClientNotificationConsumerListener(IClientSession session, IClientNotificationConsumerListener listener) {
    getServiceState(session).m_listenerList.add(IClientNotificationConsumerListener.class, listener);
  }

  @Override
  public void removeClientNotificationConsumerListener(IClientSession session, IClientNotificationConsumerListener listener) {
    getServiceState(session).m_listenerList.remove(IClientNotificationConsumerListener.class, listener);
  }

  @Override
  public void addGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    m_globalListenerList.add(IClientNotificationConsumerListener.class, listener);
  }

  @Override
  public void removeGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    m_globalListenerList.remove(IClientNotificationConsumerListener.class, listener);
  }

  private void fireEvent(IClientSession session, IClientNotification notification, boolean sync) {
    ClientNotificationConsumerEvent e = new ClientNotificationConsumerEvent(this, notification);
    IClientNotificationConsumerListener[] globalListeners = m_globalListenerList.getListeners(IClientNotificationConsumerListener.class);
    IClientNotificationConsumerListener[] listeners = getServiceState(session).m_listenerList.getListeners(IClientNotificationConsumerListener.class);
    if (globalListeners != null) {
      for (IClientNotificationConsumerListener listener : globalListeners) {
        try {
          listener.handleEvent(e, sync);
        }
        catch (Throwable t) {
          LOG.error("Listener " + listener.getClass().getName() + " on event " + notification, t);
        }
      }
    }
    if (listeners != null) {
      for (IClientNotificationConsumerListener listener : listeners) {
        try {
          listener.handleEvent(e, sync);
        }
        catch (Throwable t) {
          LOG.error("Listener " + listener.getClass().getName() + " on event " + notification, t);
        }
      }
    }
  }

  private static class ServiceState {
    EventListenerList m_listenerList = new EventListenerList();
  }

}
