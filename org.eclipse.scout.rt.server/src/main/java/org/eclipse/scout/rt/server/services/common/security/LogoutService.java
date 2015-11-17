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
package org.eclipse.scout.rt.server.services.common.security;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.services.common.security.IAccessControlService;
import org.eclipse.scout.rt.shared.services.common.security.ILogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogoutService implements ILogoutService {
  private static final Logger LOG = LoggerFactory.getLogger(LogoutService.class);

  @Override
  public void logout() {
    try {
      BEANS.get(IAccessControlService.class).clearCacheOfCurrentUser();

      HttpServletRequest httpRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
      HttpServletResponse httpResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

      BEANS.get(IHttpSessionCacheService.class).remove(IServerSession.class.getName(), httpRequest, httpResponse);
      BEANS.get(IHttpSessionCacheService.class).remove(Subject.class.getName(), httpRequest, httpResponse);
      HttpSession session = httpRequest.getSession();
      session.invalidate();
    }
    catch (IllegalStateException e) {
      //already invalid
    }
    catch (Exception e) {
      LOG.warn("Failed to invalidate HTTP session.", e);
    }
  }
}
