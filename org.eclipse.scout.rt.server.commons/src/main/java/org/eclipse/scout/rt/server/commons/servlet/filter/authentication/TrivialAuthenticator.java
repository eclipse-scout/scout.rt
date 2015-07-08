/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;

/**
 * Checks whether the user is already logged in or the request has valid auth info or the request path is excluded from
 * auth checks
 * <p>
 * <p>
 * If a subject is already set as {@link Subject#getSubject(java.security.AccessControlContext)} then the filter is
 * transparent.
 * <p>
 * Otherwise {@link HttpServletRequest#getRemoteUser()} or {@link HttpServletRequest#getUserPrincipal()} is checked.
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
public class TrivialAuthenticator {
  protected PathInfoFilter m_excludePathFilter;

  public void init(FilterConfig filterConfig) throws ServletException {
    m_excludePathFilter = new PathInfoFilter(filterConfig.getInitParameter("filter-exclude"));
  }

  public void destroy() {
  }

  /**
   * @return true if the request was handled (caller returns), false if nothing was done (caller continues)
   */
  public boolean handle(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    //within subject?
    if (currentSubjectHasValidPrincipal(req)) {
      chain.doFilter(req, resp);
      return true;
    }

    // already authenticated?
    Principal principal = findPrincipal(req);
    if (principal != null) {
      BEANS.get(ServletFilterHelper.class).continueChainAsSubject(principal, req, resp, chain);
      return true;
    }

    //excluded path
    if (m_excludePathFilter.accepts(req.getPathInfo())) {
      chain.doFilter(req, resp);
      return true;
    }

    // this is a copy from UiServlet.doGet
    String contextPath = req.getServletContext().getContextPath();
    if (StringUtility.hasText(contextPath) && req.getRequestURI().endsWith(contextPath)) {
      resp.sendRedirect(req.getRequestURI() + "/");
      return true;
    }

    return false;
  }

  /**
   * @return true, if a {@link Subject} is already set with a principal corresponding to the given username.
   */
  protected boolean currentSubjectHasValidPrincipal(HttpServletRequest req) {
    String username = req.getRemoteUser();
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject != null) {
      for (Principal p : subject.getPrincipals()) {
        if (StringUtility.hasText(username) && StringUtility.equalsIgnoreCase(p.getName(), username)) {
          return true;
        }
      }
    }
    return false;
  }

  protected Principal findPrincipal(HttpServletRequest req) {
    Principal principal = null;

    // on session cache
    principal = BEANS.get(ServletFilterHelper.class).getPrincipalOnSession(req);
    if (principal != null) {
      return principal;
    }

    // on request as principal
    principal = req.getUserPrincipal();
    if (principal != null && StringUtility.hasText(principal.getName())) {
      return principal;
    }

    // on request as remoteUser
    principal = null;
    String name = req.getRemoteUser();
    if (StringUtility.hasText(name)) {
      return new RemoteUserPrincipal(name);
    }

    return null;
  }

  // FIXME BSH We have to find a better solution for this! See also ServletFilterHelper
  protected void sendSessionTimeout(HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().print("{\"error\":{\"code\":10,\"message\":\"The session has expired, please reload the page.\"}}"); // JsonResponse.ERR_SESSION_TIMEOUT
  }
}
