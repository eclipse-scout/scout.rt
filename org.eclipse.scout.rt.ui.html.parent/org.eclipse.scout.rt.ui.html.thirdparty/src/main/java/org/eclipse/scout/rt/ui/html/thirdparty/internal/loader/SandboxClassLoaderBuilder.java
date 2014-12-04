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
package org.eclipse.scout.rt.ui.html.thirdparty.internal.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashMap;

import org.osgi.framework.FrameworkUtil;

public final class SandboxClassLoaderBuilder {
  private static final int ANY_SIZE = 10240;
  private final LinkedHashMap<String, URL> m_urls = new LinkedHashMap<String, URL>();
  private final JarLocator m_loc;

  public SandboxClassLoaderBuilder() {
    this(null);
  }

  public SandboxClassLoaderBuilder(JarLocator loc) {
    m_loc = (loc != null ? loc : new JarLocator(SandboxClassLoaderBuilder.class, FrameworkUtil.getBundle(SandboxClassLoaderBuilder.class)));
  }

  public SandboxClassLoaderBuilder addJar(URL url) {
    m_urls.put(url.toExternalForm(), unwrapNestedJar(notNull(url)));
    return this;
  }

  public SandboxClassLoaderBuilder addLocalJar(String path) {
    URL url = m_loc.getResource(path);
    m_urls.put(url.toExternalForm(), unwrapNestedJar(notNull(url)));
    return this;
  }

  public SandboxClassLoaderBuilder addJarContaining(Class<?> clazz) {
    URL url = m_loc.getJarContaining(clazz);
    m_urls.put(url.toExternalForm(), unwrapNestedJar(notNull(url)));
    return this;
  }

  public ClassLoader build(final ClassLoader parent) {
    return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
      @Override
      public URLClassLoader run() {
        return new URLClassLoader(m_urls.values().toArray(new URL[0]), parent);
      }
    });
  }

  protected static URL notNull(URL url) {
    if (url == null) {
      throw new IllegalArgumentException("url is null");
    }
    return url;
  }

  protected static URL unwrapNestedJar(URL url) {
    try {
      if (!"file".equals(url.getProtocol())) {
        //copy to tmp location
        String name = url.getPath();
        int i = name.lastIndexOf('/');
        if (i >= 0) {
          name = name.substring(i + 1);
        }
        File f = File.createTempFile("html-", name);
        try (
            InputStream in = url.openStream();
            FileOutputStream out = new FileOutputStream(f);) {
          byte[] buf = new byte[ANY_SIZE];
          int n;
          while ((n = in.read(buf)) > 0) {
            out.write(buf, 0, n);
          }
        }
        url = f.toURI().toURL();
      }
      return url;
    }
    catch (IOException e) {
      throw new IllegalArgumentException("nexted url " + url + " can not be extracted to temp directory", e);
    }
  }
}
