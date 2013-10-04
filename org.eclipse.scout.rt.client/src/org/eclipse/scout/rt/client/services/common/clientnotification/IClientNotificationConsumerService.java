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
package org.eclipse.scout.rt.client.services.common.clientnotification;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.servicetunnel.IServiceTunnel;
import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.service.IService;

/**
 * This service is used to listen for and handle client notifications. It is
 * also used by {@link IServiceTunnel} to dispatch notifications received by
 * every service response.
 */
@Priority(-3)
public interface IClientNotificationConsumerService extends IService {

  /**
   * The {@link IServiceTunnel} calles this method whenever client notifications
   * have been received. This method is normally not called by clients
   */
  void dispatchClientNotifications(IClientNotification[] notifications, IClientSession session);

  /**
   * A consumer of client notifications can add a listener to this service. The
   * listener is notified immediately on new notifications, in whatever Thread
   * 
   * @deprecated use {@link #addClientNotificationConsumerListener(IClientSession, IClientNotificationConsumerListener)}
   *             instead. Services that register in their {@link IService#initializeService()} phase normally use
   *             {@link #addGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener)}. Will be
   *             removed in Release 3.10.
   */
  @Deprecated
  void addClientNotificationConsumerListener(IClientNotificationConsumerListener listener);

  /**
   * @deprecated use
   *             {@link #removeClientNotificationConsumerListener(IClientSession, IClientNotificationConsumerListener)}
   *             instead. Will be removed in Release 3.10.
   */
  @Deprecated
  void removeClientNotificationConsumerListener(IClientNotificationConsumerListener listener);

  /**
   * A consumer of client notifications can add a listener on this service. The
   * listener is notified immediately on new notifications, in whatever thread
   */
  void addClientNotificationConsumerListener(IClientSession session, IClientNotificationConsumerListener listener);

  void removeClientNotificationConsumerListener(IClientSession session, IClientNotificationConsumerListener listener);

  /**
   * A consumer of client notifications can add a global listener on this service. The
   * listener is notified immediately on new notifications, in whatever thread.
   * A global listener is notified on any client session, this can be used to attach generic functionality for any
   * session.
   * Note that this does not imply that a client notification is broadcast to any client session. It simply means that
   * whenever a client session receives a notification,
   * this listener is informed.
   */
  void addGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener listener);

  void removeGlobalClientNotificationConsumerListener(IClientNotificationConsumerListener listener);

}
