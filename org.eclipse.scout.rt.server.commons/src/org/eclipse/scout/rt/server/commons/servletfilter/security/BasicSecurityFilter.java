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
package org.eclipse.scout.rt.server.commons.servletfilter.security;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.Base64Utility;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servletfilter.FilterConfigInjection;
import org.eclipse.scout.service.SERVICES;

/**
 * <h4>BasicSecurityFilter</h4> A simple security filter using username,password
 * tuples either set in the extension point or in the config ini.
 * <ul>
 * <li><code>&lt;fully qualified name of class&gt;#active=true/false</code> <b>might be set in the extension point</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#realm=abcde</code> <b>required</b></li>
 * <li><code>&lt;fully qualified name of class&gt;#failover=true/false</code> <b>default false</b></li>
 * <li>
 * <code>&lt;fully qualified name of class&gt;#users=username1\=password1,username2\=password2</code>
 * <b>required</b></li>
 * </ul>
 * <p>
 * 
 * @since 1.0.3 06.02.2009
 */
public class BasicSecurityFilter extends AbstractChainableSecurityFilter {
  public static final String PROP_BASIC_ATTEMPT = "BasicSecurityFilter.basicAttempt";

  private HashMap<String, String> m_userDatabase;

  @Override
  public void init(FilterConfig config0) throws ServletException {
    super.init(config0);
    FilterConfigInjection.FilterConfig config = new FilterConfigInjection(config0, getClass()).getAnyConfig();
    String usersParam = config.getInitParameter("users");
    if (usersParam == null) {
      throw new ServletException("missing init-param with name 'users'");
    }
    m_userDatabase = new HashMap<String, String>();
    for (String pair : usersParam.split(",")) {
      String[] a = pair.trim().split("=", 2);
      m_userDatabase.put(a[0].toLowerCase(), a[1]);
    }
  }

  @Override
  public void destroy() {
    m_userDatabase.clear();
  }

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    String h = req.getHeader("Authorization");
    if (h != null && h.matches("Basic .*")) {
      String[] a = new String(Base64Utility.decode(h.substring(6)), "ISO-8859-1").split(":", 2);
      String user = a[0].toLowerCase();
      String pass = a[1];
      if (user != null && pass != null) {
        if (pass.equals(m_userDatabase.get(user))) {// check can also be based on
          // filter init params
          // success
          holder.setPrincipal(new SimplePrincipal(user));
          return STATUS_CONTINUE_WITH_PRINCIPAL;
        }
      }
    }
    int attempts = getBasicAttempt(req, resp);
    if (attempts > 2) {
      return STATUS_CONTINUE_CHAIN;
    }
    else {
      setBasicAttept(req, resp, attempts + 1);
      resp.setHeader("WWW-Authenticate", "Basic realm=\"" + getRealm() + "\"");
      return STATUS_CONTINUE_CHAIN;
    }
  }

  private int getBasicAttempt(HttpServletRequest req, HttpServletResponse res) {
    int basicAtttempt = 0;
    Object attribute = SERVICES.getService(IHttpSessionCacheService.class).getAndTouch(PROP_BASIC_ATTEMPT, req, res);
    if (attribute instanceof Integer) {
      basicAtttempt = ((Integer) attribute).intValue();
    }
    return basicAtttempt;
  }

  private void setBasicAttept(HttpServletRequest req, HttpServletResponse res, int attempts) {
    SERVICES.getService(IHttpSessionCacheService.class).put(PROP_BASIC_ATTEMPT, attempts, req, res);
  }

}
