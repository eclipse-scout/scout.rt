/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The {@link ScoutServiceLoader} has a similar behavior as the {@link ServiceLoader} and is able to load services in a
 * OSGi environment.
 */
public final class ScoutServiceLoader {

  private ScoutServiceLoader() {
  }

  public static <T> List<T> loadServices(Class<T> clazz) {
    List<T> services = new ArrayList<T>();
    Iterator<T> it = ServiceLoader.load(clazz).iterator();
    while (it.hasNext()) {
      services.add(it.next());
    }
    return services;
  }
}
