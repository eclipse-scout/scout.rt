/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.security.SimplePrincipalProducer;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;

/**
 * Authenticator for Form-based authentication. This authenticator is designed to collaborate with
 * 'scout-login-module.js', and is to be installed in UI server only.
 * <p>
 * User authentication works as following:
 * <ol>
 * <li>The user provides credentials via 'login.html'</li>
 * <li>'scout-login-module.js' sends the credentials to '/auth' URL to be verified by this authenticator's
 * {@link ICredentialVerifier}</li>
 * <li>On successful authentication, the user's principal is put onto HTTP session, so that subsequent requests of that
 * user can be fast authenticated by {@link TrivialAccessController}. However, the filter-chain is not continued.
 * Instead, the JavaScript login module take appropriate actions upon HTTP 200 response code.
 * </ol>
 *
 * @see "login.js"
 * @see "logout.js"
 * @since 5.2
 */
public class FormBasedAccessController implements IAccessController {

  protected FormBasedAuthConfig m_config;

  public FormBasedAccessController init(FormBasedAuthConfig config) {
    m_config = config;
    Assertions.assertNotNull(m_config.getCredentialVerifier(), "CredentialVerifier must not be null");
    Assertions.assertNotNull(m_config.getPrincipalProducer(), "PrincipalProducer must not be null");
    return this;
  }

  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    if ("/auth".equals(request.getPathInfo())) {
      return handleAuthRequest(request, response);
    }

    return false;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * Method invoked to handle an authentication request targeted to '/auth'. The default implementation verifies
   * credentials sent via request parameters, and upon successful authentication puts the principal onto HTTP session.
   * However, the filter chain is not continued, meaning that 'login.js' is responsible to redirect the user to the
   * requested resource.
   *
   * @return <code>true</code> if the request was handled (caller should exit chain), or <code>false</code> if nothing
   *         was done (caller should continue by invoking subsequent authenticators).
   */
  @SuppressWarnings({"squid:RedundantThrowsDeclarationCheck", "DuplicatedCode"}) // required so that overriding subclasses can throw ServletExceptions
  protected boolean handleAuthRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    // Never cache authentication requests.
    response.setHeader("Cache-Control", "private, no-store, no-cache, max-age=0"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // prevents caching at the proxy server

    // requests on /auth should have the default headers as this might also be called using GET which returns a 404 html page (container dependent)
    BEANS.get(HttpServletControl.class).doDefaults(null, request, response);

    Pair<String, char[]> credentials = readCredentials(request);
    if (credentials == null) {
      handleForbidden(ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED, response);
      return true;
    }

    int status = m_config.getCredentialVerifier().verify(credentials.getLeft(), credentials.getRight());
    if (status != ICredentialVerifier.AUTH_OK) {
      handleForbidden(status, response);
      return true;
    }

    // OWASP: force a new HTTP session to be created.
    ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    helper.invalidateSessionAfterLogin(request);

    // Put authenticated principal onto (new) HTTP session
    Principal principal = m_config.getPrincipalProducer().produce(credentials.getLeft());
    helper.putPrincipalOnSession(request, principal);
    return true;
  }

  /**
   * Method invoked if the user could not be verified. The default implementation waits some time to address brute-force
   * attacks, and sets a 403 HTTP status code.
   *
   * @param status
   *          is a {@link ICredentialVerifier} AUTH_* constant
   */
  protected void handleForbidden(int status, HttpServletResponse response) throws IOException {
    if (m_config.getStatus403WaitMillis() > 0L) {
      SleepUtil.sleepSafe(m_config.getStatus403WaitMillis(), TimeUnit.MILLISECONDS);
    }
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  /**
   * Reads the credentials sent by 'login.js' from request parameters.
   */
  protected Pair<String, char[]> readCredentials(HttpServletRequest request) {
    String user = request.getParameter("user");
    if (StringUtility.isNullOrEmpty(user)) {
      return null;
    }

    String password = request.getParameter("password");
    if (StringUtility.isNullOrEmpty(password)) {
      return null;
    }

    // Generally, passwords should be stored as char array, because Strings are immutable and
    // might stay in the memory forever, while char arrays _could_ be overwritten after user:
    // http://stackoverflow.com/questions/8881291/why-is-char-preferred-over-string-for-passwords-in-java
    // However, the password is already a String when using the servlet API, so this technique
    // is basically useless... http://stackoverflow.com/questions/15016250/in-java-how-do-i-extract-a-password-from-a-httpservletrequest-header-without-ge
    // We do it nevertheless to prevent accidental logging of passwords.
    return new ImmutablePair<>(user, password.toCharArray());
  }

  /**
   * Configuration for {@link FormBasedAccessController}.
   */
  public static class FormBasedAuthConfig {

    private boolean m_enabled = true;
    private long m_status403WaitMillis = 500L;
    private ICredentialVerifier m_credentialVerifier;
    private IPrincipalProducer m_principalProducer = BEANS.get(SimplePrincipalProducer.class);

    public boolean isEnabled() {
      return m_enabled;
    }

    public FormBasedAuthConfig withEnabled(boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public ICredentialVerifier getCredentialVerifier() {
      return m_credentialVerifier;
    }

    /**
     * Sets the {@link ICredentialVerifier} to verify user's credentials.
     */
    public FormBasedAuthConfig withCredentialVerifier(ICredentialVerifier credentialVerifier) {
      m_credentialVerifier = credentialVerifier;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    /**
     * Sets the {@link IPrincipalProducer} to produce a {@link Principal} for authenticated users. By default,
     * {@link SimplePrincipalProducer} is used.
     */
    public FormBasedAuthConfig withPrincipalProducer(IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }

    public long getStatus403WaitMillis() {
      return m_status403WaitMillis;
    }

    /**
     * Sets the time to wait to respond with a 403 response code. That is a simple mechanism to address brute-force
     * attacks, but may have a negative effect on DoS attacks. By default, this authenticator waits for 500ms.
     */
    public FormBasedAuthConfig withStatus403WaitMillis(long waitMillis) {
      m_status403WaitMillis = waitMillis;
      return this;
    }
  }
}
