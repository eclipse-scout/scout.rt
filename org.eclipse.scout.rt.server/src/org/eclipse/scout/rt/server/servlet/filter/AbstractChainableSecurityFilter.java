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

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.http.servletfilter.FilterConfigInjection;
import org.eclipse.scout.rt.shared.services.common.security.SimplePrincipal;

/**
 * <h4>AbstractChainableSecurityFilter</h4> The following properties can be set
 * in the <code>config.ini</code> file:
 * <ul>
 * <li><code>&lt;fully qualified name of class&gt;#active=true/false</code></li>
 * <li><code>&lt;fully qualified name of class&gt;#realm=abcde</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#failover=true/false</code> <b>default false</b></li>
 * </ul>
 * <p>
 * <h5>NOTE</h5> All security filters inheriting from {@link AbstractChainableSecurityFilter} are chainable. What means
 * can be used together with other Filters. The <code>runOrder</code> flag of the extension point defines the run order
 * of chainable security filters. To make this filter chainable set the flag failover to true. <b>Ensure to set the
 * failover flag on the last security filter to false!</b>
 * <p>
 * Make sure to dectivate session persistence. In tomcat: in server.xml inside <Context> tag add
 * 
 * <pre>
 * &lt;Manager className="org.apache.catalina.session.StandardManager" pathname=""&gt; &lt;/Manager&gt;
 * </pre>
 * 
 * @since 1.0.3 06.02.2009
 */
public abstract class AbstractChainableSecurityFilter implements Filter {
  private static IScoutLogger LOG = ScoutLogManager.getLogger(AbstractChainableSecurityFilter.class);
  public static final String PROP_SUBJECT = Subject.class.getName();

  public static final int STATUS_CONTINUE_CHAIN = 1;
  public static final int STATUS_BREAK_CHAIN = 2;
  public static final int STATUS_CONTINUE_WITH_PRINCIPAL = 3;

  private boolean m_failover;
  private String m_realm;
  private FilterConfigInjection m_injection;

  public AbstractChainableSecurityFilter() {
  }

  /**
   * identifier for this filter.
   * 
   * @rn aho, 4.6.09
   */
  protected String getFilterId() {
    return this.getClass().getSimpleName();
  }

  @Override
  public void init(FilterConfig config0) throws ServletException {
    m_injection = new FilterConfigInjection(config0, getClass());
    FilterConfigInjection.FilterConfig config = m_injection.getAnyConfig();
    String failoverString = config.getInitParameter("failover");
    m_failover = Boolean.parseBoolean(failoverString);
    String realmParam = config.getInitParameter("realm");
    if (realmParam == null) {
      realmParam = "Default";
    }
    m_realm = realmParam;
  }

  @Override
  public void destroy() {
    m_injection = null;
  }

  @Override
  public final void doFilter(ServletRequest in, ServletResponse out, final FilterChain chain) throws IOException, ServletException {
    //ticket 94794
    FilterConfigInjection.FilterConfig config = m_injection.getConfig(in);
    if (!config.isActive()) {
      chain.doFilter(in, out);
      return;
    }
    //
    final HttpServletRequest req = (HttpServletRequest) in;
    final HttpServletResponse res = (HttpServletResponse) out;
    //touch the session so it is effectively used
    req.getSession();
    // check subject on session
    Subject subject = null;
    synchronized (req.getSession()) {
      subject = findSubjectOnSession(req, res);
    }
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
          synchronized (req.getSession()) {
            req.getSession().setAttribute(PROP_SUBJECT, subject);
          }
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

  private Subject findSubjectOnSession(HttpServletRequest req, HttpServletResponse resp) {
    Object o = null;
    Subject subject = null;
    o = req.getSession().getAttribute(PROP_SUBJECT);

    if (o instanceof Subject) {
      subject = (Subject) o;
    }
    //check if we are already authenticated
    if (subject == null) {
      subject = Subject.getSubject(AccessController.getContext());
    }

    //    Subject subject=Subject.getSubject(AccessController.getContext());
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
        subject.setReadOnly();
        req.getSession().setAttribute(PROP_SUBJECT, subject);
      }
    }
    return subject;
  }

  /**
   * set the 'WWW-Authenticate' value on the response to enforce the client to provide login data.
   * 
   * @param req
   * @param resp
   * @return
   */
  protected abstract int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException;

  private void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(req, res);
  }

  public String getRealm() {
    return m_realm;
  }

  public boolean isFailover() {
    return m_failover;
  }

}
