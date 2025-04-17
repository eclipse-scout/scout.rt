/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jetty.ee10.servlet.ErrorHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHandler;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.util.URIUtil;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.URLResourceFactory;
import org.eclipse.scout.rt.app.Application;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ProcessingException;

public class ScoutServletContextHandler extends ServletContextHandler {

  private boolean m_extendedResourceLookup = false;

  public ScoutServletContextHandler() {
  }

  public ScoutServletContextHandler(String contextPath) {
    super(contextPath);
  }

  public ScoutServletContextHandler(int options) {
    super(options);
  }

  public ScoutServletContextHandler(String contextPath, int options) {
    super(contextPath, options);
  }

  public ScoutServletContextHandler(String contextPath, boolean sessions, boolean security) {
    super(contextPath, sessions, security);
  }

  public ScoutServletContextHandler(SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
    super(sessionHandler, securityHandler, servletHandler, errorHandler);
  }

  public ScoutServletContextHandler(String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler) {
    super(contextPath, sessionHandler, securityHandler, servletHandler, errorHandler);
  }

  public ScoutServletContextHandler(String contextPath, SessionHandler sessionHandler, SecurityHandler securityHandler, ServletHandler servletHandler, ErrorHandler errorHandler, int options) {
    super(contextPath, sessionHandler, securityHandler, servletHandler, errorHandler, options);
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
      return new URLResourceFactory().newResource(url);
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
      URLResourceFactory urlResourceFactory = new URLResourceFactory();
      return Collections.list(Application.class.getClassLoader().getResources(path.substring(1)))
          .stream()
          .map(url -> urlResourceFactory.newResource(url).list())
          .flatMap(List::stream)
          .map(e -> path + e)
          .collect(Collectors.toSet());
    }
    catch (IOException e) {
      throw new ProcessingException("Error during getResourcePaths", e);
    }
  }

  /**
   * Optional provider for a custom Jetty {@link SessionHandler} implementation.
   */
  @Override
  protected SessionHandler newSessionHandler() {
    return BEANS.optional(ISessionHandlerProvider.class)
        .orElse(super::newSessionHandler)
        .newSessionHandler();
  }

  @ApplicationScoped
  public interface ISessionHandlerProvider {
    SessionHandler newSessionHandler();
  }
}
