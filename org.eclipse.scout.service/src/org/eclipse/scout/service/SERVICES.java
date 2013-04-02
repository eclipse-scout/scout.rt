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
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.service.internal.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Utility class for querying registered OSGi services
 * <p>
 * There might be log warnings when a service returns null due to factory visiblity decisions. see bug
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=299351 (solved with eclipse 3.6)
 * <p>
 * see also {@link INullService}
 */
public final class SERVICES {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SERVICES.class);

  private SERVICES() {
  }

  /**
   * @param serviceInterfaceClass
   *          The interface or class name with which the service was registered.
   * @return the service with the highest ranking or <code>null</code>, if the service is not found.
   */
  public static <T extends Object> T getService(Class<T> serviceInterfaceClass) {
    return getService(serviceInterfaceClass, null);
  }

  /**
   * @param serviceInterfaceClass
   *          The interface or class name with which the service was registered.
   * @param filter
   *          The filter expression or {@code null}. Invalid filter expressions are ignored.
   * @return the service with the highest ranking
   */
  public static <T extends Object> T getService(Class<T> serviceInterfaceClass, String filter) {
    Activator a = Activator.getDefault();
    if (a == null || serviceInterfaceClass == null) {
      return null;
    }
    // start the tracker, blocks until all service factories have been registered
    a.getServicesExtensionManager().start();
    BundleContext context = a.getBundle().getBundleContext();
    if (context == null) {
      return null;
    }

    ServiceReference serviceReference = null;
    if (filter == null) {
      //If no filter is specified directly get the serviceReference with the highest ranking
      serviceReference = context.getServiceReference(serviceInterfaceClass.getName());

      if (serviceReference != null) {
        T service = resolveService(serviceInterfaceClass, context, serviceReference);
        if (service != null) {
          return service;
        }
      }
    }

    ServiceReference[] refs = null;
    try {
      refs = context.getAllServiceReferences(serviceInterfaceClass.getName(), filter);
    }
    catch (InvalidSyntaxException e) {
      // nop
    }

    if (refs != null) {
      for (ServiceReference ref : refs) {
        T service = resolveService(serviceInterfaceClass, context, ref);
        if (service != null) {
          return service;
        }
      }
    }
    return null;
  }

  /**
   * safely get and immediately unget the service in an atomic section using the service reference as lock
   */
  @SuppressWarnings("unchecked")
  private static <T extends Object> T resolveService(Class<T> serviceInterfaceClass, BundleContext context, ServiceReference ref) {
    if (ref == null) {
      return null;
    }
    synchronized (ref) {
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
    return null;
  }

  /**
   * @return the services ordered by ranking or an empty array, if no services are found.
   */
  public static <T extends Object> T[] getServices(Class<T> serviceInterfaceClass) {
    return getServices(serviceInterfaceClass, null);
  }

  /**
   * @param serviceInterfaceClass
   *          The interface or class name with which the service was registered.
   * @param filter
   *          The filter expression or {@code null}. Invalid filter expressions are ignored.
   * @return the services ordered by ranking or an empty array, if no services are found.
   */
  @SuppressWarnings("unchecked")
  public static <T extends Object> T[] getServices(Class<T> serviceInterfaceClass, String filter) {
    Activator a = Activator.getDefault();
    if (a == null || serviceInterfaceClass == null) {
      return (T[]) Array.newInstance(serviceInterfaceClass, 0);
    }
    // start the tracker, blocks until all service factories have been
    // registered
    a.getServicesExtensionManager().start();
    BundleContext context = a.getBundle().getBundleContext();
    if (context == null) {
      return (T[]) Array.newInstance(serviceInterfaceClass, 0);
    }
    ServiceReference[] refs = null;
    try {
      refs = context.getAllServiceReferences(serviceInterfaceClass.getName(), filter);
    }
    catch (InvalidSyntaxException e) {
      // nop
    }
    if (refs != null) {
      Arrays.sort(refs, new Comparator<ServiceReference>() {
        @Override
        public int compare(ServiceReference ref1, ServiceReference ref2) {
          return ((Comparable) ref2.getProperty(Constants.SERVICE_RANKING)).compareTo(((Comparable) ref1.getProperty(Constants.SERVICE_RANKING)));
        }
      });
      ArrayList<T> list = new ArrayList<T>(refs.length);
      for (ServiceReference ref : refs) {
        T s = resolveService(serviceInterfaceClass, context, ref);
        if (s != null) {
          list.add((T) s);
        }
      }
      return list.toArray((T[]) Array.newInstance(serviceInterfaceClass, list.size()));
    }
    return (T[]) Array.newInstance(serviceInterfaceClass, 0);
  }

  private static Object safeGetService(BundleContext context, ServiceReference ref) {
    @SuppressWarnings("unchecked")
    Object o = context.getService(ref);
    if (o instanceof INullService) {
      o = null;
    }
    return o;
  }
}
