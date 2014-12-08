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
package org.eclipse.scout.rt.ui.html;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheControlInDevelopment;
import org.eclipse.scout.rt.ui.html.cache.HttpCacheControlInProduction;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.service.SERVICES;

/**
 * Instances of this class must be registered as servlet root path "/"
 * <p>
 * The index.html is served as "/" or "/index.html" using HTTP GET
 * <p>
 * Ajax requests are processed as "/json" using HTTP POST
 */
public abstract class AbstractScoutAppServlet extends HttpServletEx {
  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractScoutAppServlet.class);

  private P_AbstractInterceptor m_interceptGet = new P_AbstractInterceptor("GET") {

    @Override
    protected boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return interceptor.interceptGet(AbstractScoutAppServlet.this, req, resp);
    }
  };

  private P_AbstractInterceptor m_interceptPost = new P_AbstractInterceptor("POST") {

    @Override
    boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return interceptor.interceptPost(AbstractScoutAppServlet.this, req, resp);
    }
  };

  private final IWebArchiveResourceLocator m_resourceLocator;
  private final IHttpCacheControl m_httpCacheControl;

  protected AbstractScoutAppServlet() {
    m_resourceLocator = createResourceLocator();
    m_httpCacheControl = createHttpCacheControl();
  }

  protected IWebArchiveResourceLocator createResourceLocator() {
    //TODO imo change once we switch from OSGI to JEE; move WebContent to src/main/resources/META-INF/resources/WebContent, move src/main/js to src/main/resources/META-INF/resources/js
    return new OsgiWebArchiveResourceLocator();
  }

  public IWebArchiveResourceLocator getResourceLocator() {
    return m_resourceLocator;
  }

  protected IHttpCacheControl createHttpCacheControl() {
    return Platform.inDevelopmentMode() ? new HttpCacheControlInDevelopment() : new HttpCacheControlInProduction();
  }

  public IHttpCacheControl getHttpCacheControl() {
    return m_httpCacheControl;
  }

  /**
   * @return a new uninitialized JsonSession
   */
  public abstract IJsonSession createJsonSession();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("GET request started: " + req.getRequestURI());

    // The servlet is registered at '/'. To make relative URLs work, we need to make sure the request URL has a trailing '/'.
    // It is not possible to just check for an empty pathInfo because the container returns "/" even if the user has not entered a '/' at the end.
    String contextPath = getServletContext().getContextPath();
    if (StringUtility.hasText(contextPath) && req.getRequestURI().endsWith(contextPath)) {
      resp.sendRedirect(req.getRequestURI() + "/");
      return;
    }

    try {
      m_interceptGet.intercept(req, resp);
    }
    catch (Exception ex) {
      LOG.error("GET " + req.getRequestURI(), ex);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.info("POST request started: " + req.getRequestURI());
    try {
      m_interceptPost.intercept(req, resp);
    }
    catch (Exception ex) {
      LOG.error("POST " + req.getRequestURI(), ex);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Template pattern.
   */
  protected abstract class P_AbstractInterceptor {

    private final String m_requestType;

    protected P_AbstractInterceptor(String requestType) {
      m_requestType = requestType;
    }

    protected void intercept(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      try {
        IServletRequestInterceptor[] interceptors = SERVICES.getServices(IServletRequestInterceptor.class);
        for (IServletRequestInterceptor interceptor : interceptors) {
          if (intercept(interceptor, req, resp)) {
            return;
          }
        }
      }
      catch (Exception t) {
        LOG.error("Exception while processing " + m_requestType + " request: " + req.getRequestURI(), t);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      finally {
        LOG.info(m_requestType + " request finished: " + req.getRequestURI());
      }
    }

    protected abstract boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
  }
}
