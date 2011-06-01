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

import org.eclipse.scout.commons.VerboseUtility;
import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.server.services.common.security.internal.AccessControlStore;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.servicetunnel.ServiceTunnelAccessDenied;
import org.eclipse.scout.service.AbstractService;
import org.eclipse.scout.service.IService;

@Priority(-1)
public class AbstractAccessControlService extends AbstractService implements IAccessControlService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractAccessControlService.class);

  private AccessControlStore m_accessControlStore;
  //this property will be true by default in a future release
  private boolean m_remoteAccessPolicyEnabled = false;

  public AbstractAccessControlService() {
    String s = Activator.getDefault().getBundle().getBundleContext().getProperty(PROPERTY_POLICY_ENABLED);
    if (s != null) {
      m_remoteAccessPolicyEnabled = ("true".equals(s));
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void initializeService() {
    m_accessControlStore = new AccessControlStore();
    super.initializeService();
  }

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

  public int getPermissionLevel(Permission p) {
    if (p == null) {
      return BasicHierarchyPermission.LEVEL_NONE;
    }
    if (!(p instanceof BasicHierarchyPermission)) {
      if (checkPermission(p)) return BasicHierarchyPermission.LEVEL_ALL;
      else return BasicHierarchyPermission.LEVEL_NONE;
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
   */
  protected Permissions execLoadPermissions() {
    return null;
  }

  private void setPermissions(Permissions p) {
    m_accessControlStore.setPermissionsOfCurrentSubject(p);
  }

  public boolean isProxyService() {
    return false;
  }

  public void clearCache() {
    m_accessControlStore.clearCache();
  }

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

  public boolean checkServiceTunnelAccess(Class serviceInterfaceClass, Method method, Object[] args) {
    try {
      //check: must be an interface
      if (!serviceInterfaceClass.isInterface()) {
        throw new SecurityException("access to " + serviceInterfaceClass + " denied.");
      }
      //check: must be a subclass of IService
      if (!IService.class.isAssignableFrom(serviceInterfaceClass)) {
        throw new SecurityException("remote acess to non-IService type: " + serviceInterfaceClass);
      }
      //check: method is defined on service interface itself
      Method verifyMethod = serviceInterfaceClass.getMethod(method.getName(), method.getParameterTypes());
      //check: method annotation exception
      if (verifyMethod.getAnnotation(ServiceTunnelAccessDenied.class) != null) {
        throw new SecurityException("ServiceTunnelAccessDenied by annotation ServiceTunnelAccessDenied");
      }
      if (method.getAnnotation(ServiceTunnelAccessDenied.class) != null) {
        throw new SecurityException("ServiceTunnelAccessDenied by annotation ServiceTunnelAccessDenied");
      }
      //check: explicitly granted service interfaces
      //XXX
    }
    catch (Throwable t) {
      LOG.warn("illegal service tunnel access to " + serviceInterfaceClass + "#" + method.getName() + " with arguments " + VerboseUtility.dumpObject(args), t);
    }
    //default
    if (!m_remoteAccessPolicyEnabled) {
      return true;
    }
    throw new SecurityException("ServiceTunnelAccessDenied by annotation ServiceTunnelAccessDenied");
  }
}
