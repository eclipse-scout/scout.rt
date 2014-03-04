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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;

import org.eclipse.scout.jaxws.Activator;
import org.osgi.framework.Bundle;

public class BundleProxyClassLoader extends ClassLoader {

  private static final Logger LOG = Logger.getLogger("com.sun.xml.ws.server.http");

  private Bundle m_bundle;

  public BundleProxyClassLoader(Bundle bundle) {
    m_bundle = bundle;
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    return new P_FindClassResolver(name, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  @Override
  protected URL findResource(String name) {
    return new P_FindResourceResolver(name, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  @Override
  protected Enumeration<URL> findResources(String name) throws IOException {
    return new P_FindResourcesResolver(name, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    return new P_GetResourcesResolver(name, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  @Override
  public URL getResource(String name) {
    return new P_GetResourceResolver(name, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  @Override
  public InputStream getResourceAsStream(String name) {
    return new P_GetResourceAsStream(name, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  private class P_FindClassResolver extends AbstractResolver<Class<?>> {

    private String m_name;

    public P_FindClassResolver(String name, Bundle... bundles) {
      super(bundles);
      m_name = name;
    }

    @Override
    public Class<?> resolve(Bundle bundle) throws ResourceNotFoundException {
      try {
        return bundle.loadClass(m_name);
      }
      catch (Exception e) {
        throw new ResourceNotFoundException();
      }
    }
  }

  private class P_FindResourceResolver extends AbstractResolver<URL> {

    private String m_name;

    public P_FindResourceResolver(String name, Bundle... bundles) {
      super(bundles);
      m_name = name;
    }

    @Override
    public URL resolve(Bundle bundle) throws ResourceNotFoundException {
      URL url = bundle.getResource(m_name);
      if (url != null) {
        return url;
      }
      throw new ResourceNotFoundException();
    }
  }

  private class P_GetResourceResolver extends AbstractResolver<URL> {

    private String m_name;

    public P_GetResourceResolver(String name, Bundle... bundles) {
      super(bundles);
      m_name = name;
    }

    @Override
    public URL resolve(Bundle bundle) throws ResourceNotFoundException {
      URL url = bundle.getResource(m_name);
      if (url != null) {
        return url;
      }
      throw new ResourceNotFoundException();
    }
  }

  private class P_GetResourcesResolver extends AbstractResolver<Enumeration<URL>> {

    private String m_name;

    public P_GetResourcesResolver(String name, Bundle... bundles) {
      super(bundles);
      m_name = name;
    }

    @Override
    public Enumeration<URL> resolve(Bundle bundle) throws ResourceNotFoundException {
      try {
        @SuppressWarnings("unchecked")
        Enumeration<URL> urls = bundle.getResources(m_name);
        if (urls != null && urls.hasMoreElements()) {
          return urls;
        }
      }
      catch (IOException e) {
      }

      throw new ResourceNotFoundException();
    }

    @Override
    public Enumeration<URL> resolveEmpty() {
      return Collections.enumeration(new ArrayList<URL>(0));
    }
  }

  private class P_FindResourcesResolver extends AbstractResolver<Enumeration<URL>> {

    private String m_name;

    public P_FindResourcesResolver(String name, Bundle... bundles) {
      super(bundles);
      m_name = name;
    }

    @Override
    public Enumeration<URL> resolve(Bundle bundle) throws ResourceNotFoundException {
      try {
        @SuppressWarnings("unchecked")
        Enumeration<URL> urls = bundle.getResources(m_name);
        if (urls != null && urls.hasMoreElements()) {
          return urls;
        }
      }
      catch (Exception e) {
      }

      throw new ResourceNotFoundException();
    }

    @Override
    public Enumeration<URL> resolveEmpty() {
      return Collections.enumeration(new ArrayList<URL>(0));
    }
  }

  private class P_GetResourceAsStream extends AbstractResolver<InputStream> {

    private String m_name;

    public P_GetResourceAsStream(String name, Bundle... bundles) {
      super(bundles);
      m_name = name;
    }

    @Override
    public InputStream resolve(Bundle bundle) throws ResourceNotFoundException {
      URL url = bundle.getResource(m_name);
      if (url != null) {
        try {
          return url.openStream();
        }
        catch (Exception e) {
          // nop
        }
      }
      throw new ResourceNotFoundException();
    }
  }
}
