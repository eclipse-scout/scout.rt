/*******************************************************************************
 * Copyright (c) 2014-2018 BSI Business Systems Integration AG.
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
import java.security.AccessController;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
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
import org.eclipse.scout.rt.platform.util.PathValidator;
import org.eclipse.scout.rt.platform.util.StringUtility;
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

  private static final Set<String> HTTP_METHODS_SUPPORTED_BY_JAVAX_HTTP_SERVLET = new HashSet<>(Arrays.asList(
      "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE"));

  // Remember bean instances to save lookups on each request
  private final HttpServletControl m_httpServletControl;
  private final UiThreadInterruption m_uiThreadInterruption;

  public UiServlet() {
    m_httpServletControl = BEANS.get(HttpServletControl.class);
    m_uiThreadInterruption = BEANS.get(UiThreadInterruption.class);
  }

  protected boolean isHttpMethodSupportedByJavaxHttpServlet(String method) {
    return HTTP_METHODS_SUPPORTED_BY_JAVAX_HTTP_SERVLET.contains(method);
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

  protected Locale getPreferredLocale(HttpServletRequest req) {
    Cookie cookie = CookieUtility.getCookieByName(req, IUiSession.PREFERRED_LOCALE_COOKIE_NAME);
    if (cookie == null) {
      return req.getLocale();
    }
    else {
      return Locale.forLanguageTag(cookie.getValue());
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    // Use methods provided by javax.servlet.http.HttpServlet because HttpServlet#service contains some special behavior depending on the http method.
    if (isHttpMethodSupportedByJavaxHttpServlet(req.getMethod())) {
      // Will delegate to corresponding doX method below (which in turn will delegate to handleRequest).
      // Wrapping the call is done is super.service (from AbstractHttpServlet).
      super.service(req, resp);
    }
    else {
      // Handle any other method too.
      // Manual wrapping required because no super call is made.
      wrap(req, resp, this::handleHttpMethodsNotSupportedByJavaxHttpServlet);
    }
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      // Send 404 instead of 405 (super implementation) if resource has not been found
      sendNotFound(req, resp);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      // Send 404 instead of 405 (super implementation) if resource has not been found
      sendNotFound(req, resp);
    }
  }

  @Override
  protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      // Send 404 instead of 405 (super implementation) if resource has not been found
      sendNotFound(req, resp);
    }
  }

  @Override
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      // Send 404 instead of 405 (super implementation) if resource has not been found
      sendNotFound(req, resp);
    }
  }

  @Override
  protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      super.doHead(req, resp);
    }
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      super.doOptions(req, resp);
    }
  }

  @Override
  protected void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      super.doTrace(req, resp);
    }
  }

  /**
   * Called in {@link #service(HttpServletRequest, HttpServletResponse)} when
   * {@link #isHttpMethodSupportedByJavaxHttpServlet(String)} return <code>false</code>.
   */
  protected void handleHttpMethodsNotSupportedByJavaxHttpServlet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (!handleRequest(req, resp)) {
      resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "HTTP method not supported");
    }
  }

  /**
   * @return <code>true</code> if request was handled, <code>false</code> otherwise.
   */
  protected boolean handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    m_uiThreadInterruption.detectAndClear(this, "handleRequest");
    m_httpServletControl.doDefaults(this, req, resp);
    try {
      return createServletRunContext(req, resp).call(() -> handleRequestInternal(req, resp), DefaultExceptionTranslator.class);
    }
    catch (Exception e) {
      LOG.error("Failed to process HTTP-{} request from UI", req.getMethod(), e);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return true;
    }
  }

  /**
   * @return <code>true</code> if request was handled, <code>false</code> otherwise.
   */
  protected boolean handleRequestInternal(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    if ("GET".equals(req.getMethod())) {
      // To make relative URLs work, we need to make sure the request URL has a trailing '/'.
      // It is not possible to just check for an empty pathInfo because the container returns "/" even if the user has not entered a '/' at the end.
      String path = req.getServletContext().getContextPath() + req.getServletPath();
      if (StringUtility.hasText(path) && req.getRequestURI().endsWith(path)) {
        resp.sendRedirect(req.getRequestURI() + "/");
        return true;
      }
    }

    long start = System.nanoTime();
    try {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Request started");
      }
      if (!PathValidator.isValid(req.getPathInfo())) {
        LOG.info("Request with invalid path detected: '{}'. Parent paths are not allowed by default. To change this behavior replace {}.", req.getPathInfo(), PathValidator.class);
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return true;
      }

      List<IUiServletRequestHandler> handlers = BEANS.all(IUiServletRequestHandler.class);
      for (IUiServletRequestHandler handler : handlers) {
        if (handler.handle(req, resp)) {
          return true;
        }
      }
      return false;
    }
    catch (Exception t) {
      LOG.error("Exception while processing request", t);
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      return true;
    }
    finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug("Request completed in {} ms", StringUtility.formatNanos(System.nanoTime() - start));
      }
    }
  }

  protected void sendNotFound(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // No handler was able to handle the request
    LOG.info("404_NOT_FOUND: {} {}", req.getMethod(), req.getPathInfo());
    resp.sendError(HttpServletResponse.SC_NOT_FOUND);
  }

  public static class CheckSessionCookieSecureFlagProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.cookieSessionValidateSecure";
    }

    @Override
    public String description() {
      return "Specifies if the UI server should ensure a secure cookie configuration of the webapp.\n"
          + "If enabled the application validates that the 'httpOnly' and 'Secure' flags are set in the cookie configuration in the web.xml.\n"
          + "This property should be disabled if no secure connection (https) is used to the client browser (not recommended).\n"
          + "The default value is true.";
    }

    @Override
    public Boolean getDefaultValue() {
      return Boolean.TRUE;
    }
  }
}
