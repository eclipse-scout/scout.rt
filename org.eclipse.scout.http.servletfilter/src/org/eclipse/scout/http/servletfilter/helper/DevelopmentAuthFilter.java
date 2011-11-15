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

import org.eclipse.core.runtime.Platform;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.http.servletfilter.security.SecureHttpServletRequestWrapper;

/**
 * Convenience authentication filter for development mode.
 * <p>
 * This filter is registered in the scout server plugin.xml as /process by default with order 1'000'010 and has the
 * active flag set to true
 */
public class DevelopmentAuthFilter implements Filter {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DevelopmentAuthFilter.class);

  private FilterConfigInjection m_injection;
  private boolean m_showWarning = true;

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
    if (isSubjectSet() || !Platform.inDevelopmentMode()) {
      chain.doFilter(in, out);
      return;
    }
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(in);
    if (!config.isActive()) {
      chain.doFilter(in, out);
      return;
    }
    Subject subject = createDevelopmentSubject(m_showWarning);
    m_showWarning = false;
    if (subject == null) {
      chain.doFilter(in, out);
      return;
    }
    continueChainWithPrincipal(subject, (HttpServletRequest) in, (HttpServletResponse) out, chain);
  }

  private boolean isSubjectSet() {
    Subject subject = Subject.getSubject(AccessController.getContext());
    if (subject == null) {
      return false;
    }
    return true;
  }

  protected Subject createDevelopmentSubject(boolean showWarning) {
    if (showWarning) {
      LOG.warn("+++Development security: Create Subject based on System.getProperty('user.name')");
    }
    Subject subject = new Subject();
    subject.getPrincipals().add(new SimplePrincipal(System.getProperty("user.name")));
    return subject;
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
