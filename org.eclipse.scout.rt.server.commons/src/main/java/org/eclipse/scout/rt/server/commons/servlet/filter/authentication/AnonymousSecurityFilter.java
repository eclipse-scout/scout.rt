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

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.SimplePrincipal;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;

/**
 * A security filter allowing anonymous access to the application.
 *
 * @deprecated will be removed in release 6.0; is to be replaced with a project specific ServletFilter with the
 *             authenticators chained yourself; see depreciation note of {@link AbstractChainableSecurityFilter}
 */
@SuppressWarnings("deprecation")
@Deprecated
public class AnonymousSecurityFilter extends AbstractChainableSecurityFilter {

  public static final String ANONYMOUS_USER_NAME = "anonymous";

  @Override
  protected int negotiate(HttpServletRequest req, HttpServletResponse resp, PrincipalHolder holder) throws IOException, ServletException {
    holder.setPrincipal(new SimplePrincipal(ANONYMOUS_USER_NAME));
    return STATUS_CONTINUE_WITH_PRINCIPAL;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
    if (isLogoutRequest(req)) {
      ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
      helper.doLogout(req);
      helper.forwardToLogoutForm(req, res);
      return;
    }

    if (isLoginRequest(req)) {
      ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
      helper.doLogout(req);
      helper.redirectTo(req, res, "");
      return;
    }

    super.doFilterInternal(req, res, chain);
  }

  protected boolean isLogoutRequest(HttpServletRequest req) {
    return "/logout".equals(req.getPathInfo());
  }

  protected boolean isLoginRequest(HttpServletRequest req) {
    return "/login".equals(req.getPathInfo());
  }

  @Override
  public void destroy() {
  }
}
