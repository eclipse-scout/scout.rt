/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

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

import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.authentication.SecureHttpServletRequestWrapper;
import org.eclipse.scout.rt.server.commons.authentication.TrivialAccessController;

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
 *
 * @deprecated will be removed in release 6.0; is to be replaced with a project specific ServletFilter with the
 *             authenticators chained yourself; use {@link TrivialAccessController} instead.
 */
@Deprecated
public class HttpAuthJaasFilter implements Filter {

  @Override
  public void init(FilterConfig config) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) in;
    HttpServletResponse res = (HttpServletResponse) out;

    String username = req.getRemoteUser();
    if (isSubjectSetWithCorrectPrincipal(username)) {
      chain.doFilter(in, out);
      return;
    }

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

  /**
   * @return true, if a {@link Subject} is already set with a principal corresponding to the given username.
   */
  private boolean isSubjectSetWithCorrectPrincipal(String username) {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      return false;
    }
    if (subject.getPrincipals().size() == 0) {
      return false;
    }
    String name = CollectionUtility.firstElement(subject.getPrincipals()).getName();
    return StringUtility.hasText(name) && name.equalsIgnoreCase(username);
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
          });
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
