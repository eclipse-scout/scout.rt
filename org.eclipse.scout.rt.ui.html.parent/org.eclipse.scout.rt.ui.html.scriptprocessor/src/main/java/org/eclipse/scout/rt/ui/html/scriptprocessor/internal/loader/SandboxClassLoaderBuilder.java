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
package org.eclipse.scout.rt.ui.html.scriptprocessor.internal.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SandboxClassLoaderBuilder {

  private static final int ANY_SIZE = 10240;

  private final Map<String, URL> m_urls = new LinkedHashMap<String, URL>();
  private final JarLocator m_jarLocator;

  public SandboxClassLoaderBuilder() {
    this(null);
  }

  public SandboxClassLoaderBuilder(JarLocator jarLocator) {
    m_jarLocator = (jarLocator != null ? jarLocator : new JarLocator(SandboxClassLoaderBuilder.class));
  }

  public SandboxClassLoaderBuilder addJar(URL url) {
    if (url == null) {
      throw new IllegalArgumentException("Argument 'url' must not be null");
    }
    m_urls.put(url.toExternalForm(), unwrapNestedJar(url));
    return this;
  }

  public SandboxClassLoaderBuilder addLocalJar(String path) {
    URL url = m_jarLocator.getResource(path);
    if (url == null) {
      throw new IllegalStateException("Could not resolve URL for path '" + path + "'");
    }
    m_urls.put(url.toExternalForm(), unwrapNestedJar(url));
    return this;
  }

  public SandboxClassLoaderBuilder addJarContaining(Class<?> clazz) {
    if (clazz == null) {
      throw new IllegalStateException("Argument 'clazz' must not be null");
    }
    URL url = m_jarLocator.getJarContaining(clazz);
    if (url == null) {
      throw new IllegalStateException("Could not resolve URL for class " + clazz.getName());
    }
    m_urls.put(url.toExternalForm(), unwrapNestedJar(url));
    return this;
  }

  public URLClassLoader build(final ClassLoader parent) {
    return AccessController.doPrivileged(new PrivilegedAction<URLClassLoader>() {
      @Override
      public URLClassLoader run() {
        return new URLClassLoader(m_urls.values().toArray(new URL[0]), parent);
      }
    });
  }

  protected static URL unwrapNestedJar(URL url) {
    try {
      // if (!"file".equals(url.getProtocol())) {
      //create a copy of all jars, to avoid locked files
      if (url.getPath().endsWith(".jar")) {
        //copy to tmp location
        String name = url.getPath();
        int i = name.lastIndexOf('/');
        if (i >= 0) {
          name = name.substring(i + 1);
        }
        File f = File.createTempFile("jar-sandbox-", name);
        f.deleteOnExit();
        try (InputStream in = url.openStream(); FileOutputStream out = new FileOutputStream(f)) {
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
      throw new IllegalArgumentException("JAR " + url + " could not be extracted to temp directory", e);
    }
  }
}
