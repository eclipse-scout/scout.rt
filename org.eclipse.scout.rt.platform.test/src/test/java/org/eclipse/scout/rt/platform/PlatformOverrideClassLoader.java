/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;

/**
 * <h3>{@link PlatformOverrideClassLoader}</h3>
 * <p>
 * This {@link ClassLoader} is used to mock a {@link IPlatform} that is used in {@link Platform#get()}.
 * <p>
 * Therefore it emulates a <code>META-INF/services/org.eclipse.scout.rt.platform.IPlatform</code> resource file with the
 * desired class name as content.
 * <p>
 * In order to use this {@link ClassLoader} it must be set into context, for example using
 * <code>Thread.currentThread().setContextClassLoader()</code>
 *
 * @author imo
 */
public class PlatformOverrideClassLoader extends ClassLoader {
  private static final String PLATFORM_SERVICE_NAME = "META-INF/services/" + IPlatform.class.getName();
  private final Class<?> m_platformOverrideClass;

  /**
   * @param parent
   *          class loader
   * @param platformOverrideClass
   *          the {@link IPlatform} class to be used in {@link Platform#get()}
   */
  public PlatformOverrideClassLoader(ClassLoader parent, Class<?> platformOverrideClass) {
    super(parent);
    m_platformOverrideClass = platformOverrideClass;
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (PLATFORM_SERVICE_NAME.equals(name)) {
      try {
        return Collections.enumeration(Collections.singleton(new URL(null, "mock:///" + name, new MockHandler())));
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return super.getResources(name);
  }

  private class MockHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
      return new MockUrlConnection(u);
    }
  }

  private class MockUrlConnection extends URLConnection {
    public MockUrlConnection(URL url) {
      super(url);
    }

    @Override
    public void connect() throws IOException {
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new ByteArrayInputStream(m_platformOverrideClass.getName().getBytes());
    }
  }

}
