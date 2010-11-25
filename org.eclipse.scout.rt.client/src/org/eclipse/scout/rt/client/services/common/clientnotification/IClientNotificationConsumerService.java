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
import org.eclipse.scout.rt.client.servicetunnel.IServiceTunnel;
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
   */
  void addClientNotificationConsumerListener(IClientNotificationConsumerListener listener);

  void removeClientNotificationConsumerListener(IClientNotificationConsumerListener listener);

}
