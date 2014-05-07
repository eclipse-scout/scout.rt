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
package org.eclipse.scout.rt.server.services.common.code;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.service.SERVICES;

/**
 *
 */
public class UnloadCodeTypeCacheNodeNotificationListener implements IClusterNotificationListener {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(CodeService.class);

  @Override
  public void onNotification(IClusterNotificationMessage notification) {
    if (accept(notification.getNotification())) {
      UnloadCodeTypeCacheClusterNotification n = (UnloadCodeTypeCacheClusterNotification) notification;
      try {
        SERVICES.getService(IClusterSyncCodeService.class).reloadCodeTypesNoFire(n.getTypes());
      }
      catch (ProcessingException e) {
        LOG.error("Unable to reload CodeTypes", e);
      }
    }
  }

  private boolean accept(IClusterNotification notification) {
    return (notification instanceof UnloadCodeTypeCacheClusterNotification);
  }

}
