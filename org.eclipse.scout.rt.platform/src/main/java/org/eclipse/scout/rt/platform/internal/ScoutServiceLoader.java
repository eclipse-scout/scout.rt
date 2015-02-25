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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The {@link ScoutServiceLoader} has a similar behavior as the {@link ServiceLoader} and is able to load services in a
 * OSGi environment.
 */
public final class ScoutServiceLoader {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ScoutServiceLoader.class);

  private ScoutServiceLoader() {
  }

  public static <T> List<T> loadServices(Class<T> clazz) {
    // OSGi support
    if (Platform.isOsgiRunning()) {
      return loadServicesOsgi(clazz);
    }
    // pure Java
    List<T> services = new ArrayList<T>();
    Iterator<T> it = ServiceLoader.load(clazz).iterator();
    while (it.hasNext()) {
      services.add(it.next());
    }
    return services;
  }

  /**
   * @return
   */
  private static <T> List<T> loadServicesOsgi(Class<T> clazz) {
    final List<T> services = new LinkedList<T>();
    for (Bundle bundle : FrameworkUtil.getBundle(ScoutServiceLoader.class).getBundleContext().getBundles()) {
      URL servicesUrl = bundle.getResource("META-INF/services/" + clazz.getName());
      if (servicesUrl != null) {
        BufferedReader reader = null;
        try {
          // parse modules
          reader = new BufferedReader(new InputStreamReader(servicesUrl.openStream()));
          String line = reader.readLine();
          while (line != null) {
            try {
              line = line.trim();
              @SuppressWarnings("unchecked")
              Class<T> serviceClazz = (Class<T>) bundle.loadClass(line);
              services.add(serviceClazz.newInstance());

            }
            catch (Exception e) {
              LOG.error(String.format("Could not instanciate service '%s' defined in '%s' of bundle '%s'", line, servicesUrl, bundle.getSymbolicName()), e);
            }
            line = reader.readLine();
          }
        }
        catch (Exception ex) {
          LOG.error(String.format("Could not load modules defined in '%s' of bundle '%s'", servicesUrl, bundle.getSymbolicName()), ex);
        }
        finally {
          if (reader != null) {
            try {
              reader.close();
            }
            catch (IOException e) {
              // void
            }
          }
        }
      }
    }
    return services;
  }
}
