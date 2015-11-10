/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.server.commons.authentication.IAuthenticator;
import org.eclipse.scout.rt.server.commons.authentication.IPrincipalProducer;

/**
 * Checks whether the user is already authenticated, or if the request has valid auth info, or if the request path is
 * excluded from authentication checks.
 * <p>
 * <p>
 * If a subject is already set as {@link Subject#getSubject(java.security.AccessControlContext)} then the filter is
 * transparent. Otherwise {@link HttpServletRequest#getRemoteUser()} or {@link HttpServletRequest#getUserPrincipal()} is
 * checked.
 * <p>
 * <h2>init-params</h2>
 * <ul>
 * <li>filter-exclude: resource paths (with wildcard '*') that are excluded from this filter, comma, newline or
 * whitespace separated</li>
 * </ul>
 * <p>
 * POST requests with json message are responded with a json timeout message
 * <p>
 * access to path "" is redirected using "/"
 *
 * @since 5.0
 */
@Bean
public class TrivialAuthenticator implements IAuthenticator {

  private TrivialAuthConfig m_config;

  public void init(final TrivialAuthConfig config) throws ServletException {
    m_config = config;
  }

  @Override
  public boolean handle(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    // within subject?
    if (BEANS.get(ServletFilterHelper.class).isRunningWithinSubject(req)) {
      chain.doFilter(req, resp);
      return true;
    }

    // already authenticated?
    final Principal principal = BEANS.get(ServletFilterHelper.class).findPrincipal(req, m_config.getPrincipalProducer());
    if (principal != null) {
      BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, req, resp, chain);
      return true;
    }

    // excluded path
    if (m_config.getPathInfoFilter().accepts(StringUtility.emptyIfNull(req.getServletPath()) + StringUtility.emptyIfNull(req.getPathInfo()))) {
      chain.doFilter(req, resp);
      return true;
    }

    // this is a copy from UiServlet.doGet
    final String contextPath = req.getServletContext().getContextPath();
    if (StringUtility.hasText(contextPath) && req.getRequestURI().endsWith(contextPath)) {
      resp.sendRedirect(req.getRequestURI() + "/");
      return true;
    }

    return false;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * Configuration for {@link TrivialAuthenticator}.
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
