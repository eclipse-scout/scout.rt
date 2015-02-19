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

import java.util.List;

import org.eclipse.scout.rt.platform.cdi.OBJ;

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
  public static <T extends Object> T getService(Class<T> serviceInterfaceClass) {
    return OBJ.NEW(serviceInterfaceClass);
  }

  /**
   * @return the services ordered by ranking or an empty array, if no services are found.
   */
  public static <T extends Object> List<T> getServices(Class<T> serviceInterfaceClass) {
    return OBJ.ALL(serviceInterfaceClass);
  }

}
