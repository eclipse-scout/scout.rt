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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.eclipse.scout.commons.xmlparser.ScoutXmlParser;
import org.eclipse.scout.jaxws.Activator;
import org.osgi.framework.Bundle;

import com.sun.xml.internal.ws.transport.http.ResourceLoader;

public class BundleProxyResourceLoader implements ResourceLoader {

  private static final Logger LOG = Logger.getLogger("com.sun.xml.ws.server.http");

  private Bundle m_bundle;

  public BundleProxyResourceLoader(Bundle bundle) {
    m_bundle = bundle;
  }

  @Override
  public URL getCatalogFile() throws MalformedURLException {
    return getResource("/WEB-INF/jax-ws-catalog.xml");
  }

  @Override
  public URL getResource(String path) throws MalformedURLException {
    return new P_GetResourceResolver(path, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  @Override
  public Set<String> getResourcePaths(String path) {
    return new P_GetResourcePathsResolver(path, Activator.getDefault().getBundle(), m_bundle).resolve();
  }

  private class P_GetResourcePathsResolver extends AbstractResolver<Set<String>> {

    private String m_path;

    public P_GetResourcePathsResolver(String path, Bundle... bundles) {
      super(bundles);
      m_path = path;
    }

    @Override
    public Set<String> resolve(Bundle bundle) throws ResourceNotFoundException {
      Set<String> paths = new HashSet<String>();

      // Collect resources of bundle and its attached fragmentsl
      Enumeration entries = m_bundle.findEntries(m_path, "*", false);
      if (entries == null || !entries.hasMoreElements()) {
        throw new ResourceNotFoundException();
      }

      while (entries.hasMoreElements()) {
        URL url = (URL) entries.nextElement();
        if (url != null && !url.getPath().matches("^.+/\\..+/$")) { // skip hidden files, e.g. /WEB-INF/wsdl/.svn/
          if (isValidXml(url)) {
            paths.add(url.getPath());
          }
          else {
            LOG.info("Resource '" + url.getPath() + "' skipped for webservice resource as file is corrupt or does not contain valid XML. [bundle=" + m_bundle.getSymbolicName() + "]");
          }
        }
      }
      if (paths.size() > 0) {
        return paths;
      }
      throw new ResourceNotFoundException();
    }

    @Override
    public Set<String> resolveEmpty() {
      return new HashSet<String>();
    }

    private boolean isValidXml(URL url) {
      try {
        // skip files which are not valid XML as JAX-WS fails to start properly otherwise.
        ScoutXmlParser parser = new ScoutXmlParser();
        parser.parse(url);
        return true;
      }
      catch (Exception e) {
        return false;
      }
    }
  }

  private class P_GetResourceResolver extends AbstractResolver<URL> {

    private String m_path;

    public P_GetResourceResolver(String path, Bundle... bundles) {
      super(bundles);
      m_path = path;
    }

    @Override
    public URL resolve(Bundle bundle) throws ResourceNotFoundException {
      // do not use {@link Bundle#bundle.getEntry(String)} as the m_bundle's classloader must be used in order to work for fragments.
      URL url = bundle.getResource(m_path);
      if (url != null) {
        return url;
      }
      throw new ResourceNotFoundException();
    }
  }
}
