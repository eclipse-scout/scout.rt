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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.server.commons.context.ServletRunContexts;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestInterceptor;
import org.eclipse.scout.rt.ui.html.res.ResourceRequestInterceptor;

/**
 * Instances of this class must be registered as servlet root path "/"
 * <p>
 * The index.html is served as "/" or "/index.html" using HTTP GET, see {@link ResourceRequestInterceptor}
 * <p>
 * Scripts js and css are served using HTTP GET, see {@link ResourceRequestInterceptor}
 * <p>
 * Images and fonts are served using HTTP GET, see {@link ResourceRequestInterceptor}
 * <p>
 * Ajax requests are processed as "/json" using HTTP POST, see {@link JsonMessageRequestInterceptor}
 */
public class UiServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(UiServlet.class);

  private final P_AbstractRequestHandler m_requestHandlerGet;
  private final P_AbstractRequestHandler m_requestHandlerPost;

  public UiServlet() {
    m_requestHandlerGet = createRequestHandlerGet();
    m_requestHandlerPost = createRequestHandlerPost();
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
      ServletRunContexts.empty().withServletRequest(req).withServletResponse(resp).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          m_requestHandlerGet.handleRequest(req, resp);
        }
      }, BEANS.get(ExceptionTranslator.class));
    }
    catch (Exception e) {
      LOG.error("Failed to process HTTP-GET request from UI", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    try {
      ServletRunContexts.empty().withServletRequest(req).withServletResponse(resp).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          m_requestHandlerPost.handleRequest(req, resp);
        }
      }, BEANS.get(ExceptionTranslator.class));
    }
    catch (Exception e) {
      LOG.error("Failed to process HTTP-POST request from UI", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Template pattern.
   */
  protected abstract class P_AbstractRequestHandler implements Serializable {
    private static final long serialVersionUID = 1L;

    protected P_AbstractRequestHandler() {
    }

    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      long start = System.nanoTime();
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug("started");
        }
        List<IServletRequestInterceptor> interceptors = BEANS.all(IServletRequestInterceptor.class);
        for (IServletRequestInterceptor interceptor : interceptors) {
          if (intercept(interceptor, req, resp)) {
            return;
          }
        }
        // No interceptor was able to handle the request
        LOG.info("404_NOT_FOUND_POST: " + req.getPathInfo());
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      catch (Exception t) {
        LOG.error("Exception while processing", t);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      finally {
        if (LOG.isDebugEnabled()) {
          LOG.debug("completed in " + StringUtility.formatNanos(System.nanoTime() - start) + " ms");
        }
      }
    }

    protected abstract boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
  }

  protected class P_RequestHandlerGet extends P_AbstractRequestHandler {
    private static final long serialVersionUID = 1L;

    protected P_RequestHandlerGet() {
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
    }

    @Override
    protected boolean intercept(IServletRequestInterceptor interceptor, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return interceptor.interceptPost(UiServlet.this, req, resp);
    }
  }
}
