package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.server.commons.authentication.IAuthenticator;
import org.eclipse.scout.rt.server.commons.authentication.IPrincipalProducer;
import org.eclipse.scout.rt.shared.servicetunnel.http.AbstractHttpServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.http.DefaultAuthToken;

/**
 * Security filter verifying service tunnel access token based on
 * {@link AbstractHttpServiceTunnel#TOKEN_AUTH_HTTP_HEADER} and {@link AbstractHttpServiceTunnel#TOKEN_USER_ID_HEADER}.
 */
@Bean
public class ServiceTunnelAccessTokenAuthenticator implements IAuthenticator {

  private ServiceTunnelAccessTokenAuthConfig m_config;

  public void init() throws ServletException {
    init(new ServiceTunnelAccessTokenAuthConfig());
  }

  public void init(final ServiceTunnelAccessTokenAuthConfig config) throws ServletException {
    m_config = config;
  }

  @Override
  public boolean handle(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    final String tokenString = req.getHeader(AbstractHttpServiceTunnel.TOKEN_AUTH_HTTP_HEADER);
    if (tokenString == null) {
      return false;
    }

    final DefaultAuthToken token = BEANS.get(DefaultAuthToken.class);
    if (!token.parse(tokenString)) {
      return false;
    }

    // check subject
    if (!StringUtility.hasText(token.getUserId())) {
      fail(resp);
      return true;
    }

    // check TTL
    if (System.currentTimeMillis() > token.getValidUntil()) {
      fail(resp);
      return true;
    }

    // check signature
    if (!token.isValid()) {
      fail(resp);
      return true;
    }

    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    final Principal principal = m_config.getPrincipalProducer().produce(token.getUserId());

    // by design: do not cache principal on session
    helper.continueChainAsSubject(principal, req, resp, chain);
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
   * Configuration for {@link ServiceTunnelAccessTokenAuthenticator}.
   */
  public static class ServiceTunnelAccessTokenAuthConfig {

    private boolean m_enabled = DefaultAuthToken.isEnabled();
    private IPrincipalProducer m_principalProducer = BEANS.get(ServiceTunnelPrincipalProducer.class);

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
