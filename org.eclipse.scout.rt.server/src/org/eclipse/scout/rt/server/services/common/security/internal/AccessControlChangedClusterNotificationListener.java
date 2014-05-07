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

import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.security.IClusterSyncAccessControlService;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class AccessControlChangedClusterNotificationListener implements IClusterNotificationListener {

  @Override
  public void onNotification(IClusterNotificationMessage notification) {
    if (isInteresting(notification.getNotification())) {
      SERVICES.getService(IClusterSyncAccessControlService.class).clearCacheNoFire();
    }
  }

  public boolean isInteresting(IClusterNotification notification) {
    return (notification instanceof AccessControlCacheChangedClusterNotification);
  }

}
