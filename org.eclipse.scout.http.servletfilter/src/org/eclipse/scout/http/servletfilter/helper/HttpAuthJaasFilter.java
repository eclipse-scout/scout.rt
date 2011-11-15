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
package org.eclipse.scout.http.servletfilter.helper;

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

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.http.servletfilter.security.SecureHttpServletRequestWrapper;

/**
 * Transformation filter used to create a subject based on {@link HttpServletRequest#getRemoteUser()} or
 * {@link HttpServletRequest#getUserPrincipal()}
 * <p>
 * If there is already a subject set as {@link Subject#getSubject(java.security.AccessControlContext)} then the filter
 * is transparent.
 * <p>
 * Normally this filters the alias /process
 * <p>
 * This filter is registered in the scout server plugin.xml as /process by default with order 1'000'000 and has the
 * active flag set to true
 */
public class HttpAuthJaasFilter implements Filter {
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
    if (isSubjectSet()) {
      chain.doFilter(in, out);
      return;
    }
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(in);
    if (!config.isActive()) {
      chain.doFilter(in, out);
      return;
    }

    HttpServletRequest req = (HttpServletRequest) in;
    HttpServletResponse res = (HttpServletResponse) out;

    // check if subject that has one principal at minimum is available
    // create subject if necessary
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null || subject.isReadOnly()) {
      subject = new Subject();
    }

    // create principal if necessary
    Principal principal = req.getUserPrincipal();
    if (principal != null && !StringUtility.hasText(principal.getName())) {
      principal = null;
      String name = req.getRemoteUser();
      if (StringUtility.hasText(name)) {
        principal = new SimplePrincipal(name);
      }
    }
    if (principal == null) {
      // no principal provided, abort chain
      res.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
    }

    subject.getPrincipals().add(principal);
    subject.setReadOnly();

    continueChainWithPrincipal(subject, req, res, chain);
  }

  private boolean isSubjectSet() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      return false;
    }
    if (subject.getPrincipals().size() == 0) {
      return false;
    }
    String name = subject.getPrincipals().iterator().next().getName();
    if (name == null || name.trim().length() == 0) {
      return false;
    }
    return true;
  }

  private void continueChainWithPrincipal(Subject subject, final HttpServletRequest req, final HttpServletResponse res, final FilterChain chain) throws IOException, ServletException {
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
