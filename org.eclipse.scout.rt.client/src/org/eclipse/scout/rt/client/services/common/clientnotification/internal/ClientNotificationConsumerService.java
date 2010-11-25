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
public class ClientNotificationConsumerService extends AbstractService implements IClientNotificationConsumerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ClientNotificationConsumerService.class);

  private final EventListenerList m_listenerList = new EventListenerList();

  public ClientNotificationConsumerService() {
  }

  public void dispatchClientNotifications(final IClientNotification[] notifications, IClientSession session) {
    if (notifications == null || notifications.length == 0) {
      return;
    }
    if (ClientJob.isSyncClientJob()) {
      // we are sync
      for (IClientNotification n : notifications) {
        fireEvent(n, true);
      }
    }
    else {
      // async
      new ClientAsyncJob("Dispatch client notifications", session) {
        @Override
        protected void runVoid(IProgressMonitor monitor) throws Throwable {
          for (IClientNotification n : notifications) {
            fireEvent(n, false);
          }
        }
      }.schedule();
    }
  }

  public void addClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    m_listenerList.add(IClientNotificationConsumerListener.class, listener);
  }

  public void removeClientNotificationConsumerListener(IClientNotificationConsumerListener listener) {
    m_listenerList.remove(IClientNotificationConsumerListener.class, listener);
  }

  private void fireEvent(IClientNotification notification, boolean sync) {
    IClientNotificationConsumerListener[] listeners = m_listenerList.getListeners(IClientNotificationConsumerListener.class);
    if (listeners != null) {
      ClientNotificationConsumerEvent e = new ClientNotificationConsumerEvent(this, notification);
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

}
