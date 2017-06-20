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
import java.security.AccessController;
import java.util.List;
import java.util.Locale;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.server.commons.servlet.AbstractHttpServlet;
import org.eclipse.scout.rt.server.commons.servlet.CookieUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.server.commons.servlet.logging.ServletDiagnosticsProviderFactory;
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
public class UiServlet extends AbstractHttpServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOG = LoggerFactory.getLogger(UiServlet.class);

  private final P_AbstractRequestHandler m_requestHandlerGet;
  private final P_AbstractRequestHandler m_requestHandlerPost;
  private final HttpServletControl m_httpServletControl;

  public UiServlet() {
    m_requestHandlerGet = createRequestHandlerGet();
    m_requestHandlerPost = createRequestHandlerPost();
    m_httpServletControl = BEANS.get(HttpServletControl.class);
  }

  protected P_AbstractRequestHandler createRequestHandlerGet() {
    return new P_RequestHandlerGet();
  }

  protected P_AbstractRequestHandler createRequestHandlerPost() {
    return new P_RequestHandlerPost();
  }

  protected RunContext createServletRunContext(final HttpServletRequest req, final HttpServletResponse resp) {
    final String cid = req.getHeader(CorrelationId.HTTP_HEADER_NAME);

    return RunContexts.copyCurrent(true)
        .withSubject(Subject.getSubject(AccessController.getContext()))
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST, req)
        .withThreadLocal(IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE, resp)
        .withDiagnostics(BEANS.get(ServletDiagnosticsProviderFactory.class).getProviders(req, resp))
        .withLocale(getPreferredLocale(req))
        .withCorrelationId(cid != null ? cid : BEANS.get(CorrelationId.class).newCorrelationId());
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    checkSessionCookieConfig(config.getServletContext().getSessionCookieConfig());
  }

  @SuppressWarnings("bsiRulesDefinition:htmlInString")
  protected void checkSessionCookieConfig(SessionCookieConfig sessionCookieConfig) throws ServletException {
    if (sessionCookieConfig == null) {
      LOG.warn("Cannot validate the configuration of the session cookie!");
      return;
    }

    boolean checkSessionCookieSecureFlag = CONFIG.getPropertyValue(CheckSessionCookieSecureFlagProperty.class).booleanValue();
    boolean secureFlagOk = !checkSessionCookieSecureFlag || sessionCookieConfig.isSecure();
    boolean isValid = true;
    if (!sessionCookieConfig.isHttpOnly()) {
      LOG.error("'HttpOnly' flag has not been set on session cookie. Enable the flag in your web.xml (<session-config>...<cookie-config>...<http-only>true</http-only>...</cookie-config>...</session-config>)");
      isValid = false;
    }

    if (!secureFlagOk) {
      LOG.error("'Secure' flag has not been set on session cookie. Enable the flag in your web.xml "
          + "(<session-config>...<cookie-config>...<secure>true</secure>...</cookie-config>...</session-config>)"
          + " or disable the 'Secure' flag check using property '{}=false' if no encrypted channel (https) to the end user is used.", BEANS.get(CheckSessionCookieSecureFlagProperty.class).getKey());
      isValid = false;
    }

    if (!isValid) {
      // don't give detailed error message to clients!
      ServletException ex = new ServletException("Internal Server Error. See server log for details.");
      ex.setStackTrace(new StackTraceElement[]{});
      throw ex;
    }
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
  protected abstract static class P_AbstractRequestHandler implements Serializable {
    private static final long serialVersionUID = 1L;

    protected P_AbstractRequestHandler() {
    }

    protected void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
      long start = System.nanoTime();
      try {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Request started");
        }
        List<IUiServletRequestHandler> handlers = BEANS.all(IUiServletRequestHandler.class);
        for (IUiServletRequestHandler handler : handlers) {
          if (delegateRequest(handler, req, resp)) {
            return;
          }
        }
        // No handler was able to handle the request
        LOG.info("404_NOT_FOUND: {} {}", req.getMethod(), req.getPathInfo());
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      }
      catch (Exception t) {
        LOG.error("Exception while processing request", t);
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      }
      finally {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Request completed in {} ms", StringUtility.formatNanos(System.nanoTime() - start));
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

  /**
   * Specifies if the {@link SessionCookieConfig} should be checked for the 'Secure' flag. This flag should be set for
   * encrypted (https) channels to ensure the user agent only sends the cookie over secured channels.<br>
   * Unfortunately it is not possible to detect if the request from the user is using a secured channel.
   * {@link ServletRequest#isSecure()} only detects if the request received by the container is secured. But the
   * container may be behind a proxy which forwards the requests without encryption but the request from the browser to
   * the proxy itself is encrypted.<br>
   * To handle these cases the check is executed by default even if the request is not secure. In those cases where
   * really no encrypted channel to the user agent is used (not recommended) this property should be set to
   * <code>false</code>.
   */
  public static class CheckSessionCookieSecureFlagProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.cookie.session.validate.secure";
    }

    @Override
    protected Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }
}
