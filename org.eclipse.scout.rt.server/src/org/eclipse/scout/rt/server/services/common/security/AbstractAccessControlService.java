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

import java.lang.reflect.Method;
import java.security.AllPermission;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.services.common.security.internal.AccessControlStore;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.security.RemoteServiceAccessPermission;
import org.eclipse.scout.rt.shared.services.common.ping.IPingService;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.SERVICES;

/**
 * Implementations should override {@link #execLoadPermissions()}
 */
@Priority(-1)
public class AbstractAccessControlService extends AbstractService implements IAccessControlService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractAccessControlService.class);

  private AccessControlStore m_accessControlStore;

  public AbstractAccessControlService() {
  }

  @SuppressWarnings("deprecation")
  @Override
  public void initializeService() {
    m_accessControlStore = new AccessControlStore();
    super.initializeService();
  }

  @Override
  public boolean checkPermission(Permission p) {
    if (p == null) {
      return true;
    }
    Permissions c = getPermissions();
    if (c == null) {
      return true;
    }
    else {
      return c.implies(p);
    }
  }

  @Override
  public int getPermissionLevel(Permission p) {
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
    Permissions c = getPermissions();
    if (c == null) {
      List<Integer> levels = hp.getValidLevels();
      return levels.get(levels.size() - 1);
    }
    else {
      int maxLevel = BasicHierarchyPermission.LEVEL_UNDEFINED;
      Enumeration<Permission> en = c.elements();
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
    Permissions permSet = m_accessControlStore.getPermissionsOfCurrentSubject();

    if (permSet != null) {
      return permSet;
    }

    setPermissions(execLoadPermissions());
    permSet = m_accessControlStore.getPermissionsOfCurrentSubject();

    return permSet;
  }

  /**
   * This default implementation does nothing
   * Override this method to retrieve permissions from a custom store
   */
  protected Permissions execLoadPermissions() {
    return null;
  }

  private void setPermissions(Permissions p) {
    //legacy support: if there are no remote service permissions available, warn and add default rule to allow shared interfaces
    //to support legacy functionality, this default also accepts other so far valid requests but generates a warning.
    //a future release will throw a {@link SecurityException} when no permission is granted.
    if (p != null) {
      if (!p.implies(new RemoteServiceAccessPermission(IPingService.class.getName(), "ping"))) {
        boolean existsAny = false;
        for (Enumeration<Permission> en = p.elements(); en.hasMoreElements();) {
          Permission perm = en.nextElement();
          if (perm instanceof RemoteServiceAccessPermission) {
            existsAny = true;
            break;
          }
        }
        if (!existsAny) {
          LOG.warn("Legacy security hint: missing any RemoteServiceAccessPermissions in AccessController. Please verify the " + SERVICES.getService(IAccessControlService.class).getClass() + " to include such permissions for accessing services using client proxies. Adding default rule to allow services of pattern '*.shared.*'");
          p.add(new RemoteServiceAccessPermission("*.shared.*", "*"));
        }
      }
    }
    //end legacy
    m_accessControlStore.setPermissionsOfCurrentSubject(p);
  }

  @Override
  public boolean isProxyService() {
    return false;
  }

  @Override
  public void clearCache() {
    m_accessControlStore.clearCache();
  }

  @Override
  public void clearCacheOfPrincipals(String... principalNames) {
    if (principalNames == null) {
      return;
    }
    String[] principals = m_accessControlStore.getPrincipalNames();
    ArrayList<String> toDelete = new ArrayList<String>();
    for (String name : principalNames) {
      if (name != null) {
        name = name.toLowerCase();
        for (String p : principals) {
          if (p.equals(name) || p.endsWith("\\" + name)) {
            toDelete.add(p);
          }
        }
      }
    }
    m_accessControlStore.clearCacheOfPrincipals(toDelete.toArray(new String[toDelete.size()]));
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean checkServiceTunnelAccess(Class serviceInterfaceClass, Method method, Object[] args) {
    return false;
  }
}
