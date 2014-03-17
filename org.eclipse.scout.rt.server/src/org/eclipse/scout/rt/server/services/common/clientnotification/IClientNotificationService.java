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
package org.eclipse.scout.rt.server.services.common.clientnotification;

import java.util.Set;

import org.eclipse.scout.rt.shared.services.common.clientnotification.IClientNotification;
import org.eclipse.scout.service.IService;

public interface IClientNotificationService extends IService {

  /**
   * this methods will be called asynchronous, there is no session available but
   * a proxyHandler is.
   */
  Set<IClientNotification> getNextNotifications(long blockingTimeout);

  /**
   * Put a notification to the queue on the server. <br>
   * The notification is enqueued in XA-transaction first and will be put in the
   * global-queue after the commit of the XA-transaction.
   * <p>
   * Please consider, that the accept method of the filter is launched in server session thread and the run method of
   * the notification is launched in client session thread.
   * 
   * @param notification
   * @param filter
   */
  void putNotification(IClientNotification notification, IClientNotificationFilter filter);

  void putDistributedNotification(IClientNotification notification, IClientNotificationFilter filter);

  void addClientNotificationQueueListener(IClientNotificationQueueListener listener);

  void removeClientNotificationQueueListener(IClientNotificationQueueListener listener);

  void putNotification(ClientNotificationNotification notification);

  void updateNotification(ClientNotificationNotification notification);

  void removeNotification(ClientNotificationNotification notification);
}
