/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JarLocator {
  private final Class<?> m_loaderClass;

  public JarLocator(Class<?> loaderClass) {
    m_loaderClass = loaderClass;
  }

  public URL getResource(String resourceName) {
    URL url = null;
    //path in binary build
    url = m_loaderClass.getResource("/" + resourceName);
    if (url != null) {
      return url;
    }
    //path in development workspace
    url = m_loaderClass.getResource("/src/main/resources/" + resourceName);
    if (url != null) {
      return url;
    }
    return null;
  }

  public URL getJarContaining(Class<?> clazz) {
    URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
    if (url == null) {
      return url;
    }
    if (url.getPath().endsWith(".jar")) {
      return url;
    }
    //workspace
    try {
      URL fileUrl = new URL(url, "target/classes/");
      File f = new File(fileUrl.getPath());
      if (f.exists()) {
        return fileUrl;
      }
      fileUrl = url;
      f = new File(fileUrl.getPath());
      if (f.exists()) {
        return fileUrl;
      }
    }
    catch (MalformedURLException e) {
      //nop
    }
    return url;
  }
}
