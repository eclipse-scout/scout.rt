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
package org.eclipse.scout.rt.server.services.common.security.internal;

import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotification;
import org.eclipse.scout.rt.server.services.common.notification.IDistributedNotificationListener;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class ResetAccessControlChangedNodeNotificationListener implements IDistributedNotificationListener {

  @Override
  public void onNewNotification(IDistributedNotification notification) {
    if (isInteresting(notification)) {
      SERVICES.getService(INodeSynchronizationAccessControlService.class).clearCacheInternal(notification.getOriginUser(), notification.getOriginNode());
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
    return (notification instanceof ResetAccessControlChangedNodeNotification);
  }

}
