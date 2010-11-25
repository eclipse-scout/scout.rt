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
package org.eclipse.scout.commons.runtime;

import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.osgi.framework.Bundle;

/**
 * Browser classes in a bundle This browser checks in order: 1. / (zipped
 * bundles) 2. bin/ (workspace bundles) 3. classes/ (workspace bundles)
 */
public class BundleBrowser {
  public static final IScoutLogger LOG = ScoutLogManager.getLogger(BundleBrowser.class);

  private Bundle m_bundle;
  private String m_packagePath;

  public BundleBrowser(String symbolicName, String packageName) {
    m_bundle = Platform.getBundle(symbolicName);
    String s = packageName;
    if (s != null) {
      s = s.replace('.', '/');
    }
    m_packagePath = s;
  }

  /**
   * @param includeInnerTypes
   * @param includeSubtree
   * @return all found classes in side the bundle
   *         If the bundle is a binary bundle, simply visits all its classes, otherwise visits the bin/ or the /classes
   *         folder
   */
  @SuppressWarnings("unchecked")
  public String[] getClasses(boolean includeInnerTypes, boolean includeSubtree) {
    HashSet<String> set = new HashSet<String>();
    if (m_bundle != null && m_packagePath != null) {
      String path = removeLeadingSlash(m_packagePath);

      String prefix = "/bin/";
      Enumeration<String> en = getResourcesEnumeration(m_bundle, prefix + path);

      if (en == null) {
        prefix = "/classes/";
        en = getResourcesEnumeration(m_bundle, prefix + path);
      }

      if (en == null) {
        prefix = "/";
        en = getResourcesEnumeration(m_bundle, prefix + path);
      }

      visit(set, prefix, en, includeInnerTypes, includeSubtree);
    }
    return set.toArray(new String[set.size()]);
  }

  private String removeLeadingSlash(String path) {
    if (path.startsWith("/")) {
      return path.substring(1);
    }
    return path;
  }

  /**
   * To recursively get all resources placed at the given location. Thereby, attaching fragments are also looked for
   * resources.
   * 
   * @param bundle
   * @param path
   * @return
   */
  private Enumeration<String> getResourcesEnumeration(Bundle bundle, String path) {
    Set<String> resources = new HashSet<String>();

    Enumeration entries = bundle.findEntries(path, "*", true);
    if (entries == null || !entries.hasMoreElements()) {
      return null;
    }

    while (entries.hasMoreElements()) {
      URL url = (URL) entries.nextElement();
      resources.add(url.getPath());
    }

    return Collections.enumeration(resources);
  }

  private Enumeration convertToStringPaths(Enumeration entries) {
    Set<String> paths = new HashSet<String>();

    if (entries != null) {
      while (entries.hasMoreElements()) {
        URL url = (URL) entries.nextElement();
        paths.add(url.getPath());
      }
    }

    return Collections.enumeration(paths);
  }

  @SuppressWarnings("unchecked")
  private void visit(HashSet<String> set, String prefix, Enumeration<String> en, boolean includeInnerTypes, boolean includeSubtree) {
    if (en != null) {
      while (en.hasMoreElements()) {
        String path = en.nextElement();
        if (path.endsWith(".class")) {
          String className = path.substring(prefix.length(), path.length() - 6);
          if (path.indexOf("$") < 0 || includeInnerTypes) {
            className = className.replaceAll("[/]", ".");
            set.add(className);
          }
        }
        else {
          if (includeSubtree) {
            visit(set, prefix, m_bundle.getEntryPaths(path), includeInnerTypes, includeSubtree);
          }
        }
      }
    }
  }

}
