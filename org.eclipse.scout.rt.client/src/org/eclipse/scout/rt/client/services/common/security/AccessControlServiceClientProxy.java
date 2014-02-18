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
package org.eclipse.scout.rt.client.services.common.security;

import java.security.AllPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientJob;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.security.FineGrainedAccessCheckRequiredException;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.ServiceRegistration;

/**
 * Access control permissions received from backend (JAAS permissions), cached for convenience and performance.
 * <p>
 * Service state is per [{@link IClientSession} instance and stored as {@link IClientSession#getData(String)}
 */
@Priority(-3)
public class AccessControlServiceClientProxy extends AbstractService implements IAccessControlService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AccessControlServiceClientProxy.class);
  private static final String SESSION_DATA_KEY = "accessControlServiceState";

  public AccessControlServiceClientProxy() {
  }

  private ServiceState getServiceState() {
    IClientSession session = ClientJob.getCurrentSession();
    if (session == null) {
      LOG.warn("could not find a client session");
      return null;
    }
    ServiceState data = (ServiceState) session.getData(SESSION_DATA_KEY);
    if (data == null) {
      data = new ServiceState();
      session.setData(SESSION_DATA_KEY, data);
    }
    return data;
  }

  @Override
  public void initializeService(ServiceRegistration registration) {
    super.initializeService(registration);
    // add client notification listener
    SERVICES.getService(IClientNotificationConsumerService.class).addGlobalClientNotificationConsumerListener(new IClientNotificationConsumerListener() {
      @Override
      public void handleEvent(ClientNotificationConsumerEvent e, boolean sync) {
        if (e.getClientNotification().getClass() == AccessControlChangedNotification.class) {
          ServiceState state = getServiceState();
          synchronized (state.m_cacheLock) {
            state.m_permissions = ((AccessControlChangedNotification) e.getClientNotification()).getPermissions();
          }
        }
        else if (e.getClientNotification().getClass() == ResetAccessControlChangedNotification.class) {
          clearCache();
        }
      }
    });
  }

  @Override
  public boolean checkPermission(Permission p) {
    ServiceState state = getServiceState();
    ensureCacheLoaded(state);
    if (p == null) {
      return true;
    }
    if (state.m_permissions == null) {
      return true;
    }
    else {
      Boolean b = state.m_checkPermissionCache.get(p.getName());
      if (b == null) {
        try {
          b = state.m_permissions.implies(p);
        }
        catch (FineGrainedAccessCheckRequiredException e) {
          // must be checked online
          b = getRemoteService().checkPermission(p);
        }
        state.m_checkPermissionCache.put(p.getName(), b);
      }
      return b;
    }
  }

  @Override
  public int getPermissionLevel(Permission p) {
    ServiceState state = getServiceState();
    ensureCacheLoaded(state);
    if (p == null) {
      return BasicHierarchyPermission.LEVEL_NONE;
    }
    if (!(p instanceof BasicHierarchyPermission)) {
      if (checkPermission(p)) {
        return BasicHierarchyPermission.LEVEL_ALL;
      }
      else {
        return BasicHierarchyPermission.LEVEL_NONE;
      }
    }
    BasicHierarchyPermission hp = (BasicHierarchyPermission) p;
    if (state.m_permissions == null) {
      List<Integer> levels = hp.getValidLevels();
      return levels.get(levels.size() - 1);
    }
    else {
      int maxLevel = BasicHierarchyPermission.LEVEL_UNDEFINED;
      Enumeration<Permission> en = state.m_permissions.elements();
      while (en.hasMoreElements()) {
        Permission grantedPermission = en.nextElement();

        // catch AllPermission
        if (grantedPermission instanceof AllPermission) {
          return BasicHierarchyPermission.LEVEL_ALL;
        }

        // process basic hierarchy permissions
        if (grantedPermission instanceof BasicHierarchyPermission) {
          BasicHierarchyPermission hgrantedPermission = (BasicHierarchyPermission) grantedPermission;
          if (hgrantedPermission.getClass().isAssignableFrom(hp.getClass())) {
            maxLevel = Math.max(maxLevel, hgrantedPermission.getLevel());
            if (maxLevel >= BasicHierarchyPermission.LEVEL_ALL) {
              break;
            }
          }
        }
      }
      return maxLevel;
    }
  }

  @Override
  public Permissions getPermissions() {
    ServiceState state = getServiceState();
    ensureCacheLoaded(state);
    return state.m_permissions;
  }

  private void ensureCacheLoaded(ServiceState state) {
    synchronized (state.m_cacheLock) {
      if (state.m_permissions == null) {
        // clear cache
        state.m_checkPermissionCache = new TTLCache<String, Boolean>(BasicHierarchyPermission.getCacheTimeoutMillis());
        // load permissions from backend
        state.m_permissions = getRemoteService().getPermissions();
      }
    }
  }

  @Override
  public boolean isProxyService() {
    return true;
  }

  @Override
  public String getUserIdOfCurrentSubject() {
    return getRemoteService().getUserIdOfCurrentSubject();
  }

  @Override
  public void clearCache() {
    ServiceState state = getServiceState();
    synchronized (state.m_cacheLock) {
      state.m_permissions = null;
    }
  }

  @Override
  public void clearCacheOfUserIds(Collection<String> userIds) {
    //nop
  }

  private IAccessControlService getRemoteService() {
    return ServiceTunnelUtility.createProxy(IAccessControlService.class, ClientSyncJob.getCurrentSession().getServiceTunnel());
  }

  private static class ServiceState {
    final Object m_cacheLock = new Object();
    // permissions cache
    Permissions m_permissions;
    // query cache
    TTLCache<String, Boolean> m_checkPermissionCache = new TTLCache<String, Boolean>();
  }
}
