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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.scout.commons.XmlUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

@SuppressWarnings("restriction")
public class ProxyResourceLoader extends com.sun.xml.internal.ws.api.ResourceLoader implements com.sun.xml.internal.ws.transport.http.ResourceLoader {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ProxyResourceLoader.class);

  public ProxyResourceLoader() {
  }

  @Override
  public URL getCatalogFile() throws MalformedURLException {
    return getResource("/WEB-INF/jax-ws-catalog.xml");
  }

  @Override
  public Set<String> getResourcePaths(String paramString) {
    Set<String> paths = new HashSet<>();
    ClassLoader contextClassLoader = getClassLoader();

    try {
      Enumeration<URL> entries = contextClassLoader.getResources(paramString);
      if (entries == null || !entries.hasMoreElements()) {
        return paths;
      }

      while (entries.hasMoreElements()) {
        URL url = entries.nextElement();
        if (url == null) {
          continue;
        }
        // Skip hidden files like /WEB-INF/wsdl/.svn/
        if (url.getPath().matches("^.+/\\..+/$")) {
          continue;
        }
        // Skip files in webservice consumer folder as they might interfere with webservice provider files.
        // E.g. if having a consumer WSDL file whose service or port is the very same as from a provider to be published. See @{link EndpointFactory#findPrimary(List)}.
        if (url.getPath().endsWith("/consumer/")) {
          continue;
        }

        if (url.getPath().endsWith("/")) { // to support sub-folders. In turn, JAX-WS recursively browses those folders in @{link DeploymentDescriptorParser#collectDocs(String)} by calling this resolver anew.
          paths.add(url.getPath());
        }
        else if (isValidXml(url)) {
          paths.add(url.getPath());
        }
        else {
          LOG.info("Resource '" + url.getPath() + "' skipped for webservice resource as file is corrupt or does not contain valid XML.");
        }
      }
    }
    catch (IOException e) {
      LOG.error("Unable to get resources.", e);
      return paths;
    }
    return paths;
  }

  @Override
  public URL getResource(String paramString) throws MalformedURLException {
    return getClassLoader().getResource(paramString);
  }

  private ClassLoader getClassLoader() {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      return contextClassLoader;
    }
    return getClass().getClassLoader();
  }

  private boolean isValidXml(URL url) {
    try {
      XmlUtility.getXmlDocument(url);
      return true;
    }
    catch (ProcessingException e1) {
      return false;
    }
  }
}
