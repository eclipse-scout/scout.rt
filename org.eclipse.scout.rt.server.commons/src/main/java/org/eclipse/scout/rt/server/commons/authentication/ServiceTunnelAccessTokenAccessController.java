/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.security.SimplePrincipalProducer;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.shared.servicetunnel.http.DefaultAuthToken;
import org.eclipse.scout.rt.shared.servicetunnel.http.HttpServiceTunnel;

/**
 * Access controller to continue filter-chain if a valid {@link AbstractHttpServiceTunnel#TOKEN_AUTH_HTTP_HEADER Service
 * Tunnel Token} is provided with the request.
 * <p>
 * By design: The {@link Principal} for authenticated users is not put onto {@link HttpSession}, so that every tunnel
 * request is authenticated.
 *
 * @since 5.1
 */
public class ServiceTunnelAccessTokenAccessController implements IAccessController {

  private ServiceTunnelAccessTokenAuthConfig m_config;

  public ServiceTunnelAccessTokenAccessController init() throws ServletException {
    init(new ServiceTunnelAccessTokenAuthConfig());
    return this;
  }

  public ServiceTunnelAccessTokenAccessController init(final ServiceTunnelAccessTokenAuthConfig config) {
    m_config = config;
    return this;
  }

  @Override
  public boolean handle(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    final String tokenString = request.getHeader(HttpServiceTunnel.TOKEN_AUTH_HTTP_HEADER);
    if (tokenString == null) {
      return false;
    }

    final DefaultAuthToken token = BEANS.get(DefaultAuthToken.class);
    if (!token.parse(tokenString)) {
      return false;
    }

    // check subject
    if (!StringUtility.hasText(token.getUserId())) {
      fail(response);
      return true;
    }

    // check TTL
    if (System.currentTimeMillis() > token.getValidUntil()) {
      fail(response);
      return true;
    }

    // check signature
    if (!token.isValid()) {
      fail(response);
      return true;
    }

    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    final Principal principal = m_config.getPrincipalProducer().produce(token.getUserId());

    // By design: do not cache principal on session. Otherwise, TrivialAccessController would skip this access controller for subsequent requests.
    helper.continueChainAsSubject(principal, request, response, chain);
    return true;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  protected void fail(final HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  /**
   * Configuration for {@link ServiceTunnelAccessTokenAccessController}.
   */
  public static class ServiceTunnelAccessTokenAuthConfig {

    private boolean m_enabled = DefaultAuthToken.isEnabled();
    private IPrincipalProducer m_principalProducer = BEANS.get(SimplePrincipalProducer.class);

    public boolean isEnabled() {
      return m_enabled;
    }

    public ServiceTunnelAccessTokenAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    public ServiceTunnelAccessTokenAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }
  }
}
