/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Authenticator for Form-based authentication. This authenticator is designed to work with 'scout-login-module.js' with
 * a 'login.html' and 'logout.html' HTML page in place.
 * <p>
 * This authentication method works as following:
 * <ol>
 * <li>The user enters credentials in 'login.html'</li>
 * <li>The credentials are sent to '/auth' URL to start authentication</li>
 * <li>The credentials are verified by the configured {@link ICredentialVerifier}</li>
 * <li>On success, the user's principal is put onto HTTP session, but the filter chain not continued. In turn, login.js
 * reloads the requested page or redirects to the entry point</li>
 * </ol>
 *
 * @see scout-login-module.js
 * @see login.js
 * @see logout.js
 * @since 5.2
 */
@Bean
public class FormAuthenticator implements IAuthenticator {

  protected FormAuthConfig m_config;

  /**
   * Invoke to initialize this authenticator.
   */
  public void init(final FormAuthConfig config) {
    m_config = config;
    Assertions.assertNotNull(m_config.getCredentialVerifier(), "CredentialVerifier must not be null");
    Assertions.assertNotNull(m_config.getPrincipalProducer(), "PrincipalProducer must not be null");
    Assertions.assertNotNull(m_config.getForbiddenHandler(), "ForbiddenHandler must not be null");
  }

  @Override
  public boolean handle(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    switch (getTarget(req)) {
      case "/login":
        return handleLoginRequest(req, resp);
      case "/auth":
        return handleAuthRequest(req, resp);
      case "/logout":
        return handleLogoutRequest(req, resp);
      default:
        return false;
    }
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * Invoke to handle a login request targeted to '/login' URL. The default implementation forward to '/login.html' page
   * so that the user can enter username and password.
   */
  protected boolean handleLoginRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
    if (!m_config.isHandleLoginRequest()) {
      return false;
    }

    forwardToLoginForm(req, resp);
    return true;
  }

  /**
   * Invoke to handle a logout request targeted to '/logout' URL. The default implementation invalidates HTTP session
   * and forwards to '/logout.html' page.
   */
  protected boolean handleLogoutRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
    if (!m_config.isHandleLogoutRequest()) {
      return false;
    }

    BEANS.get(ServletFilterHelper.class).doLogout(req);
    BEANS.get(ServletFilterHelper.class).forwardToLogoutForm(req, resp);
    return true;
  }

  /**
   * Method invoked to handle an authentication request targeted to '/auth' URL. The default implementation verifies
   * credentials sent in HTTP headers or request parameters, and upon successful authentication puts the principal onto
   * HTTP session. However, the filter chain is not continued, meaning that reload or redirection is to be done by
   * 'login.js'.
   */
  protected boolean handleAuthRequest(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
    final Entry<String, char[]> credentials = readCredentials(req);
    if (credentials == null) {
      m_config.getForbiddenHandler().onForbidden(resp, ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED);
      return true;
    }

    // Never cache authentication requests.
    resp.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
    resp.setDateHeader("Expires", 0); // prevents caching at the proxy server

    final int status = m_config.getCredentialVerifier().verify(credentials.getKey(), credentials.getValue());
    if (status != ICredentialVerifier.AUTH_OK) {
      m_config.getForbiddenHandler().onForbidden(resp, status);
      return true;
    }

    // OWASP: force a new HTTP session to be created.
    final HttpSession session = req.getSession(false);
    if (session != null) {
      session.invalidate();
    }

    // Put authenticated Subject onto HTTP session.
    final Principal principal = m_config.getPrincipalProducer().produce(credentials.getKey());
    BEANS.get(ServletFilterHelper.class).putPrincipalOnSession(req, principal);
    return true;
  }

  /**
   * Reads the credentials from the request, and expects the credentials to be included in HTTP headers in the BASIC
   * Authentication format, or in the request parameters 'user' and 'password'.
   *
   * @return
   */
  protected Entry<String, char[]> readCredentials(final HttpServletRequest req) {
    String user;
    String password;

    final String[] basicCredentials = BEANS.get(ServletFilterHelper.class).parseBasicAuthRequest(req);
    if (basicCredentials != null && basicCredentials.length == 2) {
      user = basicCredentials[0];
      password = basicCredentials[1];
    }
    else {
      user = req.getParameter("user");
      password = req.getParameter("password");
    }

    if (user == null || password == null) {
      return null;
    }

    return new SimpleEntry<>(user, password.toCharArray());
  }

  protected String getTarget(final HttpServletRequest req) {
    final String pathInfo = req.getPathInfo();
    if (pathInfo != null) {
      return pathInfo;
    }

    final String requestURI = req.getRequestURI();
    return requestURI.substring(requestURI.lastIndexOf("/"));
  }

  /**
   * Invoke to forward to '/login.html' page so that the user can enter his credentials.
   */
  public void forwardToLoginForm(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, ServletException {
    BEANS.get(ServletFilterHelper.class).forwardToLoginForm(req, resp);
  }

  /**
   * Handler invoked upon authentication failed.
   */
  public static class ForbiddenHandler {

    /**
     * Method invoked upon authentication failed. The default implementation sets HTTP status code 403 and waits 500ms.
     */
    public void onForbidden(final HttpServletResponse resp, final int verifierAuthStatus) throws IOException {
      // Wait some time to address brute-force attacks.
      try {
        Thread.sleep(500L);
      }
      catch (final InterruptedException e) {
        // NOOP
      }
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  /**
   * Configuration for {@link FormAuthenticator}.
   */
  public static class FormAuthConfig {

    private boolean m_enabled = true;
    private boolean m_handleLoginRequest = true;
    private boolean m_handleLogoutRequest = true;
    private ICredentialVerifier m_credentialVerifier;
    private IPrincipalProducer m_principalProducer = BEANS.get(SimplePrincipalProducer.class);
    private ForbiddenHandler m_forbiddenHandler = new ForbiddenHandler();

    public boolean isEnabled() {
      return m_enabled;
    }

    public FormAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public boolean isHandleLoginRequest() {
      return m_handleLoginRequest;
    }

    public FormAuthConfig withHandleLoginRequest(final boolean handleLoginRequest) {
      m_handleLoginRequest = handleLoginRequest;
      return this;
    }

    public boolean isHandleLogoutRequest() {
      return m_handleLogoutRequest;
    }

    public FormAuthConfig withHandleLogoutRequest(final boolean handleLogoutRequest) {
      m_handleLogoutRequest = handleLogoutRequest;
      return this;
    }

    public ICredentialVerifier getCredentialVerifier() {
      return m_credentialVerifier;
    }

    /**
     * Sets the {@link ICredentialVerifier} to verify user's credentials.
     */
    public FormAuthConfig withCredentialVerifier(final ICredentialVerifier credentialVerifier) {
      m_credentialVerifier = credentialVerifier;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    /**
     * Sets the {@link IPrincipalProducer} to produce a principal for authenticated users.
     */
    public FormAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }

    public ForbiddenHandler getForbiddenHandler() {
      return m_forbiddenHandler;
    }

    public FormAuthConfig withForbiddenHandler(final ForbiddenHandler forbiddenHandler) {
      m_forbiddenHandler = forbiddenHandler;
      return this;
    }
  }
}
