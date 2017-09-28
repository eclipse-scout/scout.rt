/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.security.ConfigFileCredentialVerifier;
import org.eclipse.scout.rt.platform.security.ICredentialVerifier;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;

/**
 * Authenticates a request using <a href="https://tools.ietf.org/html/rfc7617">HTTP Basic Authentication</a>.
 * <h2>IMPORTANT</h2>
 * <ul>
 * <li>This access controller is indented to be used for technical clients. Do not use basic authentication for
 * front-end applications. Because the credentials must be sent along with each request, the user agent must cache them,
 * which could be a security risk if the client's environment can't be fully trusted. Another unwanted consequence is
 * that the user agent application (e.g. a web browser) must be terminated to "log out", as there is no other standard
 * mechanism to clear the cached credentials.
 * <li>Credentials are transferred <i>unencrypted</i> with each request. Is is therefore crucial to use a secure
 * transport layer such as HTTPS!
 * <li>This access controller does <i>not</i> create or invalidate any HTTP sessions.
 * </ul>
 *
 * @since 6.1
 */
@Bean
public class HttpBasicAuthAccessController implements IAccessController {

  protected HttpBasicAuthConfig m_config;

  public HttpBasicAuthAccessController init() {
    return init(new HttpBasicAuthConfig());
  }

  public HttpBasicAuthAccessController init(final HttpBasicAuthConfig config) {
    m_config = config;
    Assertions.assertNotNull(m_config.getCredentialVerifier(), "CredentialVerifier must not be null");
    return this;
  }

  @Override
  public boolean handle(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }
    return handleInternal(request, response, chain);
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * If <code>"Authorization"</code> header is not set, <code>"WWW-Authenticate: Basic"</code> header is returned with
   * status code 401 ({@link HttpServletResponse#SC_UNAUTHORIZED}).
   * <p>
   * Otherwise, the given credentials are checked using the configured credential verifier. If the credentials are
   * invalid, status code 403 ({@link HttpServletResponse#SC_FORBIDDEN}) is returned. Otherwise, the filter chain
   * continues. If a principal producer is configured, the chain continues on behalf of a subject.
   */
  protected boolean handleInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    // Never cache authentication requests.
    response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // prevents caching at the proxy server

    final Pair<String, char[]> credentials = readCredentials(request);
    if (credentials == null) {
      handleForbidden(ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED, response);
      return true;
    }

    final int status = m_config.getCredentialVerifier().verify(credentials.getLeft(), credentials.getRight());
    if (status != ICredentialVerifier.AUTH_OK) {
      handleForbidden(status, response);
      return true;
    }

    if (m_config.getPrincipalProducer() != null) {
      final Principal principal = m_config.getPrincipalProducer().produce(credentials.getLeft());
      BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, request, response, chain);
    }
    else {
      chain.doFilter(request, response);
    }
    return true;
  }

  /**
   * Method invoked if the user could not be verified, or the user's credential was wrong.
   */
  protected void handleForbidden(final int status, final HttpServletResponse resp) throws IOException {
    if (status == ICredentialVerifier.AUTH_CREDENTIALS_REQUIRED) {
      resp.addHeader(ServletFilterHelper.HTTP_HEADER_WWW_AUTHENTICATE, ServletFilterHelper.HTTP_BASIC_AUTH_NAME);
      resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
    else {
      if (m_config.getStatus403WaitMillis() > 0L) {
        SleepUtil.sleepSafe(m_config.getStatus403WaitMillis(), TimeUnit.MILLISECONDS);
      }
      resp.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }

  /**
   * Reads the credentials from the request's {@link ServletFilterHelper#HTTP_HEADER_AUTHORIZATION} headers.
   */
  protected Pair<String, char[]> readCredentials(final HttpServletRequest req) {
    final String[] basicCredentials = BEANS.get(ServletFilterHelper.class).parseBasicAuthRequest(req);
    if (basicCredentials == null || basicCredentials.length != 2) {
      return null;
    }

    final String user = basicCredentials[0];
    if (StringUtility.isNullOrEmpty(user)) {
      return null;
    }

    final String password = basicCredentials[1];
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
   * Configuration for {@link HttpBasicAuthAccessController}.
   */
  public static class HttpBasicAuthConfig {

    private boolean m_enabled = true;
    private ICredentialVerifier m_credentialVerifier = BEANS.get(ConfigFileCredentialVerifier.class);
    private IPrincipalProducer m_principalProducer = null;
    private long m_status403WaitMillis = 500L;

    public boolean isEnabled() {
      return m_enabled;
    }

    public HttpBasicAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public ICredentialVerifier getCredentialVerifier() {
      return m_credentialVerifier;
    }

    /**
     * Sets the {@link ICredentialVerifier} to verify user's credentials.
     */
    public HttpBasicAuthConfig withCredentialVerifier(final ICredentialVerifier credentialVerifier) {
      m_credentialVerifier = credentialVerifier;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    /**
     * Sets the {@link IPrincipalProducer} to produce a {@link Principal} for authenticated users. By default, no
     * principal is produced.
     */
    public HttpBasicAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
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
    public HttpBasicAuthConfig withStatus403WaitMillis(final long waitMillis) {
      m_status403WaitMillis = waitMillis;
      return this;
    }
  }
}
