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
package org.eclipse.scout.commons;

import java.io.File;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.internal.Activator;
import org.osgi.framework.BundleContext;

/**
 * Utility to replace ${name} variables in strings. The values are retrieved
 * using {@link BundleContext#getProperty(String)}
 */
public final class BundleContextUtility {
  private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^\\}]+)\\}");

  private BundleContextUtility() {
  }

  /**
   * @return s with all variables ${...} resolved using {@link BundleContext#getProperty(String)} One special variable
   *         is
   *         included: ${workspace_loc} is the workspace location when running
   *         in development mode, and it is only supported in {@link Platform#inDevelopmentMode()}
   */
  public static String resolve(String s) {
    if (s == null || s.length() == 0) {
      return s;
    }
    String t = s;
    Matcher m = VARIABLE_PATTERN.matcher(t);
    while (m.find()) {
      String key = m.group(1);
      String value = null;
      if (key.equals("workspace_loc")) {
        if (!Platform.inDevelopmentMode()) {
          if (value == null) {
            throw new IllegalArgumentException("resolving expression \"" + s + "\": variable ${" + key + "} is not supported in production mode");
          }
        }
        try {
          String dev = new URL(Activator.getDefault().getBundle().getBundleContext().getProperty("osgi.dev")).getFile();
          value = new File(dev.substring(0, dev.indexOf("/.metadata/"))).getAbsolutePath();
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      }
      else {
        value = Activator.getDefault().getBundle().getBundleContext().getProperty(key);
        try {
          value = new File(new URL(value).getFile()).getAbsolutePath();
        }
        catch (Exception e) {
        }
      }
      if (value == null) {
        throw new IllegalArgumentException("resolving expression \"" + s + "\": variable ${" + key + "} is not defined in the bundle context");
      }
      t = t.substring(0, m.start()) + value + t.substring(m.end());
      // next
      m = VARIABLE_PATTERN.matcher(t);
    }
    return t;
  }
}
