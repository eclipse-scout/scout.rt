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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.authentication.token.ITokenPrincipalProducer;
import org.eclipse.scout.rt.server.commons.authentication.token.ITokenVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authenticates a request using <a href="https://tools.ietf.org/html/rfc6750">Bearer Token Usage</a>.
 */
@Bean
public class BearerAuthAccessController implements IAccessController {

  public static final String HTTP_BEARER_AUTH_NAME = "Bearer";
  private static final Logger LOG = LoggerFactory.getLogger(BearerAuthAccessController.class);

  protected HttpBearerAuthConfig m_config;

  public BearerAuthAccessController init(final HttpBearerAuthConfig config) {
    m_config = config;
    Assertions.assertNotNull(m_config.getTokenVerifier(), "TokenVerifier must not be null");
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
   * If <code>"Authorization"</code> header is not set, <code>"WWW-Authenticate: Bearer"</code> header is returned with
   * status code 401 ({@link HttpServletResponse#SC_UNAUTHORIZED}).
   * <p>
   * Otherwise, the given token is checked using the configured token verifier. If the token is invalid, status code 403
   * ({@link HttpServletResponse#SC_FORBIDDEN}) is returned. Otherwise, the filter chain continues. If a principal
   * producer is configured, the chain continues on behalf of a subject.
   */
  protected boolean handleInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    // Never cache authentication requests.
    response.setHeader("Cache-Control", "no-cache"); // HTTP 1.1
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0
    response.setDateHeader("Expires", 0); // prevents caching at the proxy server

    final List<byte[]> bearerTokenParts = readBearerToken(request);
    if (CollectionUtility.isEmpty(bearerTokenParts)) {
      handleForbidden(ITokenVerifier.AUTH_CREDENTIALS_REQUIRED, response);
      return true;
    }

    final int status = m_config.getTokenVerifier().verify(bearerTokenParts);
    if (status != ITokenVerifier.AUTH_OK) {
      handleForbidden(status, response);
      return true;
    }

    if (m_config.getPrincipalProducer() != null) {
      final Principal principal = m_config.getPrincipalProducer().produce(bearerTokenParts);
      BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, request, response, chain);
    }
    else {
      chain.doFilter(request, response);
    }
    return true;
  }

  /**
   * Method invoked if the token could not be verified.
   */
  protected void handleForbidden(final int status, final HttpServletResponse resp) throws IOException {
    if (status == ITokenVerifier.AUTH_CREDENTIALS_REQUIRED) {
      resp.addHeader(ServletFilterHelper.HTTP_HEADER_WWW_AUTHENTICATE, HTTP_BEARER_AUTH_NAME);
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
   * Reads the bearer token from the request's {@link ServletFilterHelper#HTTP_HEADER_AUTHORIZATION} headers.
   *
   * @return The provided token. A bearer token is in most cases base64 encoded, but may contain character like "-",
   *         ".", "_" or "~" which are not part of a base64 token and used to separate different token parts (see
   *         https://datatracker.ietf.org/doc/html/rfc6750#section-2.1)
   */
  protected List<byte[]> readBearerToken(final HttpServletRequest req) {
    final String bearerToken = parseBearerAuthRequest(req);
    if (StringUtility.isNullOrEmpty(bearerToken)) {
      return null;
    }

    String[] encodedBearerTokenParts = StringUtility.split(bearerToken, "[-._~]");

    List<byte[]> tokenParts = new ArrayList<>();
    for (int i = 0; i < encodedBearerTokenParts.length; i++) {
      String encodedPart = encodedBearerTokenParts[i];
      try {
        tokenParts.add(Base64Utility.decode(encodedPart));
      }
      catch (IllegalArgumentException e) {
        LOG.error("Token is not a valid base64 encoded value. Check part {} of the token", i, e);
      }
    }

    return tokenParts;
  }

  /**
   * Parse request authorization header with bearer token and return it
   *
   * @param req
   *          ServletRequest
   * @return token
   */
  public String parseBearerAuthRequest(HttpServletRequest req) {
    String h = req.getHeader(ServletFilterHelper.HTTP_HEADER_AUTHORIZATION);
    if (h == null || !h.startsWith(HTTP_BEARER_AUTH_NAME + " ")) {
      return null;
    }
    return h.substring(HTTP_BEARER_AUTH_NAME.length() + 1);
  }

  /**
   * Configuration for {@link BearerAuthAccessController}.
   */
  public static class HttpBearerAuthConfig {

    private boolean m_enabled = true;
    private ITokenVerifier m_tokenVerifier;
    private ITokenPrincipalProducer m_principalProducer = null;
    private long m_status403WaitMillis = 500L;

    public boolean isEnabled() {
      return m_enabled;
    }

    public HttpBearerAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public ITokenVerifier getTokenVerifier() {
      return m_tokenVerifier;
    }

    /**
     * Sets the {@link ITokenVerifier} to verify the token.
     */
    public HttpBearerAuthConfig withTokenVerifier(final ITokenVerifier tokenVerifier) {
      m_tokenVerifier = tokenVerifier;
      return this;
    }

    public ITokenPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    /**
     * Sets the {@link ITokenPrincipalProducer} to produce a {@link Principal} for authenticated tokens. By default, no
     * principal is produced.
     */
    public HttpBearerAuthConfig withPrincipalProducer(final ITokenPrincipalProducer principalProducer) {
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
    public HttpBearerAuthConfig withStatus403WaitMillis(final long waitMillis) {
      m_status403WaitMillis = waitMillis;
      return this;
    }
  }
}
