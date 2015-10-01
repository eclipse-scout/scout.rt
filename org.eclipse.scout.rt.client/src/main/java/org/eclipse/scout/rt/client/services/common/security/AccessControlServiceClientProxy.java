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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.commons.TTLCache;
import org.eclipse.scout.commons.annotations.Order;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.Client;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.CreateImmediately;
import org.eclipse.scout.rt.shared.notification.INotificationHandler;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.security.FineGrainedAccessCheckRequiredException;
import org.eclipse.scout.rt.shared.services.common.security.AccessControlChangedNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlNotification;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ResetAccessControlChangedNotification;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelUtility;

/**
 * Access control permissions received from backend (JAAS permissions), cached for convenience and performance.
 * <p>
 * Service state is per {@link IClientSession} instance and stored as {@link IClientSession#getData(String)}
 */
@Client
@Order(10)
@CreateImmediately
public class AccessControlServiceClientProxy implements IAccessControlService, INotificationHandler<IAccessControlNotification> {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AccessControlServiceClientProxy.class);
  private static final String SESSION_DATA_KEY = "accessControlServiceState";

  private ServiceState getServiceState() {
    IClientSession session = ClientSessionProvider.currentSession();
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
  public void handleNotification(IAccessControlNotification notification) {
    if (notification.getClass() == AccessControlChangedNotification.class) {
      ServiceState state = getServiceState();
      // use tryLock because the ensureCacheLoaded() calls the backend which triggers a notification import (that's why this method may be invoked)
      // in this case don't lock because it is already held by ensureCacheLoaded() and the notifications can be ignored because the ensureCacheLoaded() writes them.
      if (state.m_lock.tryLock()) {
        try {
          state.m_permissions = ((AccessControlChangedNotification) notification).getPermissions();
        }
        finally {
          state.m_lock.unlock();
        }
      }
    }
    else if (notification.getClass() == ResetAccessControlChangedNotification.class) {
      clearCache();
    }
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
    state.m_lock.lock();
    try {
      if (state.m_permissions == null) {
        // clear cache
        state.m_checkPermissionCache = new TTLCache<String, Boolean>(BasicHierarchyPermission.getCacheTimeoutMillis());
        // load permissions from backend
        state.m_permissions = getRemoteService().getPermissions();
      }
    }
    finally {
      state.m_lock.unlock();
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
    state.m_lock.lock();
    try {
      state.m_permissions = null;
    }
    finally {
      state.m_lock.unlock();
    }
  }

  @Override
  public void clearCacheOfUserIds(Collection<String> userIds) {
    //nop
  }

  private IAccessControlService getRemoteService() {
    return ServiceTunnelUtility.createProxy(IAccessControlService.class);
  }

  private static class ServiceState {
    final Lock m_lock = new ReentrantLock();
    // permissions cache
    Permissions m_permissions;
    // query cache
    TTLCache<String, Boolean> m_checkPermissionCache = new TTLCache<String, Boolean>();
  }
}
