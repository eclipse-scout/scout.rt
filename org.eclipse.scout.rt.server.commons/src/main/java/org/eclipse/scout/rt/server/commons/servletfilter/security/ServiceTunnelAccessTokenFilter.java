package org.eclipse.scout.rt.server.commons.servletfilter.security;

import java.io.IOException;
import java.security.AccessController;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.security.auth.Subject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.shared.servicetunnel.http.AbstractHttpServiceTunnel;
import org.eclipse.scout.rt.shared.servicetunnel.http.DefaultAuthToken;

/**
 * Security filter detecting service tunnel access token based on
 * {@link AbstractHttpServiceTunnel#TOKEN_AUTH_HTTP_HEADER} and {@link AbstractHttpServiceTunnel#TOKEN_USER_ID_HEADER}
 */
public class ServiceTunnelAccessTokenFilter implements Filter {

  @Override
  public void init(FilterConfig config) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) in;

    if (!DefaultAuthToken.isActive()) {
      chain.doFilter(in, out);
      return;
    }

    String tokenString = req.getHeader(AbstractHttpServiceTunnel.TOKEN_AUTH_HTTP_HEADER);
    DefaultAuthToken token = DefaultAuthToken.parse(tokenString);
    if (token == null) {
      chain.doFilter(in, out);
      return;
    }

    // check subject
    if (!StringUtility.hasText(token.getUserId())) {
      fail(out);
      return;
    }

    // check TTL
    if (System.currentTimeMillis() > token.getValidUntil()) {
      fail(out);
      return;
    }

    // check signature
    if (!token.isValid()) {
      fail(out);
      return;
    }
    continueChainWithPrincipal(token.getUserId(), req, out, chain);
  }

  protected void fail(ServletResponse out) throws IOException {
    HttpServletResponse res = (HttpServletResponse) out;
    res.sendError(HttpServletResponse.SC_FORBIDDEN);
  }

  protected void continueChainWithPrincipal(String userId, final HttpServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(userId));
    subject.setReadOnly();

    try {
      Subject.doAs(
          subject,
          new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
              Principal principal = CollectionUtility.firstElement(Subject.getSubject(AccessController.getContext()).getPrincipals());
              HttpServletRequest secureReq = new SecureHttpServletRequestWrapper(req, principal, HttpServletRequestWrapper.CLIENT_CERT_AUTH);
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
}
