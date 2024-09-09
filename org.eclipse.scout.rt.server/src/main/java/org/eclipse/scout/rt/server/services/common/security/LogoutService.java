/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.services.common.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.IAccessControlService;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.services.common.security.ILogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutService implements ILogoutService {
  private static final Logger LOG = LoggerFactory.getLogger(LogoutService.class);

  @Override
  public void logout() {
    HttpServletRequest httpRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    if (httpRequest != null) {
      // clear permission cache only if logout is triggered by an external HTTP request (explicitly not when triggered
      // by HTTP session invalidation)
      BEANS.get(IAccessControlService.class).clearCacheOfCurrentUser();

      try {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
          session.invalidate();
        }
      }
      catch (IllegalStateException e) {
        LOG.debug("Tried to invalidate an already invalidated session.", e);
      }
      catch (Exception e) {
        LOG.warn("Failed to invalidate HTTP session.", e);
      }
    }
  }
}
