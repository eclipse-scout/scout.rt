/*
 * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
 * http://www.bsiag.com/
 */
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.exception.PlatformException;

/**
 * @since 5.0
 */
@Bean
public class ServletFilterHelper {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ServletFilterHelper.class);

  public static final String SESSION_ATTRIBUTE_FOR_PRINCIPAL = Principal.class.getName();

  public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";
  public static final String HTTP_HEADER_AUTHORIZED = "Authorized";
  public static final String HTTP_BASIC_AUTH_NAME = "Basic";
  public static final String HTTP_BASIC_AUTH_CHARSET = "ISO-8859-1";

  /**
   * get a cached principal from the {@link HttpSession} as {@link #SESSION_ATTRIBUTE_FOR_PRINCIPAL}
   */
  public Principal getPrincipalOnSession(HttpServletRequest req) {
    final HttpSession session = req.getSession(false);
    if (session != null) {
      Principal principal = (Principal) session.getAttribute(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_PRINCIPAL);
      if (principal != null) {
        return principal;
      }
    }
    return null;
  }

  /**
   * put a principal to the {@link HttpSession} as {@link #SESSION_ATTRIBUTE_FOR_PRINCIPAL}
   */
  public void putPrincipalOnSession(HttpServletRequest req, Principal principal) {
    HttpSession session = req.getSession();
    session.setAttribute(ServletFilterHelper.SESSION_ATTRIBUTE_FOR_PRINCIPAL, principal);
  }

  public Subject createSubject(Principal principal) {
    // create subject if necessary
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.isReadOnly()) {
      subject = new Subject();
    }
    subject.getPrincipals().add(principal);
    subject.setReadOnly();
    return subject;
  }

  public void continueChainAsSubject(final Principal principal, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
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

  public String createBasicAuthRequest(String username, char[] password) {
    try {
      StringBuffer cred = new StringBuffer(username).append(":").append(password);
      String encodedCred;
      encodedCred = Base64Utility.encode(cred.toString().getBytes(HTTP_BASIC_AUTH_CHARSET));
      return new StringBuffer(HTTP_BASIC_AUTH_NAME).append(" ").append(encodedCred).toString();
    }
    catch (UnsupportedEncodingException e) {
      throw new PlatformException("charset " + HTTP_BASIC_AUTH_CHARSET, e);
    }
  }

  public String[] parseBasicAuthRequest(HttpServletRequest req) {
    try {
      String h = req.getHeader(HTTP_HEADER_AUTHORIZATION);
      if (h == null || !h.matches(HTTP_BASIC_AUTH_NAME + " .*")) {
        return null;
      }
      return new String(Base64Utility.decode(h.substring(6)), HTTP_BASIC_AUTH_CHARSET).split(":", 2);
    }
    catch (UnsupportedEncodingException e) {
      throw new PlatformException("charset " + HTTP_BASIC_AUTH_CHARSET, e);
    }
  }

  /**
   * forward the request to the login.html
   * <p>
   * Detect if the request is a POST. For json send a timeout message, otherwise log a warning
   *
   * @throws IOException
   * @throws ServletException
   */
  public void forwardToLoginForm(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    //check if POST request
    if ("POST".equals(req.getMethod())) {
      if (("" + req.getContentType()).startsWith("application/json")) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("Returning session timeout error as json.");
        }
        sendJsonSessionTimeout(resp);
        return;
      }
      else {
        LOG.warn("The request for {0} is a POST request. Forwarding to the login page will most likely fail because StaticResourceRequestInterceptor doesn't handle post.", req.getPathInfo());
      }
    }

    if (LOG.isDebugEnabled()) {
      LOG.debug("Forwarding '{0}' to /login.html", (req).getPathInfo());
    }
    req.getRequestDispatcher("/login.html").forward(req, resp);
  }

  protected void sendJsonSessionTimeout(HttpServletResponse resp) throws IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    resp.getWriter().print(("{\"errorMessage\":\"The session has expired, please reload the page.\",\"errorCode\":10}")); // JsonResponse.ERR_SESSION_TIMEOUT
  }

}
