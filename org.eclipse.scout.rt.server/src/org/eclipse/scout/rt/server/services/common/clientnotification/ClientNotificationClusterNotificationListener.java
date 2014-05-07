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

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class ClientNotificationClusterNotificationListener implements IClusterNotificationListener {

  @Override
  public void onNotification(IClusterNotificationMessage notification) {
    if (accept(notification.getNotification())) {
      ClientNotificationClusterNotification n = (ClientNotificationClusterNotification) notification;
      SERVICES.getService(IClientNotificationService.class).putNonClusterDistributedNotification(n.getQueueElement().getNotification(), n.getQueueElement().getFilter());
    }
  }

  protected boolean accept(IClusterNotification notification) {
    return (notification instanceof ClientNotificationClusterNotification)
        && ((ClientNotificationClusterNotification) notification).getQueueElement().isActive();
  }

}
