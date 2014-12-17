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

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.server.commons.servletfilter.HttpServletEx;
import org.eclipse.scout.rt.ui.html.cache.DefaultHttpCacheControl;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.json.IJsonSession;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestInterceptor;
import org.eclipse.scout.rt.ui.html.res.IWebContentResourceLocator;
import org.eclipse.scout.rt.ui.html.res.OsgiWebContentResourceLocator;
import org.eclipse.scout.rt.ui.html.res.StaticResourceRequestInterceptor;
import org.eclipse.scout.service.SERVICES;

/**
 * Instances of this class must be registered as servlet root path "/"
 * <p>
 * The index.html is served as "/" or "/index.html" using HTTP GET, see {@link StaticResourceRequestInterceptor}
 * <p>
 * Scripts js and css are served using HTTP GET, see {@link StaticResourceRequestInterceptor}
 * <p>
 * Images and fonts are served using HTTP GET, see {@link StaticResourceRequestInterceptor}
 * <p>
 * Ajax requests are processed as "/json" using HTTP POST, see {@link JsonMessageRequestInterceptor}
 */
public abstract class AbstractUiServlet extends HttpServletEx {
  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(AbstractUiServlet.class);

  private final IWebContentResourceLocator m_resourceLocator;
  private final IHttpCacheControl m_httpCacheControl;
  private final P_AbstractRequestHandler m_requestHandlerGet;
  private final P_AbstractRequestHandler m_requestHandlerPost;

  protected AbstractUiServlet() {
    m_resourceLocator = createResourceLocator();
    m_httpCacheControl = createHttpCacheControl();
    m_requestHandlerGet = createRequestHandlerGet();
    m_requestHandlerPost = createRequestHandlerPost();
  }

  protected IWebContentResourceLocator createResourceLocator() {
    //TODO imo change once we switch from OSGI to JEE; move WebContent to src/main/resources/META-INF/resources/WebContent, move src/main/js to src/main/resources/META-INF/resources/js
    return new OsgiWebContentResourceLocator();
  }

  public IWebContentResourceLocator getResourceLocator() {
    return m_resourceLocator;
  }

  protected IHttpCacheControl createHttpCacheControl() {
    return new DefaultHttpCacheControl();
  }

  public IHttpCacheControl getHttpCacheControl() {
    return m_httpCacheControl;
  }

  protected P_AbstractRequestHandler createRequestHandlerGet() {
    return new P_RequestHandlerGet();
  }

  protected P_AbstractRequestHandler createRequestHandlerPost() {
    return new P_RequestHandlerPost();
  }

  /**
   * @return a new uninitialized JsonSession
   */
  public abstract IJsonSession createJsonSession();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    m_requestHandlerGet.handleRequest(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    m_requestHandlerPost.handleRequest(req, resp);
  }

  /**
   * Template pattern.
   */
  protected abstract class P_AbstractRequestHandler {

    private final String m_requestType;

    protected P_AbstractRequestHandler(String requestType) {
      m_requestType = requestType;
    }

    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      long start = System.nanoTime();
      if (LOG.isDebugEnabled()) {
        LOG.debug(m_requestType + " request " + req.getRequestURI() + " started");
      }
      try {
        IServletRequestInterceptor[] interceptors = SERVICES.getServices(IServletRequestInterceptor.class);
        for (IServletRequestInterceptor interceptor : interceptors) {
          if (intercept(interceptor, req, resp)) {
            return;
          }
        }
      }
      catch (Exception t) {
        LOG.error("Exception while processing " + m_requestType + " request " + req.getRequestURI(), t);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      finally {
        if (LOG.isDebugEnabled()) {
          LOG.debug(m_requestType + " request " + req.getRequestURI() + " completed in " + DateUtility.formatNanos(System.nanoTime() - start) + "ms");
        }
      }
    }

    protected abstract boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
  }

  protected class P_RequestHandlerGet extends P_AbstractRequestHandler {

    protected P_RequestHandlerGet() {
      super("GET");
    }

    @Override
    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      // The servlet is registered at '/'. To make relative URLs work, we need to make sure the request URL has a trailing '/'.
      // It is not possible to just check for an empty pathInfo because the container returns "/" even if the user has not entered a '/' at the end.
      String contextPath = getServletContext().getContextPath();
      if (StringUtility.hasText(contextPath) && req.getRequestURI().endsWith(contextPath)) {
        resp.sendRedirect(req.getRequestURI() + "/");
        return;
      }

      UiHints.updateHints(req);

      super.handleRequest(req, resp);
    }

    @Override
    protected boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return interceptor.interceptGet(AbstractUiServlet.this, req, resp);
    }
  }

  protected class P_RequestHandlerPost extends P_AbstractRequestHandler {

    protected P_RequestHandlerPost() {
      super("POST");
    }

    @Override
    protected boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return interceptor.interceptPost(AbstractUiServlet.this, req, resp);
    }
  }
}
