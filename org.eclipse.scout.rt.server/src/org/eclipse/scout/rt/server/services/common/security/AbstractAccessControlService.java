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
package org.eclipse.scout.rt.server.services.common.security;

import java.security.Permissions;
import java.util.Collection;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.clientnotification.IClientNotificationService;
import org.eclipse.scout.rt.server.services.common.clientnotification.SingleUserFilter;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotification;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListener;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationListenerService;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterNotificationMessage;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.shared.services.common.security.AbstractSharedAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.service.IService;
import org.eclipse.scout.service.SERVICES;

/**
 * Implementations should override {@link #execLoadPermissions()}
 */
@Priority(-1)
public abstract class AbstractAccessControlService extends AbstractSharedAccessControlService implements IClusterNotificationListenerService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractAccessControlService.class);

  @Override
  protected void notifySetPermisions(Permissions p) {
    // notify clients:
    String userId = SERVICES.getService(IAccessControlService.class).getUserIdOfCurrentSubject();
    SERVICES.getService(IClientNotificationService.class).putNotification(new AccessControlChangedNotification(p), new SingleUserFilter(userId, 120000L));
  }

  @Override
  public void clearCache() {
    clearCacheNoFire();

    //notify clients with a filter, that will be accepted nowhere:
    SERVICES.getService(IClientNotificationService.class).putNotification(new ResetAccessControlChangedNotification(), new SingleUserFilter(null, 0L));

    //notify clusters:
    try {
      IClusterSynchronizationService s = SERVICES.getService(IClusterSynchronizationService.class);
      if (s != null) {
        s.publishNotification(new AccessControlCacheChangedClusterNotification());
      }
    }
    catch (ProcessingException e) {
      LOG.error("failed notifying cluster for permission changes", e);
    }
  }

  @Override
  public void clearCacheOfUserIds(Collection<String> userIds) {
    clearCacheOfUserIdsNoFire(userIds);

    //notify clusters:
    try {
      IClusterSynchronizationService s = SERVICES.getService(IClusterSynchronizationService.class);
      if (s != null) {
        s.publishNotification(new AccessControlCacheChangedClusterNotification(userIds));
      }
    }
    catch (ProcessingException e) {
      LOG.error("failed notifying cluster for permission changes", e);
    }

    //notify clients:
    for (String userId : userIds) {
      if (userId != null) {
        SERVICES.getService(IClientNotificationService.class).putNotification(new AccessControlChangedNotification(null), new SingleUserFilter(userId, 120000L));
      }
    }
  }

  @Override
  public Class<? extends IService> getDefiningServiceInterface() {
    return IAccessControlService.class;
  }

  @Override
  public IClusterNotificationListener getClusterNotificationListener() {
    return new IClusterNotificationListener() {

      @Override
      public void onNotification(IClusterNotificationMessage message) throws ProcessingException {
        IClusterNotification clusterNotification = message.getNotification();
        if ((clusterNotification instanceof AccessControlCacheChangedClusterNotification)) {
          AccessControlCacheChangedClusterNotification n = (AccessControlCacheChangedClusterNotification) clusterNotification;
          if (n.getUserIds().isEmpty()) {
            clearCacheNoFire();
          }
          else {
            clearCacheOfUserIdsNoFire(n.getUserIds());
          }
        }
      }
    };
  }
}
