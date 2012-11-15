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

import java.lang.reflect.Modifier;
import java.security.Permission;
import java.util.HashSet;

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.osgi.BundleClassDescriptor;
import org.eclipse.scout.commons.runtime.BundleBrowser;
import org.eclipse.scout.rt.server.internal.Activator;
import org.eclipse.scout.rt.shared.services.common.security.IPermissionService;
import org.eclipse.scout.service.AbstractService;
import org.osgi.framework.Bundle;

/**
 * delegates to {@link PermissionStore}
 */
@Priority(-1)
public class PermissionService extends AbstractService implements IPermissionService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(PermissionService.class);

  private Object m_permissionClassesLock = new Object();
  private BundleClassDescriptor[] m_permissionClasses;

  public PermissionService() {
  }

  @Override
  public BundleClassDescriptor[] getAllPermissionClasses() {
    checkCache();
    return m_permissionClasses;
  }

  private void checkCache() {
    synchronized (m_permissionClassesLock) {
      // null-check with lock (valid check)
      if (m_permissionClasses == null) {
        HashSet<BundleClassDescriptor> discoveredPermissions = new HashSet<BundleClassDescriptor>();
        for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
          String[] classNames = null;
          try {
            BundleBrowser bundleBrowser = new BundleBrowser(bundle.getSymbolicName(), bundle.getSymbolicName());
            classNames = bundleBrowser.getClasses(false, true);
          }
          catch (Exception e1) {
            LOG.warn(null, e1);
          }
          if (classNames != null) {
            // filter
            for (String className : classNames) {
              // fast pre-check
              if (isCandidate(className)) {
                try {
                  Class c = null;
                  c = bundle.loadClass(className);
                  if (Permission.class.isAssignableFrom(c)) {
                    if (!c.isInterface()) {
                      int flags = c.getModifiers();
                      if (Modifier.isPublic(flags) && !Modifier.isAbstract(flags)) {
                        discoveredPermissions.add(new BundleClassDescriptor(bundle.getSymbolicName(), c.getName()));
                      }
                    }
                  }
                }
                catch (Throwable t) {
                }
              }
            }
          }
        }
        m_permissionClasses = discoveredPermissions.toArray(new BundleClassDescriptor[discoveredPermissions.size()]);
      }
    }
  }

  /**
   * Checks whether the given class name is a potential permission class. This default implementation checks whether the
   * class name contains <em>Permission</em> and that the class's package path contains an segment called
   * <em>security</em>.
   * 
   * @param className
   *          The class name to check.
   * @return Returns <code>true</code> if the given class looks like a permission. Otherwise <code>false</code>.
   */
  protected boolean isCandidate(String className) {
    return className.indexOf("Permission") >= 0 && className.indexOf(".security.") >= 0;
  }
}
