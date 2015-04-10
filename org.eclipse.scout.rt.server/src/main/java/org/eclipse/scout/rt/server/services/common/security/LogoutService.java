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

import org.eclipse.scout.commons.annotations.Priority;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.service.AbstractService;
import org.eclipse.scout.rt.platform.service.SERVICES;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.Server;
import org.eclipse.scout.rt.server.commons.cache.IHttpSessionCacheService;
import org.eclipse.scout.rt.server.commons.servletfilter.IHttpServletRoundtrip;
import org.eclipse.scout.rt.shared.services.common.security.ILogoutService;

@Priority(-1)
@Server
public class LogoutService extends AbstractService implements ILogoutService {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(LogoutService.class);

  @Override
  public void logout() {
    HttpServletRequest httpRequest = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get();
    HttpServletResponse httpResponse = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_RESPONSE.get();

    SERVICES.getService(IHttpSessionCacheService.class).remove(IServerSession.class.getName(), httpRequest, httpResponse);
    SERVICES.getService(IHttpSessionCacheService.class).remove(Subject.class.getName(), httpRequest, httpResponse);
    try {
      HttpSession session = httpRequest.getSession();
      session.invalidate();
    }
    catch (IllegalStateException e) {
      //already invalid
    }
    catch (Throwable t) {
      LOG.warn("Failed to invalidate HTTP session.", t);
    }
  }
}
