/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
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

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer2;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.authentication.DefaultAuthToken;
import org.eclipse.scout.rt.shared.authentication.DefaultAuthTokenPrincipalProducer;
import org.eclipse.scout.rt.shared.authentication.DefaultAuthTokenVerifier;

/**
 * Access controller to continue filter-chain if a valid {@link DefaultAuthToken#HTTP_HEADER_NAME} access token is provided with the request.
 * <p>
 * By design: The {@link Principal} for authenticated users is not put onto {@link HttpSession}, so that every tunnel request is authenticated.
 *
 * @since 5.1
 */
public class AccessTokenAccessController implements IAccessController {

  private AccessTokenAuthConfig m_config;
  private boolean m_enabled;

  public AccessTokenAccessController init() {
    init(new AccessTokenAuthConfig());
    return this;
  }

  public AccessTokenAccessController init(AccessTokenAuthConfig config) {
    m_config = config;
    m_enabled = config.isEnabled() && config.getTokenClazz() != null && config.getTokenVerifier() != null && config.getTokenVerifier().isEnabled() && config.getPrincipalProducer2() != null;
    return this;
  }

  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (!m_enabled) {
      return false;
    }

    String tokenString = request.getHeader(DefaultAuthToken.HTTP_HEADER_NAME);
    if (StringUtility.isNullOrEmpty(tokenString)) {
      return false;
    }
    DefaultAuthToken token = BEANS.get(m_config.getTokenClazz()).read(tokenString);
    if (!m_config.getTokenVerifier().verify(token)) {
      fail(response);
      return true;
    }

    Principal principal = m_config.getPrincipalProducer2().produce(token.getUserId(), token.getCustomArgs());

    // By design: do not cache principal on session. Otherwise, TrivialAccessController would skip this access controller for subsequent requests.
    BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, request, response, chain);
    return true;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  public boolean isEnabled() {
    return m_enabled;
  }

  protected void fail(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  /**
   * Configuration for {@link AccessTokenAccessController}.
   */
  public static class AccessTokenAuthConfig {

    private Class<? extends DefaultAuthToken> m_tokenClazz = DefaultAuthToken.class;
    private DefaultAuthTokenVerifier m_tokenVerifier = BEANS.get(DefaultAuthTokenVerifier.class);
    private boolean m_enabled = true;
    private IPrincipalProducer2 m_principalProducer = BEANS.get(DefaultAuthTokenPrincipalProducer.class);

    public Class<? extends DefaultAuthToken> getTokenClazz() {
      return m_tokenClazz;
    }

    public AccessTokenAuthConfig withTokenClazz(Class<? extends DefaultAuthToken> tokenClazz) {
      m_tokenClazz = tokenClazz;
      return this;
    }

    public DefaultAuthTokenVerifier getTokenVerifier() {
      return m_tokenVerifier;
    }

    public AccessTokenAuthConfig withTokenVerifier(DefaultAuthTokenVerifier tokenVerifier) {
      m_tokenVerifier = tokenVerifier;
      return this;
    }

    public boolean isEnabled() {
      return m_enabled;
    }

    public AccessTokenAuthConfig withEnabled(boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    /**
     * Default is {@link DefaultAuthTokenPrincipalProducer}
     *
     * @since 11.0
     */
    public IPrincipalProducer2 getPrincipalProducer2() {
      return m_principalProducer;
    }

    /**
     * Default is {@link DefaultAuthTokenPrincipalProducer}
     *
     * @since 11.0
     */
    public AccessTokenAuthConfig withPrincipalProducer2(IPrincipalProducer2 principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }
  }
}
