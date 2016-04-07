/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
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
import java.util.Locale;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.authentication.SessionCookieValidator;
import org.eclipse.scout.rt.server.commons.context.ServletRunContext;
import org.eclipse.scout.rt.server.commons.context.ServletRunContexts;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.ui.html.json.JsonMessageRequestHandler;
import org.eclipse.scout.rt.ui.html.res.ResourceRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this class must be registered as global handler for "/*".
 * <p>
 * The index.html is served as "/" or "/index.html" using HTTP GET, see {@link ResourceRequestHandler}.
 * <p>
 * Scripts js and css are served using HTTP GET, see {@link ResourceRequestHandler}.
 * <p>
 * Images and fonts are served using HTTP GET, see {@link ResourceRequestHandler}.
 * <p>
 * Ajax requests are processed as "/json" using HTTP POST, see {@link JsonMessageRequestHandler}.
 */
public class UiServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(UiServlet.class);

  private final P_AbstractRequestHandler m_requestHandlerGet;
  private final P_AbstractRequestHandler m_requestHandlerPost;
  private final HttpServletControl m_httpServletControl;
  private final SessionCookieValidator m_sessionCookieValidator;

  public UiServlet() {
    m_requestHandlerGet = createRequestHandlerGet();
    m_requestHandlerPost = createRequestHandlerPost();
    m_httpServletControl = BEANS.get(HttpServletControl.class);
    m_sessionCookieValidator = BEANS.get(SessionCookieValidator.class);
  }

  protected P_AbstractRequestHandler createRequestHandlerGet() {
    return new P_RequestHandlerGet();
  }

  protected P_AbstractRequestHandler createRequestHandlerPost() {
    return new P_RequestHandlerPost();
  }

  protected ServletRunContext createServletRunContext(final HttpServletRequest req, final HttpServletResponse resp) {
    final String cid = req.getHeader(CorrelationId.HTTP_HEADER_NAME);

    return ServletRunContexts.copyCurrent()
        .withServletRequest(req)
        .withServletResponse(resp)
        .withLocale(getPreferredLocale(req))
        .withCorrelationId(cid != null ? cid : BEANS.get(CorrelationId.class).newCorrelationId());
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    m_httpServletControl.doDefaults(this, req, resp);
    try {
      createServletRunContext(req, resp).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          m_requestHandlerGet.handleRequest(req, resp);
        }
      }, DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      LOG.error("Failed to process HTTP-GET request from UI", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    finally {
      m_sessionCookieValidator.validate(req, resp);
    }
  }

  @Override
  protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    m_httpServletControl.doDefaults(this, req, resp);
    try {
      createServletRunContext(req, resp).run(new IRunnable() {
        @Override
        public void run() throws Exception {
          m_requestHandlerPost.handleRequest(req, resp);
        }
      }, DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      LOG.error("Failed to process HTTP-POST request from UI", e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
    finally {
      m_sessionCookieValidator.validate(req, resp);
    }
  }

  protected Locale getPreferredLocale(HttpServletRequest req) {
    Cookie cookie = CookieUtility.getCookieByName(req, IUiSession.PREFERRED_LOCALE_COOKIE_NAME);
    if (cookie == null) {
      return req.getLocale();
    }
    else {
      return Locale.forLanguageTag(cookie.getValue());
    }
  }

  /**
   * Template pattern.
   */
  protected static abstract class P_AbstractRequestHandler implements Serializable {
    private static final long serialVersionUID = 1L;

    protected P_AbstractRequestHandler() {
    }

    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      long start = System.nanoTime();
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug("request started");
        }
        List<IUiServletRequestHandler> handlers = BEANS.all(IUiServletRequestHandler.class);
        for (IUiServletRequestHandler handler : handlers) {
          if (delegateRequest(handler, req, resp)) {
            return;
          }
        }
        // No handler was able to handle the request
        LOG.info("404_NOT_FOUND_POST: {}", req.getPathInfo());
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      catch (Exception t) {
        LOG.error("Exception while processing request", t);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      finally {
        if (LOG.isDebugEnabled()) {
          LOG.debug("request completed in {} ms", StringUtility.formatNanos(System.nanoTime() - start));
        }
      }
    }

    protected abstract boolean delegateRequest(IUiServletRequestHandler handler, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException;
  }

  protected static class P_RequestHandlerGet extends P_AbstractRequestHandler {
    private static final long serialVersionUID = 1L;

    protected P_RequestHandlerGet() {
    }

    @Override
    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      // The servlet is registered at '/'. To make relative URLs work, we need to make sure the request URL has a trailing '/'.
      // It is not possible to just check for an empty pathInfo because the container returns "/" even if the user has not entered a '/' at the end.
      String contextPath = req.getServletContext().getContextPath();
      if (StringUtility.hasText(contextPath) && req.getRequestURI().endsWith(contextPath)) {
        resp.sendRedirect(req.getRequestURI() + "/");
        return;
      }
      super.handleRequest(req, resp);
    }

    @Override
    protected boolean delegateRequest(IUiServletRequestHandler handler, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return handler.handleGet(req, resp);
    }
  }

  protected static class P_RequestHandlerPost extends P_AbstractRequestHandler {
    private static final long serialVersionUID = 1L;

    protected P_RequestHandlerPost() {
    }

    @Override
    protected boolean delegateRequest(IUiServletRequestHandler handler, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      return handler.handlePost(req, resp);
    }
  }
}
