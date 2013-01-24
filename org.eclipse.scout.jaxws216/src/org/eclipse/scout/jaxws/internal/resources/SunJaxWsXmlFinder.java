/*******************************************************************************
 * Copyright (c) 2011 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Daniel Wiehl (BSI Business Systems Integration AG) - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.jaxws.internal.resources;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.osgi.framework.internal.core.BundleFragment;
import org.eclipse.scout.jaxws.Activator;
import org.osgi.framework.Bundle;

/**
 * Finds sun-jaxws.xml
 */
@SuppressWarnings("restriction")
public class SunJaxWsXmlFinder {

  /**
   * Searches all WEB-INF folders for sun-jaxws.xml
   */
  public List<SunJaxWsXml> findAll() {
    ArrayList<SunJaxWsXml> list = new ArrayList<SunJaxWsXml>();
    for (Bundle bundle : Activator.getDefault().getBundle().getBundleContext().getBundles()) {
      // exclude fragments as their content is searched by their host bundles.
      // Furthermore, fragments do not have a classloader and therefore cannot load classes.
      if (!(bundle instanceof BundleFragment)) {
        list.addAll(getConfigurations(bundle, "/WEB-INF"));
      }
    }

    return list;
  }

  private List<SunJaxWsXml> getConfigurations(Bundle bundle, String path) {
    List<SunJaxWsXml> configurations = new ArrayList<SunJaxWsXml>();

    // do not use {@link Bundle#bundle.getEntryPaths(String)} as the bundle's classloader must be used in order to work for fragments.
    Enumeration entries = bundle.findEntries(path, "sun-jaxws.xml", false);
    if (entries != null && entries.hasMoreElements()) {
      while (entries.hasMoreElements()) {
        URL url = (URL) entries.nextElement();
        if (url != null) {
          configurations.add(new SunJaxWsXml(bundle, url));
        }
      }
    }
    return configurations;
  }
}
