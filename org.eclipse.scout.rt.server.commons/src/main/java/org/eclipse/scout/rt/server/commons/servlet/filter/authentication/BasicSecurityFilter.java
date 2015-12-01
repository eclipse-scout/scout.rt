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
package org.eclipse.scout.rt.server.commons.servlet.filter.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.util.Base64Utility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;

/**
 * <h4>BasicSecurityFilter</h4> A simple security filter using username,password tuples defined in the config.properties
 * file. The following properties are supported:
 * <ul>
 * <li><code>realm=abcde</code> <b>default: 'Default'</b></li>
 * <li><code>failover=true/false</code> <b>default false</b></li>
 * <li><code>users=username1\=password1,username2\=password2</code> <b>required</b></li>
 * </ul>
 * <p>
 *
 * @deprecated will be removed in release 6.0; is to be replaced with a project specific ServletFilter with the
 *             authenticators chained yourself; see depreciation note of {@link AbstractChainableSecurityFilter}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class BasicSecurityFilter extends AbstractChainableSecurityFilter {
  public static final String PROP_BASIC_ATTEMPT = "BasicSecurityFilter.basicAttempt";

  private Map<String, String> m_userDatabase;

  @Override
  public void init(FilterConfig config) throws ServletException {
    super.init(config);

    // read config
    String usersParam = config.getInitParameter("users");
    if (!StringUtility.hasText(usersParam)) {
      throw new ServletException("missing init-param with name 'users'.");
    }

    m_userDatabase = new HashMap<>();
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
    Object attribute = BEANS.get(IHttpSessionCacheService.class).getAndTouch(PROP_BASIC_ATTEMPT, req, res);
    if (attribute instanceof Integer) {
      basicAtttempt = ((Integer) attribute).intValue();
    }
    return basicAtttempt;
  }

  private void setBasicAttept(HttpServletRequest req, HttpServletResponse res, int attempts) {
    BEANS.get(IHttpSessionCacheService.class).put(PROP_BASIC_ATTEMPT, attempts, req, res);
  }

}
