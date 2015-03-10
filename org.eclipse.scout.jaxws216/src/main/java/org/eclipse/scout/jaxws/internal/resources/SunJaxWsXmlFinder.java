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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Finds sun-jaxws.xml
 */
public class SunJaxWsXmlFinder {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(SunJaxWsXmlFinder.class);

  /**
   * Searches all WEB-INF folders for sun-jaxws.xml
   */
  public List<URL> findAll() {
    List<URL> configurations = new ArrayList<>();
    try {
      Enumeration<URL> entries = getClass().getClassLoader().getResources("/WEB-INF/sun-jaxws.xml");
      while (entries.hasMoreElements()) {
        URL url = entries.nextElement();
        if (url != null) {
          configurations.add(url);
        }
      }
    }
    catch (IOException e) {
      LOG.error("Unable to search for sun-jaxws.xml files.", e);
    }
    return configurations;
  }
}
