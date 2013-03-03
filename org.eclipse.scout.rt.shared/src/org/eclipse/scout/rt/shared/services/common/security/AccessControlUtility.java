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

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.TypeCastUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.rt.shared.security.BasicHierarchyPermission;
import org.eclipse.scout.service.SERVICES;
import org.osgi.framework.Bundle;

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
   * @param classLoaderBundle
   *          bundle used to load the permission classes.
   * @deprecated this method does not work if permission classes are defined in different bundles. Use
   *             {@link AccessControlUtility#createPermissions(Object[][])} instead. Will be removed in Release 3.10.
   */
  @Deprecated
  public static Permissions createPermissions(Object[][] permissionData, Bundle classLoaderBundle) {
    return createPermissions(permissionData);
  }

  /**
   * Convenience to create permissions using a raw data matrix containing rows, columns
   * 
   * @param matrix
   *          is expected to contain the columns PERMISSION_CLASS (String),
   *          PERMISSION_LEVEL (Integer)
   */
  public static Permissions createPermissions(Object[][] permissionData) {
    HashMap<String, BundleClassDescriptor> permissionNameToBundleClassDesc;
    permissionNameToBundleClassDesc = new HashMap<String, BundleClassDescriptor>();
    IPermissionService psvc = SERVICES.getService(IPermissionService.class);
    if (psvc != null) {
      for (BundleClassDescriptor d : psvc.getAllPermissionClasses()) {
        permissionNameToBundleClassDesc.put(d.getSimpleClassName(), d);
        permissionNameToBundleClassDesc.put(d.getClassName(), d);
      }
    }
    //
    Permissions permSet = new Permissions();
    for (Object[] permissionRow : permissionData) {
      String name = "" + permissionRow[0];
      /*
       * Legacy migration com.bsiag.scout.shared.security.X ->
       * org.eclipse.scout.rt.shared.security.X
       */
      name = name.replace("com.bsiag.scout.shared.security.", "org.eclipse.scout.rt.shared.security.");
      int level = TypeCastUtility.castValue(permissionRow[1], Integer.class);
      try {
        BundleClassDescriptor desc = permissionNameToBundleClassDesc.get(name);
        if (desc == null) {
          LOG.warn("Unknown permission with name: " + permissionRow[0]);
          continue;
        }
        Bundle bundle = Platform.getBundle(desc.getBundleSymbolicName());
        Class c = bundle.loadClass(desc.getClassName());
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
