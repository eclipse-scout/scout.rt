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

import org.eclipse.scout.rt.platform.OBJ;

/**
 * Utility class for querying registered Scout services
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
    return OBJ.get(serviceClass);
  }

  /**
   * @return the services ordered by ranking or an empty array, if no services are found.
   */
  public static <T extends Object> List<T> getServices(Class<T> serviceClass) {
    return OBJ.all(serviceClass);
  }

}
