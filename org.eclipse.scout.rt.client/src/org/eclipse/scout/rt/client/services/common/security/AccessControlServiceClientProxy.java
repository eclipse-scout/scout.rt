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

import java.lang.reflect.Method;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.ClientSyncJob;
import org.eclipse.scout.rt.client.services.common.clientnotification.ClientNotificationConsumerEvent;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerListener;
import org.eclipse.scout.rt.client.services.common.clientnotification.IClientNotificationConsumerService;
import org.eclipse.scout.rt.client.servicetunnel.ServiceTunnelUtility;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.security.FineGrainedAccessCheckRequiredException;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

@Priority(-3)
public class AccessControlServiceClientProxy extends AbstractService implements IAccessControlService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AccessControlServiceClientProxy.class);

  private final Object m_cacheLock = new Object();
  // permissions cache
  private Permissions m_permissions;
  // query cache
  private TTLCache<String, Boolean> m_checkPermissionCache = new TTLCache<String, Boolean>();

  public AccessControlServiceClientProxy() {
  }

  @Override
  public void initializeService() {
    super.initializeService();
    // add client notification listener
    SERVICES.getService(IClientNotificationConsumerService.class).addClientNotificationConsumerListener(new IClientNotificationConsumerListener() {
      public void handleEvent(ClientNotificationConsumerEvent e, boolean sync) {
        if (e.getClientNotification().getClass() == AccessControlChangedNotification.class) {
          synchronized (m_cacheLock) {
            m_permissions = ((AccessControlChangedNotification) e.getClientNotification()).getPermissions();
          }
        }
        else if (e.getClientNotification().getClass() == ResetAccessControlChangedNotification.class) {
          clearCache();
        }
      }
    });
  }

  public boolean checkPermission(Permission p) {
    ensureCacheLoaded();
    if (p == null) {
      return true;
    }
    if (m_permissions == null) {
      return true;
    }
    else {
      Boolean b = m_checkPermissionCache.get(p.getName());
      if (b == null) {
        try {
          b = m_permissions.implies(p);
        }
        catch (FineGrainedAccessCheckRequiredException e) {
          // must be checked online
          b = getRemoteService().checkPermission(p);
        }
        m_checkPermissionCache.put(p.getName(), b);
      }
      return b;
    }
  }

  public int getPermissionLevel(Permission p) {
    ensureCacheLoaded();
    if (p == null) {
      return BasicHierarchyPermission.LEVEL_NONE;
    }
    if (!(p instanceof BasicHierarchyPermission)) {
      if (checkPermission(p)) return BasicHierarchyPermission.LEVEL_ALL;
      else return BasicHierarchyPermission.LEVEL_NONE;
    }
    BasicHierarchyPermission hp = (BasicHierarchyPermission) p;
    if (m_permissions == null) {
      List<Integer> levels = hp.getValidLevels();
      return levels.get(levels.size() - 1);
    }
    else {
      int maxLevel = BasicHierarchyPermission.LEVEL_UNDEFINED;
      Enumeration<Permission> en = m_permissions.elements();
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

  public Permissions getPermissions() {
    ensureCacheLoaded();
    return m_permissions;
  }

  private void ensureCacheLoaded() {
    synchronized (m_cacheLock) {
      if (m_permissions == null) {
        // clear cache
        m_checkPermissionCache = new TTLCache<String, Boolean>(BasicHierarchyPermission.getCacheTimeoutMillis());
        // load permissions from backend
        m_permissions = getRemoteService().getPermissions();
      }
    }
  }

  public boolean isProxyService() {
    return true;
  }

  public void clearCache() {
    synchronized (m_cacheLock) {
      m_permissions = null;
    }
  }

  public void clearCacheOfPrincipals(String... principalNames) {
    //nop
  }

  private IAccessControlService getRemoteService() {
    return ServiceTunnelUtility.createProxy(IAccessControlService.class, ClientSyncJob.getCurrentSession().getServiceTunnel());
  }

  /**
   * no service tunnel access on client side
   */
  public boolean checkServiceTunnelAccess(Class serviceInterface, Method method, Object[] args) {
    return false;
  }
}
