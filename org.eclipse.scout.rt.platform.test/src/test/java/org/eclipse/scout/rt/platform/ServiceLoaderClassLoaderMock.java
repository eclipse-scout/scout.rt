/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
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
import java.util.ServiceLoader;

/**
 * <h3>{@link ServiceLoaderClassLoaderMock}</h3>
 * <p>
 * Testing utility used to create a {@link ClassLoader} that mocks a {@link ServiceLoader} resource.
 * <p>
 * It does this by emulating a <code>META-INF/services/interface-fully-qualified-name</code> resource file with the
 * desired implementation class name as content.
 * <p>
 * In order to use this {@link ClassLoader} it must be set into context, for example using
 * <code>Thread.currentThread().setContextClassLoader()</code>
 *
 * @since 6.1
 */
public class ServiceLoaderClassLoaderMock extends ClassLoader {
  private final String m_queryName;
  private final byte[] m_responseData;

  /**
   * @param parent
   *          class loader
   * @param serviceInterface
   *          the interface class to be used in subsequent queries to {@link ServiceLoader#load(Class)}
   * @param implementationClass
   *          the implementationClass class to be returned by {@link ServiceLoader#load(Class)}
   */
  public <T> ServiceLoaderClassLoaderMock(ClassLoader parent, Class<T> serviceInterfaceClass, Class<? extends T> implementationClass) {
    super(parent);
    m_queryName = "META-INF/services/" + serviceInterfaceClass.getName();
    m_responseData = implementationClass.getName().getBytes();
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (m_queryName.equals(name)) {
      try {
        URL url = new URL(null, "mock:///" + name, new URLStreamHandler() {
          @Override
          protected URLConnection openConnection(URL u) throws IOException {
            return new URLConnection(u) {
              @Override
              public void connect() throws IOException {
              }

              @Override
              public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(m_responseData);
              }
            };
          }
        });
        return Collections.enumeration(Collections.singleton(url));
      }
      catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return super.getResources(name);
  }
}
