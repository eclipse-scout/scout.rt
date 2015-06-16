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

import java.io.Serializable;
import java.security.Permissions;
import java.util.Collection;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.clientnotification.ClientNotificationRegistry;
import org.eclipse.scout.rt.server.services.common.clustersync.IClusterSynchronizationService;
import org.eclipse.scout.rt.server.transaction.ITransaction;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.services.common.security.AbstractSharedAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;

/**
 * Implementations should override {@link #execLoadPermissions()}
 */
public abstract class AbstractAccessControlService extends AbstractSharedAccessControlService implements INotificationHandler<AccessControlClusterNotification> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractAccessControlService.class);

  @Override
  protected void notifySetPermisions(Permissions p) {
    // notify clients:
    String userId = BEANS.get(IAccessControlService.class).getUserIdOfCurrentSubject();
    BEANS.get(ClientNotificationRegistry.class).putTransactionalForUser(userId, new AccessControlChangedNotification(p));
  }

  @Override
  public void clearCache() {
    clearCacheNoFire();

    //notify clients with a filter, that will be accepted nowhere:
    BEANS.get(ClientNotificationRegistry.class).putTransactionalForAllNodes(new ResetAccessControlChangedNotification());
    distributeCluster(new AccessControlClusterNotification());
  }

  @Override
  public void clearCacheOfUserIds(Collection<String> userIds) {
    clearCacheOfUserIdsNoFire(userIds);
    distributeCluster(new AccessControlClusterNotification(userIds));

    //notify clients:
    for (String userId : userIds) {
      if (userId != null) {
        BEANS.get(ClientNotificationRegistry.class).putTransactionalForUser(userId, new AccessControlChangedNotification(null));
      }
    }
  }

  protected void distributeCluster(Serializable notification) {
    IClusterSynchronizationService s = BEANS.opt(IClusterSynchronizationService.class);
    if (s != null) {
      try {
        if (ITransaction.CURRENT.get() != null) {
          s.publishTransactional(notification);
        }
        else {
          s.publish(notification);
        }
      }
      catch (ProcessingException e) {
        LOG.error("failed notifying cluster for permission changes", e);
      }
    }
  }

  @Override
  public void handleNotification(AccessControlClusterNotification notification) {
    if (notification.getUserIds().isEmpty()) {
      clearCacheNoFire();
    }
    else {
      clearCacheOfUserIdsNoFire(notification.getUserIds());
    }
  }

}
