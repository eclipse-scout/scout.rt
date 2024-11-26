/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.HandlerContainer;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.scout.rt.app.Application;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

public class ScoutServletContextHandler extends ServletContextHandler {

  private boolean m_extendedResourceLookup = false;

  public ScoutServletContextHandler() {
  }

  public ScoutServletContextHandler(int options) {
    super(options);
  }

  public ScoutServletContextHandler(HandlerContainer parent, String contextPath) {
    super(parent, contextPath);
  }

  public ScoutServletContextHandler(HandlerContainer parent, String contextPath, int options) {
    super(parent, contextPath, options);
  }

  public ScoutServletContextHandler(HandlerContainer parent, String contextPath, boolean sessions, boolean security) {
    super(parent, contextPath, sessions, security);
  }

  public ScoutServletContextHandler(HandlerContainer parent, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
    super(parent, sessionHandler, securityHandler, servletHandler, errorHandler);
  }

  public ScoutServletContextHandler(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
    super(parent, contextPath, sessionHandler, securityHandler, servletHandler, errorHandler);
  }

  public ScoutServletContextHandler(HandlerContainer parent, String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler, int options) {
    super(parent, contextPath, sessionHandler, securityHandler, servletHandler, errorHandler, options);
  }

  public boolean isExtendedResourceLookup() {
    return m_extendedResourceLookup;
  }

  public ScoutServletContextHandler withExtendedResourceLookup(boolean extendedResourceLookup) {
    m_extendedResourceLookup = extendedResourceLookup;
    return this;
  }

  @Override
  public Resource getResource(String pathInContext) throws MalformedURLException {
    Resource resource = super.getResource(pathInContext); // base resource might not be set, if not set returns null
    if (!m_extendedResourceLookup || resource != null) {
      return resource;
    }

    pathInContext = URIUtil.canonicalPath(pathInContext);
    if (pathInContext == null) {
      return null;
    }

    URL url = Application.class.getResource(pathInContext);
    if (url != null) {
      return Resource.newResource(url);
    }

    return null;
  }

  @Override
  public Set<String> getResourcePaths(String path0) {
    if (!m_extendedResourceLookup) {
      return super.getResourcePaths(path0);
    }

    String path = URIUtil.canonicalPath(path0);
    if (path == null) {
      return Collections.emptySet();
    }

    try {
      return Collections.list(Application.class.getClassLoader().getResources(path.substring(1)))
          .stream()
          .map(url -> {
            try (Resource resource = Resource.newResource(url)) {
              return resource.list();
            }
          })
          .flatMap(Arrays::stream)
          .map(e -> path + e)
          .collect(Collectors.toSet());
    }
    catch (IOException e) {
      throw new ProcessingException("Error during getResourcePaths", e);
    }
  }
}
