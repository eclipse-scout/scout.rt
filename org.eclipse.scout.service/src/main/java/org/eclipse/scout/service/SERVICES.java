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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.cdi.CDI;
import org.eclipse.scout.rt.platform.cdi.IBean;
import org.eclipse.scout.rt.platform.cdi.IInterceptedBean;
import org.eclipse.scout.rt.platform.cdi.OBJ;
import org.eclipse.scout.rt.platform.cdi.internal.BeanContext;

/**
 * Utility class for querying registered OSGi services
 * <p>
 * There might be log warnings when a service returns null due to factory visiblity decisions. see bug
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=299351 (solved with eclipse 3.6)
 * <p>
 * see also {@link INullService}
 * <p>
 * Since 4.0 there is a service cache for much better performance. Set the config.ini property
 * <code>org.eclipse.scout.service.cache.enabled=true</code> to use it. Call {@link #clearCache()} to clear it at
 * runtime.
 */
public final class SERVICES {

  private SERVICES() {

  }

  /**
   * @param serviceInterfaceClass
   *          The interface or class name with which the service was registered.
   * @return the service with the highest ranking or <code>null</code>, if the service is not found.
   */
  public static <T extends Object> T getService(Class<T> serviceClass) {
    if (Assertions.assertNotNull(serviceClass).isInterface()) {
      return OBJ.one(serviceClass);
    }
    else {
      // workaround since interceptors are only possible on interfaces (Proxy instances). So we try to finde the wrapped original bean and return it without interceptors.
      return getServicesWithoutInterceptors(serviceClass, true).get(0);
    }
  }

  /**
   * @return the services ordered by ranking or an empty array, if no services are found.
   */
  public static <T extends Object> List<T> getServices(Class<T> serviceClass) {
    if (Assertions.assertNotNull(serviceClass).isInterface()) {
      return OBJ.all(serviceClass);
    }
    else {
      // workaround since interceptors are only possible on interfaces (Proxy instances). So we try to finde the wrapped original bean and return it without interceptors.
      return getServicesWithoutInterceptors(serviceClass, false);
    }
  }

  private static <T extends Object> List<T> getServicesWithoutInterceptors(Class<T> serviceClass, boolean onlyFirst) {
    List<IBean<T>> beans = ((BeanContext) CDI.getBeanContext()).getBeansWithoutInterceptionCheck(serviceClass);
    List<T> services = new ArrayList<T>(onlyFirst ? 1 : beans.size());
    for (IBean<T> bean : beans) {
      services.add(getBeanWithoutInterceptors(bean).get());
      if (onlyFirst) {
        break;
      }
    }
    return services;
  }

  /**
   * potentially are asdf
   *
   * @param potentially
   * @return
   */
  @SuppressWarnings("unchecked")
  private static <T> IBean<T> getBeanWithoutInterceptors(IBean<T> potentiallyInterceptedBean) {
    if (Assertions.assertNotNull(potentiallyInterceptedBean).isIntercepted()) {
      return getBeanWithoutInterceptors(((IInterceptedBean<T>) potentiallyInterceptedBean).getInteceptedBean());
    }
    else {
      return potentiallyInterceptedBean;
    }
  }

}
