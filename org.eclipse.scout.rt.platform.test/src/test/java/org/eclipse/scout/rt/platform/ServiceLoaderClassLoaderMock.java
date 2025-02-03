/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
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
   *     class loader
   * @param serviceInterface
   *     the interface class to be used in subsequent queries to {@link ServiceLoader#load(Class)}
   * @param implementationClass
   *     the implementationClass class to be returned by {@link ServiceLoader#load(Class)}
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
          protected URLConnection openConnection(URL u) {
            return new URLConnection(u) {
              @Override
              public void connect() {
              }

              @Override
              public InputStream getInputStream() {
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
