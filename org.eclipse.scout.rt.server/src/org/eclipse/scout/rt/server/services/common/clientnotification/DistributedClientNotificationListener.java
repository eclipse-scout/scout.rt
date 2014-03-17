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

import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotification;
import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotificationListener;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class DistributedClientNotificationListener implements IDistributedNotificationListener {

  @Override
  public void onNewNotification(IDistributedNotification notification) {
    if (isInteresting(notification)) {
      ClientNotificationNotification clientNotificationNotification = (ClientNotificationNotification) notification.getNotification();
      SERVICES.getService(IClientNotificationService.class).putNotification(clientNotificationNotification);
    }
  }

  @Override
  public void onUpdateNotification(IDistributedNotification notification) {
    if (isInteresting(notification)) {
      SERVICES.getService(IClientNotificationService.class).updateNotification((ClientNotificationNotification) notification.getNotification());
    }
  }

  @Override
  public void onRemoveNotification(IDistributedNotification notification) {
    if (isInteresting(notification)) {
      SERVICES.getService(IClientNotificationService.class).removeNotification((ClientNotificationNotification) notification.getNotification());
    }
  }

  @Override
  public boolean isInteresting(IDistributedNotification notification) {
    return (notification.getNotification() instanceof ClientNotificationNotification);
  }
}
