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
import java.io.Serializable;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.DateUtility;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.IApplication;
import org.eclipse.scout.rt.platform.Platform;
import org.eclipse.scout.rt.server.commons.context.ServletRunContexts;
import org.eclipse.scout.rt.ui.html.cache.DefaultHttpCacheControl;
import org.eclipse.scout.rt.ui.html.cache.IHttpCacheControl;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestInterceptor;
import org.eclipse.scout.rt.ui.html.res.StaticResourceRequestInterceptor;

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
public class UiServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UiServlet.class);

  private final IHttpCacheControl m_httpCacheControl;
  private final P_AbstractRequestHandler m_requestHandlerGet;
  private final P_AbstractRequestHandler m_requestHandlerPost;

  public UiServlet() {
    m_httpCacheControl = createHttpCacheControl();
    m_requestHandlerGet = createRequestHandlerGet();
    m_requestHandlerPost = createRequestHandlerPost();
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    try {
      String appParam = config.getInitParameter("application");
      @SuppressWarnings("unchecked")
      Class<? extends IApplication> appType = (Class<? extends IApplication>) (appParam != null ? Class.forName(appParam) : null);
      Platform.setDefault();
      Platform.get().start(appType);
    }
    catch (Exception e) {
      throw new ServletException(e);
    }
  }

  @Override
  public void destroy() {
    try {
      Platform.get().stop();
    }
    catch (Exception e) {
      LOG.warn("Unable to stop platform.", e);
    }
    super.destroy();
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

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    try {
      ServletRunContexts.empty().servletRequest(req).servletResponse(resp).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          m_requestHandlerGet.handleRequest(req, resp);
        }
      });
    }
    catch (ProcessingException e) {
      LOG.error("Failed to process HTTP-GET request from UI", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    try {
      ServletRunContexts.empty().servletRequest(req).servletResponse(resp).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          m_requestHandlerPost.handleRequest(req, resp);
        }
      });
    }
    catch (ProcessingException e) {
      LOG.error("Failed to process HTTP-POST request from UI", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Template pattern.
   */
  protected abstract class P_AbstractRequestHandler implements Serializable {
    private static final long serialVersionUID = 1L;

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
        List<IServletRequestInterceptor> interceptors = BEANS.all(IServletRequestInterceptor.class);
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
          LOG.debug(m_requestType + " request " + req.getRequestURI() + " completed in " + DateUtility.formatNanos(System.nanoTime() - start) + " ms");
        }
      }
    }

    protected abstract boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
  }

  protected class P_RequestHandlerGet extends P_AbstractRequestHandler {
    private static final long serialVersionUID = 1L;

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
      return interceptor.interceptGet(UiServlet.this, req, resp);
    }
  }

  protected class P_RequestHandlerPost extends P_AbstractRequestHandler {
    private static final long serialVersionUID = 1L;

    protected P_RequestHandlerPost() {
      super("POST");
    }

    @Override
    protected boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return interceptor.interceptPost(UiServlet.this, req, resp);
    }
  }
}
