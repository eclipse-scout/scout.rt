/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.filter;

import java.io.IOException;

import javax.security.auth.Subject;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.dataobject.exception.AccessForbiddenException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.security.User;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.commons.authentication.ServletFilterHelper;
import org.eclipse.scout.rt.shared.ui.UserAgents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets up the {@link User} and caches it on the HTTP session.
 */
public class UiServletUserBootstrapFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(UiServletUserBootstrapFilter.class);

  protected ServletFilterHelper m_helper = BEANS.get(ServletFilterHelper.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    try {
      Subject subject = Subject.current();
      // requests excluded by AuthFilter run without Subject, exclude from resolving user
      if (subject != null && m_helper.getUserOnSession(httpRequest) == null) {
        User user = ClientRunContexts.empty()
            .withSubject(subject)
            .withUser(BEANS.get(User.class).withUserId(BEANS.get(IAccessControlService.class).extractUserId(subject)).setReadOnly()) // use ad-hoc user instance to load the real user object
            .withUserAgent(UserAgents.createDefault())
            .call(() -> BEANS.get(IAccessControlService.class).getUser(subject));

        // cache user for this HTTP session (see above)
        m_helper.putUserOnSession(httpRequest, user);
      }

      chain.doFilter(request, response);
    }
    catch (Throwable t) {
      // always invalidate session on any error
      BEANS.get(ServletFilterHelper.class).doLogout(httpRequest);

      if (t instanceof AccessForbiddenException) { // pass-through HTTP error 403, may be thrown by IAccessControlService when resolving user
        LOG.trace("Unable to load user", t);

        // send forbidden
        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
      }
      else {
        throw t;
      }
    }
  }
}
