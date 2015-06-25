/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

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
 * <li>path.exclude: resource paths (with wildcard '*') that are excluded from this filter, comma, newline or whitespace
 * separated</li>
 * </ul>
 * <p>
 * POST requests with json message are responded with a json timeout message
 * <p>
 * access to path "" is redirected using "/"
 *
 * @since 5.0
 */
public class TrivialAuthenticator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(TrivialAuthenticator.class);

  public static final String SESSION_ATTRIBUTE_FOR_PRINCIPAL = Principal.class.getName();

  private PathInfoFilter m_excludePathFilter;

  public void init(FilterConfig filterConfig) throws ServletException {
    m_excludePathFilter = new PathInfoFilter(filterConfig.getInitParameter("path.exclude"));
  }

  public void destroy() {
  }

  /**
   * @return true if the request was handled (caller returns), false if nothing was done (caller continues)
   */
  public boolean handle(final ServletRequest in, final ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) in;
    final HttpServletResponse resp = (HttpServletResponse) out;

    //within subject?
    if (currentSubjectHasValidPrincipal(req)) {
      chain.doFilter(in, out);
      return true;
    }

    // already authenticated?
    Principal principal = findPrincipal(req);
    if (principal != null) {
      continueChainAsSubject(principal, req, resp, chain);
      return true;
    }

    //excluded path
    if (m_excludePathFilter.accepts(req.getPathInfo())) {
      chain.doFilter(in, out);
      return true;
    }

    //json post
    if ("POST".equals(req.getMethod())) {
      if (("" + req.getContentType()).startsWith("application/json")) {
        LOG.debug("Returning session timeout error as json.");
        writeSessionTimeout(resp);
        return true;
      }
      else {
        LOG.warn("The request for {0} is a POST request. Forwarding to the login page will most likely fail because StaticResourceRequestInterceptor doesn't handle post.", req.getPathInfo());
        return false;
      }
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

    // on session
    final HttpSession session = req.getSession(false);
    if (session != null) {
      principal = (Principal) session.getAttribute(SESSION_ATTRIBUTE_FOR_PRINCIPAL);
      if (principal != null) {
        return principal;
      }
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

  protected Subject createSubject(Principal principal) {
    // create subject if necessary
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.isReadOnly()) {
      subject = new Subject();
    }
    subject.getPrincipals().add(principal);
    subject.setReadOnly();
    return subject;
  }

  protected void continueChainAsSubject(final Principal principal, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
    try {
      Subject.doAs(
          createSubject(principal),
          new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
              HttpServletRequest secureReq = new SecureHttpServletRequestWrapper(req, principal);
              chain.doFilter(secureReq, res);
              return null;
            }
          }
          );
    }
    catch (PrivilegedActionException e) {
      Throwable t = e.getCause();
      if (t instanceof IOException) {
        throw (IOException) t;
      }
      else if (t instanceof ServletException) {
        throw (ServletException) t;
      }
      else {
        throw new ServletException(t);
      }
    }
  }

  protected void writeSessionTimeout(HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    resp.getWriter().print(("{\"errorMessage\":\"The session has expired, please reload the page.\",\"errorCode\":10}")); // JsonResponse.ERR_SESSION_TIMEOUT
  }

  protected void forwardToLoginPage(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    LOG.debug("Forwarding '{0}' to /login.html", req.getPathInfo());
    req.getRequestDispatcher("/login.html").forward(req, resp);
  }

}
