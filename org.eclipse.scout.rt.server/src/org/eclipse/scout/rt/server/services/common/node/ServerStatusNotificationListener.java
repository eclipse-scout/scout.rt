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
package org.eclipse.scout.rt.server.services.common.node;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotification;
import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotificationListener;
import org.eclipse.scout.rt.shared.services.common.node.INodeSynchronizationProcessService;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class ServerStatusNotificationListener implements IDistributedNotificationListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServerStatusNotificationListener.class);

  @Override
  public void onNewNotification(IDistributedNotification notification) {
    if (isInteresting(notification)) {
      ServerStatusNotification serverStatusNotification = (ServerStatusNotification) notification.getNotification();

      try {
        SERVICES.getService(INodeSynchronizationProcessService.class).updateForeignClusterNodeStatus(notification.getOriginNode(), serverStatusNotification.getStatusList());
      }
      catch (ProcessingException e) {
        LOG.error("Unable to update foreign cluster node status", e);
      }
    }
  }

  @Override
  public void onUpdateNotification(IDistributedNotification notification) {
  }

  @Override
  public void onRemoveNotification(IDistributedNotification notification) {
  }

  @Override
  public boolean isInteresting(IDistributedNotification notification) {
    return (notification.getNotification() instanceof ServerStatusNotification);
  }

}
