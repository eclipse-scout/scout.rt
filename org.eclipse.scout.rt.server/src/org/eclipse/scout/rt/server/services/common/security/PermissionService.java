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

import org.eclipse.core.runtime.Platform;
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
          // Skip uninteresting bundles
          if (!acceptBundle(bundle)) {
            continue;
          }
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
              if (acceptClassName(bundle, className)) {
                try {
                  Class c = null;
                  c = bundle.loadClass(className);
                  if (acceptClass(bundle, c)) {
                    discoveredPermissions.add(new BundleClassDescriptor(bundle.getSymbolicName(), c.getName()));
                  }

                }
                catch (Throwable t) {
                  // nop
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
   * class name contains <em>Permission</em>.
   * 
   * @deprecated replaced by {@link #acceptClassName(Bundle, String)}. Will be removed in Release 3.10.
   * @param className
   *          The class name to check.
   * @return Returns <code>true</code> if the given class looks like a permission. Otherwise <code>false</code>.
   */
  @Deprecated
  protected boolean isCandidate(String className) {
    return className.indexOf("Permission") >= 0;
  }

  /**
   * Checks whether the given bundle should be scanned for permission classes. The default implementations accepts
   * all bundles that are not fragments (because classes from fragments are automatically read when browsing the host
   * bundle).
   * 
   * @return Returns <code>true</code> if the given bundle meets the requirements to be scanned for permission classes.
   *         <code>false</code> otherwise.
   */
  protected boolean acceptBundle(Bundle bundle) {
    return !Platform.isFragment(bundle);
  }

  /**
   * Checks whether the given class name is a potential permission class. Class names that do not meet the
   * requirements of this method are not considered further, i.e. the "expensive" class instantiation is skipped.
   * The default implementation checks whether the class name contains <code>"Permission"</code>.
   * 
   * @param bundle
   *          The class's hosting bundle
   * @param className
   *          the class name to be checked
   * @return Returns <code>true</code> if the given class name meets the requirements to be considered as a permission
   *         class. <code>false</code> otherwise.
   */
  protected boolean acceptClassName(Bundle bundle, String className) {
    return isCandidate(className);
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
   * @param bundle
   *          The class's hosting bundle
   * @param c
   *          the class to be checked
   * @return Returns <code>true</code> if the class is a permission class. <code>false</code> otherwise.
   */
  protected boolean acceptClass(Bundle bundle, Class<?> c) {
    if (Permission.class.isAssignableFrom(c)) {
      if (!c.isInterface()) {
        int flags = c.getModifiers();
        if (Modifier.isPublic(flags) && !Modifier.isAbstract(flags)) {
          return true;
        }
      }
    }
    return false;
  }
}
