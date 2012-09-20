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
 * Browses classes in a bundle.
 * <p>
 * This browser checks for classes in the following order:
 * <ol>
 * <li>/bin/ (workspace bundles)</li>
 * <li>/classes/ (workspace bundles)</li>
 * <li>/target/classes/ (maven workspace bundles)</li>
 * <li>/ (zipped bundles)</li>
 * </ol>
 */
public class BundleBrowser {
  public static final IScoutLogger LOG = ScoutLogManager.getLogger(BundleBrowser.class);

  private final Bundle m_bundle;
  private final String m_packagePath;
  //context as members (performance)
  private HashSet<String> m_set;
  private String m_prefix;
  private int m_prefixLen;
  /**
   * fix: when running in dev mode, all classes are spidered twice, but even worse
   * if the prefix is /bin/ then the enum returns paths such as bin/... without the leading /
   * The double-check verifies this fact.
   */
  private boolean m_doubleCheckPrefix;
  boolean m_includeInnerTypes;
  boolean m_includeSubtree;

  public BundleBrowser(String symbolicName, String packageName) {
    m_bundle = Platform.getBundle(symbolicName);
    String s = packageName;
    if (s != null) {
      s = s.replace('.', '/');
    }
    m_packagePath = s;
  }

  /**
   * @return all found classes in side the bundle
   *         If the bundle is a binary bundle, simply visits all its classes, otherwise visits the /bin, the /classes or
   *         the /target/classes folder
   */
  public String[] getClasses(boolean includeInnerTypes, boolean includeSubtree) {
    m_includeInnerTypes = includeInnerTypes;
    m_includeSubtree = includeSubtree;
    m_set = new HashSet<String>();
    if (m_bundle != null && m_packagePath != null) {
      String path = removeLeadingSlash(m_packagePath);
      m_prefix = "/bin/";
      m_doubleCheckPrefix = true;
      Enumeration<String> en = getResourcesEnumeration(m_bundle, m_prefix + path);
      if (en == null) {
        m_prefix = "/classes/";
        en = getResourcesEnumeration(m_bundle, m_prefix + path);
      }
      if (en == null) {
        m_prefix = "/target/classes/";
        en = getResourcesEnumeration(m_bundle, m_prefix + path);
      }
      if (en == null) {
        m_prefix = "/";
        en = getResourcesEnumeration(m_bundle, m_prefix + path);
      }
      m_prefixLen = m_prefix.length();
      visit(en);
    }
    return m_set.toArray(new String[m_set.size()]);
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

  private void visit(Enumeration<String> en) {
    if (en != null) {
      while (en.hasMoreElements()) {
        String path = en.nextElement();
        if (path.endsWith(".class")) {
          String className;
          if (m_doubleCheckPrefix) {
            if (path.startsWith(m_prefix)) {
              className = path.substring(m_prefixLen, path.length() - 6);
            }
            else {
              className = path.substring(m_prefixLen - 1, path.length() - 6);
            }
          }
          else {
            className = path.substring(m_prefixLen, path.length() - 6);
          }
          if (path.indexOf("$") < 0 || m_includeInnerTypes) {
            className = className.replaceAll("[/]", ".");
            m_set.add(className);
          }
        }
        else if (path.endsWith("/")) {
          if (m_includeSubtree) {
            visit(m_bundle.getEntryPaths(path));
          }
        }
      }
    }
  }

}
