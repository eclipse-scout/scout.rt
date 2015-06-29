package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.shared.servicetunnel.http.AbstractHttpServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.http.DefaultAuthToken;

/**
 * Security filter detecting service tunnel access token based on
 * {@link AbstractHttpServiceTunnel#TOKEN_AUTH_HTTP_HEADER} and {@link AbstractHttpServiceTunnel#TOKEN_USER_ID_HEADER}
 */
@Bean
public class ServiceTunnelAccessTokenAuthenticator {

  public void init(FilterConfig config) throws ServletException {
  }

  public void destroy() {
  }

  /**
   * @return true if the request was handled (caller returns), false if nothing was done (caller continues)
   */
  public boolean handle(HttpServletRequest req, HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    if (!DefaultAuthToken.isActive()) {
      return false;
    }

    String tokenString = req.getHeader(AbstractHttpServiceTunnel.TOKEN_AUTH_HTTP_HEADER);
    if (tokenString == null) {
      return false;
    }

    DefaultAuthToken token = BEANS.get(DefaultAuthToken.class);
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

    ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    Principal principal = new ServiceTunnelPrincipal(token.getUserId());
    //do NOT cache principal on session
    helper.continueChainAsSubject(principal, req, resp, chain);
    return true;
  }

  protected void fail(HttpServletResponse resp) throws IOException {
    resp.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

}
