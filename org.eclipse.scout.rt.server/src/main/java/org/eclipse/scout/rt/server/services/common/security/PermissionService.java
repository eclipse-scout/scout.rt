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

import java.security.BasicPermission;
import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.IgnoreBean;
import org.eclipse.scout.rt.platform.inventory.ClassInventory;
import org.eclipse.scout.rt.platform.inventory.IClassInfo;
import org.eclipse.scout.rt.platform.inventory.IClassInventory;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;

public class PermissionService implements IPermissionService {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PermissionService.class);

  private final Object m_permissionClassesLock = new Object();
  private Set<Class<? extends Permission>> m_permissionClasses;

  @Override
  public Set<Class<? extends Permission>> getAllPermissionClasses() {
    checkCache();
    return CollectionUtility.hashSet(m_permissionClasses);
  }

  private void checkCache() {
    synchronized (m_permissionClassesLock) {
      // null-check with lock (valid check)
      if (m_permissionClasses == null) {
        Set<IClassInfo> allKnownPermissions = getPermissionsFromInventory();
        Set<Class<? extends Permission>> discoveredPermissions = new HashSet<>(allKnownPermissions.size());
        for (IClassInfo permInfo : allKnownPermissions) {
          if (acceptClass(permInfo)) {
            try {
              @SuppressWarnings("unchecked")
              Class<? extends Permission> permClass = (Class<? extends Permission>) permInfo.resolveClass();
              discoveredPermissions.add(permClass);
            }
            catch (Exception e) {
              LOG.error("Unable to load permission.", e);
            }
          }
        }
        m_permissionClasses = CollectionUtility.hashSet(discoveredPermissions);
      }
    }
  }

  /**
   * @return permission classes from class inventory.
   */
  protected Set<IClassInfo> getPermissionsFromInventory() {
    IClassInventory inv = ClassInventory.get();
    //get BasicPermssion subclasses are not found directly, because jdk is not scanned by jandex.
    Set<IClassInfo> classes = inv.getAllKnownSubClasses(Permission.class);
    classes.addAll(inv.getAllKnownSubClasses(BasicPermission.class));
    return classes;
  }

  /**
   * Checks whether the given class name is a potential permission class. Class names that do not meet the
   * requirements of this method are not considered further.
   *
   * @param className
   *          the class name to be checked
   * @return Returns <code>true</code> if the given class name meets the requirements to be considered as a permission
   *         class. <code>false</code> otherwise.
   */
  protected boolean acceptClassName(String className) {
    return true;
  }

  /**
   * Checks whether the given class is a Permission class that should be visible to this service. The default
   * implementation checks if the class meets the following conditions:
   * <ul>
   * <li>subclass of {@link Permission}
   * <li><code>public</code>
   * <li>not an <code>interface</code>
   * <li>not <code>abstract</code>
   * </ul>
   *
   * @param permInfo
   *          the class to be checked
   * @return Returns <code>true</code> if the class is a permission class. <code>false</code> otherwise.
   */
  protected boolean acceptClass(IClassInfo permInfo) {
    return permInfo.isInstanciable() && !permInfo.hasAnnotation(IgnoreBean.class) && acceptClassName(permInfo.name());
  }
}
