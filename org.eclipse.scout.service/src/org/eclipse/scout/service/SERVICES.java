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
package org.eclipse.scout.service;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Convenience for
 * AbstractSession.getAbstractSession().getServiceRegistry().getService()
 * <p>
 * There might be log warnings when a service returns null due to factory visiblity decisions. see bug
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=299351
 */
public final class SERVICES {
  @SuppressWarnings("unused")
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SERVICES.class);

  private SERVICES() {
  }

  /**
   * @return the service with the highest ranking
   */
  @SuppressWarnings("unchecked")
  public static <T extends Object> T getService(Class<T> serviceInterfaceClass) {
    return getService(serviceInterfaceClass, null);
  }

  /**
   * @return the service with the highest ranking
   */
  @SuppressWarnings("unchecked")
  public static <T extends Object> T getService(Class<T> serviceInterfaceClass, String filter) {
    Activator a = Activator.getDefault();
    if (a == null || serviceInterfaceClass == null) return null;
    // start the tracker, blocks until all service factories have been registered
    a.getServicesExtensionManager().start();
    BundleContext context = a.getBundle().getBundleContext();
    if (context == null) return null;
    ServiceReference[] refs = null;
    try {
      refs = context.getAllServiceReferences(serviceInterfaceClass.getName(), filter);
    }
    catch (InvalidSyntaxException e) {
      // nop
    }
    if (refs != null) {
      for (ServiceReference ref : refs) {
        try {
          Object s = safeGetService(context, ref);
          if (s != null && serviceInterfaceClass.isAssignableFrom(s.getClass())) {
            return (T) s;
          }
        }
        finally {
          context.ungetService(ref);
        }
      }
    }
    return null;
  }

  /**
   * @return the services in order of registration (not by ranking)
   */
  public static <T extends Object> T[] getServices(Class<T> serviceInterfaceClass) {
    return getServices(serviceInterfaceClass, null);
  }

  /**
   * @return the services in order of registration (not by ranking)
   */
  @SuppressWarnings("unchecked")
  public static <T extends Object> T[] getServices(Class<T> serviceInterfaceClass, String filter) {
    Activator a = Activator.getDefault();
    if (a == null || serviceInterfaceClass == null) return (T[]) Array.newInstance(serviceInterfaceClass, 0);
    // start the tracker, blocks until all service factories have been
    // registered
    a.getServicesExtensionManager().start();
    BundleContext context = a.getBundle().getBundleContext();
    if (context == null) return (T[]) Array.newInstance(serviceInterfaceClass, 0);
    ServiceReference[] refs = null;
    try {
      refs = context.getAllServiceReferences(serviceInterfaceClass.getName(), filter);
    }
    catch (InvalidSyntaxException e) {
      // nop
    }
    if (refs != null) {
      try {
        ArrayList<T> list = new ArrayList<T>(refs.length);
        for (ServiceReference ref : refs) {
          Object s = safeGetService(context, ref);
          if (s != null && serviceInterfaceClass.isAssignableFrom(s.getClass())) {
            list.add((T) s);
          }
        }
        return list.toArray((T[]) Array.newInstance(serviceInterfaceClass, list.size()));
      }
      finally {
        for (ServiceReference ref : refs) {
          context.ungetService(ref);
        }
      }
    }
    return (T[]) Array.newInstance(serviceInterfaceClass, 0);
  }

  private static Object safeGetService(BundleContext context, ServiceReference ref) {
    return context.getService(ref);
  }
}
