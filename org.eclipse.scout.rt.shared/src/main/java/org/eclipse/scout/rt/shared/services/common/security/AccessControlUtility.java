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
package org.eclipse.scout.rt.shared.services.common.security;

import java.security.Permission;
import java.security.Permissions;
import java.util.HashMap;

import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;

/**
 *
 */
public final class AccessControlUtility {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AccessControlUtility.class);

  private AccessControlUtility() {
  }

  /**
   * Convenience to create permissions using a raw data matrix containing rows, columns
   *
   * @param matrix
   *          is expected to contain the columns PERMISSION_CLASS (String),
   *          PERMISSION_LEVEL (Integer)
   */
  public static Permissions createPermissions(Object[][] permissionData) {
    HashMap<String, Class<? extends Permission>> permissionNameToBundleClassDesc = new HashMap<>();
    IPermissionService psvc = BEANS.get(IPermissionService.class);
    if (psvc != null) {
      for (Class<? extends Permission> d : psvc.getAllPermissionClasses()) {
        permissionNameToBundleClassDesc.put(d.getSimpleName(), d);
        permissionNameToBundleClassDesc.put(d.getName(), d);
      }
    }
    //
    Permissions permSet = new Permissions();
    ClassLoader classLoader = AccessControlUtility.class.getClassLoader();
    for (Object[] permissionRow : permissionData) {
      String name = "" + permissionRow[0];
      /*
       * Legacy migration com.bsiag.scout.shared.security.X -> org.eclipse.scout.rt.shared.security.X
       */
      name = name.replace("com.bsiag.scout.shared.security.", "org.eclipse.scout.rt.shared.security.");

      int level = TypeCastUtility.castValue(permissionRow[1], Integer.class);
      try {
        Class desc = permissionNameToBundleClassDesc.get(name);
        if (desc == null) {
          LOG.warn("Unknown permission with name: " + permissionRow[0]);
          continue;
        }
        Class c = classLoader.loadClass(desc.getName());
        Permission p = (Permission) c.newInstance();
        if (p instanceof BasicHierarchyPermission) {
          ((BasicHierarchyPermission) p).setLevel(level);
          ((BasicHierarchyPermission) p).setReadOnly();
        }
        permSet.add(p);
      }
      catch (ClassNotFoundException e) {
        LOG.warn("Unknown permission with name: " + permissionRow[0]);
      }
      catch (InstantiationException e) {
        LOG.warn("Unable to load user-permission: " + permissionRow[0]);
      }
      catch (IllegalAccessException e) {
        LOG.warn("Unable to access user-permission: " + permissionRow[0]);
      }
    }
    return permSet;
  }
}
