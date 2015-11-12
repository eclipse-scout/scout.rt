/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;

/**
 * Access controller to fast-check user's access to requested resources.
 * <p>
 * This access controller continues chain if one of the following criteria applies:
 * <ul>
 * <li>if running within a {@link Subject} that contains the principal as set in
 * {@link HttpServletRequest#getRemoteUser()};</li>
 * <li>if {@link HttpServletRequest#getRemoteUser()} or {@link HttpServletRequest#getUserPrincipal()} is set;</li>
 * <li>if a {@link HttpSession} exists with a {@link Principal} set in
 * {@link ServletFilterHelper#SESSION_ATTRIBUTE_FOR_PRINCIPAL};</li>
 * <li>if the request path is subject for exclusion;
 * </ul>
 * Also, any request to "" is redirected to "/".
 *
 * @since 5.1
 */
public class TrivialAccessController implements IAccessController {

  private TrivialAuthConfig m_config;

  public TrivialAccessController init(final TrivialAuthConfig config) throws ServletException {
    m_config = config;
    return this;
  }

  @Override
  public boolean handle(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    // running within a valid subject?
    if (BEANS.get(ServletFilterHelper.class).isRunningWithValidSubject(request)) {
      chain.doFilter(request, response);
      return true;
    }

    // is already authenticated?
    final Principal principal = BEANS.get(ServletFilterHelper.class).findPrincipal(request, m_config.getPrincipalProducer());
    if (principal != null) {
      BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, request, response, chain);
      return true;
    }

    // is excluded path?
    if (m_config.getPathInfoFilter().accepts(StringUtility.emptyIfNull(request.getServletPath()) + StringUtility.emptyIfNull(request.getPathInfo()))) {
      chain.doFilter(request, response);
      return true;
    }

    // this is a copy from UiServlet.doGet
    final String contextPath = request.getServletContext().getContextPath();
    if (StringUtility.hasText(contextPath) && request.getRequestURI().endsWith(contextPath)) {
      response.sendRedirect(request.getRequestURI() + "/");
      return true;
    }

    return false;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * Configuration for {@link TrivialAccessController}.
   */
  public static class TrivialAuthConfig {

    private boolean m_enabled = true;
    private IPrincipalProducer m_principalProducer = BEANS.get(RemoteUserPrincipalProducer.class);
    private PathInfoFilter m_exclusionFilter;

    public boolean isEnabled() {
      return m_enabled;
    }

    public TrivialAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    public TrivialAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }

    public PathInfoFilter getPathInfoFilter() {
      return m_exclusionFilter;
    }

    /**
     * Exclude resources from authentication.
     * <p>
     * Filter format: separate resources by comma, newline or whitespace; usage of wildcard (*) character is supported;
     */
    public TrivialAuthConfig withExclusionFilter(final String exclusionFilter) {
      m_exclusionFilter = new PathInfoFilter(exclusionFilter);
      return this;
    }
  }
}
