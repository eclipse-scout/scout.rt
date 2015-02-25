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
import org.eclipse.scout.rt.platform.IApplication;
import org.eclipse.scout.rt.platform.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * The application loader is responsible to find all {@link IApplication} which are started when the {@link Platform}
 * starts. The are two ways implemented. The pure java way working with {@link ServiceLoader} and the OSGi way parses
 * the service files itselfs and loads the classes with the bundles classloader.
 */
public final class ApplicationLoader {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ApplicationLoader.class);

  private ApplicationLoader() {
  }

  public static List<IApplication> getApplications() {
    // OSGi support
    if (Platform.isOsgiRunning()) {
      return getApplicationsOsgi();
    }
    // pure Java
    List<IApplication> applications = new ArrayList<IApplication>();
    Iterator<IApplication> it = ServiceLoader.load(IApplication.class).iterator();
    while (it.hasNext()) {
      applications.add(it.next());
    }
    return applications;
  }

  /**
   * @return
   */
  private static List<IApplication> getApplicationsOsgi() {
    final List<IApplication> applications = new LinkedList<IApplication>();
    for (Bundle bundle : FrameworkUtil.getBundle(ApplicationLoader.class).getBundleContext().getBundles()) {
      URL applicationServices = bundle.getResource("META-INF/services/" + IApplication.class.getName());
      if (applicationServices != null) {
        BufferedReader reader = null;
        try {
          // parse modules
          reader = new BufferedReader(new InputStreamReader(applicationServices.openStream()));
          String line = reader.readLine();
          while (line != null) {
            try {
              line = line.trim();
              @SuppressWarnings("unchecked")
              Class<? extends IApplication> applicationClazz = (Class<? extends IApplication>) bundle.loadClass(line);
              applications.add(applicationClazz.newInstance());

            }
            catch (Exception e) {
              LOG.error(String.format("Could not instanciate application '%s' defined in '%s' of bundle '%s'", line, applicationServices, bundle.getSymbolicName()), e);
            }
            line = reader.readLine();
          }
        }
        catch (Exception ex) {
          LOG.error(String.format("Could not load applications defined in '%s' of bundle '%s'", applicationServices, bundle.getSymbolicName()), ex);
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
    return applications;
  }
}
