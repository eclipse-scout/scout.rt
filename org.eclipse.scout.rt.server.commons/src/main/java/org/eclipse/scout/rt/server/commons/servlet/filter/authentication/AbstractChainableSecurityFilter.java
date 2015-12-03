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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.authentication.SecureHttpServletRequestWrapper;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;

/**
 * @deprecated will be removed in release 6.0; is to be replaced with a project specific ServletFilter with the
 *             authenticators chained yourself. See {@link IAuthenticator} and its subclasses.
 *             <p>
 *             Example client-side filter:
 *
 *             <pre>
 *             &#64;Override
 *             public void doFilter(final ServletRequest request, final ServletResponse response, FilterChain chain) throws IOException, ServletException {
 *               final HttpServletRequest req = (HttpServletRequest) request;
 *               final HttpServletResponse resp = (HttpServletResponse) response;
 *
 *               if (m_formAuthenticator.handle(req, resp, chain)) {
 *                 return;
 *               }
 *
 *               if (m_trivialAuthenticator.handle(req, resp, chain)) {
 *                 return;
 *               }
 *
 *               if (m_devAuthenticator.handle(req, resp, chain)) {
 *                 return;
 *               }
 *
 *               ...
 *
 *               m_formAuthenticator.forwardToLoginForm(req, resp);
 *             }
 *             </pre>
 *
 *             Example server-side filter:
 *
 *             <pre>
 *             &#64;Override
 *             public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
 *               final HttpServletRequest req = (HttpServletRequest) request;
 *               final HttpServletResponse resp = (HttpServletResponse) response;
 *
 *               if (m_trivialAuthenticator.handle(req, resp, chain)) {
 *                 return;
 *               }
 *
 *               if (m_tunnelTokenAuthenticator.handle(req, resp, chain)) {
 *                 return;
 *               }
 *
 *               if (m_devAuthenticator.handle(req, resp, chain)) {
 *                 return;
 *               }
 *
 *               resp.sendError(HttpServletResponse.SC_FORBIDDEN);
 *             }
 *             </pre>
 */
@Deprecated
@SuppressWarnings("deprecation")
public abstract class AbstractChainableSecurityFilter implements Filter {
  public static final String PROP_SUBJECT = Subject.class.getName();

  public static final int STATUS_CONTINUE_CHAIN = 1;
  public static final int STATUS_BREAK_CHAIN = 2;
  public static final int STATUS_CONTINUE_WITH_PRINCIPAL = 3;

  private boolean m_failover;
  private String m_realm;

  /**
   * identifier for this filter.
   *
   * @rn aho, 4.6.09
   */
  protected String getFilterId() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void init(FilterConfig config) throws ServletException {

    // read config
    m_failover = "true".equals(config.getInitParameter("failover"));
    m_realm = StringUtility.nvl(config.getInitParameter("realm"), "Default");
  }

  @Override
  public final void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) in;
    final HttpServletResponse res = (HttpServletResponse) out;
    //touch the session so it is effectively used
    req.getSession();
    // check subject on session
    Subject subject = findSubject(req, res);

    if (subject == null || subject.getPrincipals().size() == 0) {
      //try negotiate
      PrincipalHolder pHolder = new PrincipalHolder();
      switch (negotiate(req, res, pHolder)) {
        case STATUS_CONTINUE_CHAIN:
          if (m_failover) {
            chain.doFilter(req, res);
            return;
          }
          else {
            res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }
        case STATUS_BREAK_CHAIN:
          return;
        case STATUS_CONTINUE_WITH_PRINCIPAL:
          if (subject == null || subject.isReadOnly()) {
            subject = new Subject();
          }
          subject.getPrincipals().add(pHolder.getPrincipal());
          subject.setReadOnly();
          cacheSubject(req, res, subject);
          break;
      }
    }
    //run in subject
    if (Subject.getSubject(AccessController.getContext()) != null) {
      doFilterInternal(req, res, chain);
    }
    else {
      try {
        Subject.doAs(subject, new PrivilegedExceptionAction<Object>() {
          @Override
          public Object run() throws Exception {
            HttpServletRequest secureReq = req;
            if (!(secureReq instanceof SecureHttpServletRequestWrapper)) {
              Principal principal = Subject.getSubject(AccessController.getContext()).getPrincipals().iterator().next();
              secureReq = new SecureHttpServletRequestWrapper(req, principal);
            }
            doFilterInternal(secureReq, res, chain);
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

  /**
   * Find already existing subject
   */
  protected Subject findSubject(final HttpServletRequest req, final HttpServletResponse res) {
    synchronized (req.getSession()) {
      Subject subject = getCachedSubject(req, res);
      //check if we are already authenticated
      if (subject == null) {
        subject = Subject.getSubject(AccessController.getContext());
      }
      if (subject == null) {
        Principal principal = req.getUserPrincipal();
        if (principal == null || !StringUtility.hasText(principal.getName())) {
          principal = null;
          String name = req.getRemoteUser();
          if (StringUtility.hasText(name)) {
            principal = new SimplePrincipal(name);
          }
        }
        if (principal != null) {
          subject = createSubject(principal);
          cacheSubject(req, res, subject);
        }
      }
      return subject;
    }
  }

  protected void cacheSubject(final HttpServletRequest req, final HttpServletResponse res, Subject subject) {
    synchronized (req.getSession()) {
      BEANS.get(IHttpSessionCacheService.class).put(PROP_SUBJECT, subject, req, res);
    }
  }

  protected Subject getCachedSubject(final HttpServletRequest req, final HttpServletResponse res) {
    synchronized (req.getSession()) {
      Object s = BEANS.get(IHttpSessionCacheService.class).getAndTouch(PROP_SUBJECT, req, res);
      if (s instanceof Subject) {
        return (Subject) s;
      }
      return null;
    }
  }

  protected Subject createSubject(Principal principal) {
    Subject s = new Subject();
    s.getPrincipals().add(principal);
    s.setReadOnly();
    return s;
  }

  /**
   * set the 'WWW-Authenticate' value on the response to enforce the client to provide login data.
   *
   * @param req
   * @param resp
   * @return
   */
  protected abstract int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException;

  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(req, res);
  }

  public String getRealm() {
    return m_realm;
  }

  public boolean isFailover() {
    return m_failover;
  }

}
