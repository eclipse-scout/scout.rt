/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.servlet.filter;

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

import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.shared.services.common.security.SimplePrincipal;

/**
 * In Tomcat, the Subject is lost after authentication and is not passed to the servlet.
 * Only the remoteUser and the userPrincipal are available.<br>
 * This servlet-filter ensures that each request is executed in a secure context.
 * This filter doesn't authenticate the caller, this is the responsibility of Tomcat/Container<br>
 * <br>
 * For Tomcat 6, following steps are required to achieve sso with Windows AD:<br>
 * <li>copy spnego.jar to tomcat\lib</li> <br>
 * <li>copy krb5.conf and login.conf to tomcat-home</li><br>
 * <li>adjust paramters in login.conf</li><br>
 * <li>adjust paramters in conf\web.xml</li><br>
 */
public class TomcatSecurityFilter implements Filter {
  private FilterConfigInjection m_injection;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    m_injection = new FilterConfigInjection(config0, getClass());
  }

  @Override
  public void destroy() {
    m_injection = null;
  }

  @Override
  public void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(in);
    if (!config.isActive()) {
      chain.doFilter(in, out);
      return;
    }
    //
    final HttpServletRequest req = (HttpServletRequest) in;
    final HttpServletResponse res = (HttpServletResponse) out;
    // touch the session so it is effectively used
    req.getSession();
    // check if we are already authenticated
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      Principal principal = req.getUserPrincipal();
      if (principal == null || principal.getName() == null || principal.getName().trim().length() == 0) {
        principal = null;
        String name = req.getRemoteUser();
        if (name != null && name.trim().length() > 0) {
          principal = new SimplePrincipal(name);
        }
      }
      if (principal != null) {
        subject = new Subject();
        subject.getPrincipals().add(principal);
      }
    }
    // run in subject
    if (Subject.getSubject(AccessController.getContext()) != null) {
      doFilterInternal(req, res, chain);
    }
    else {
      try {
        Subject.doAs(
            subject,
            new PrivilegedExceptionAction<Object>() {
              public Object run() throws Exception {
                HttpServletRequest secureReq = req;
                if (!(secureReq instanceof SecureHttpServletRequestWrapper)) {
                  Principal principal = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next();
                  secureReq = new SecureHttpServletRequestWrapper(req, principal, null);
                }
                doFilterInternal(secureReq, res, chain);
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

  private void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(req, res);
  }
}
