package org.eclipse.scout.rt.server;

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
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.commons.servletfilter.security.SecureHttpServletRequestWrapper;

/**
 * Security filter detecting service tunnel access token based on
 * {@link AbstractHttpServiceTunnel#TOKEN_AUTH_HTTP_HEADER} and {@link IServiceTunnel#PROP_SHARED_SECRET}
 * <p>
 * //TODO [nosgi] imo fix javadoc links
 */
public class ServiceTunnelAccessTokenFilter implements Filter {
  private String m_sharedSecret;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    m_sharedSecret = ConfigIniUtility.getProperty("org.eclipse.scout.rt.client.http.debug");//TODO imo use const IServiceTunnel.HTTP_DEBUG_PARAM
  }

  @Override
  public void destroy() {
    m_sharedSecret = null;
  }

  @Override
  public void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) in;
    HttpServletResponse res = (HttpServletResponse) out;

    String token = req.getHeader("X-ScoutAccessToken");//TODO [nosgi] imo AbstractHttpServiceTunnel.TOKEN_AUTH_HTTP_HEADER
    try {
      Principal principal = verifyToken(token);
      if (principal != null) {
        Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        subject.setReadOnly();
        continueChainWithPrincipal(subject, req, res, chain);
      }
    }
    catch (Exception e) {
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
    chain.doFilter(in, out);
  }

  protected Principal verifyToken(String token) {
    if (StringUtility.isNullOrEmpty(token)) {
      return null;
    }
    //TODO [nosgi] imo add PKI signature check
    if (!token.startsWith(m_sharedSecret)) {
      throw new SecurityException("invalid token");
    }
    return new SimplePrincipal(token.substring(m_sharedSecret.length() + 1));
  }

  protected void continueChainWithPrincipal(Subject subject, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
    try {
      Subject.doAs(
          subject,
          new PrivilegedExceptionAction<Object>() {
            @Override
            public Object run() throws Exception {
              Principal principal = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next();
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
}
