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
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.commons.security.SimplePrincipal;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;

/**
 * Convenience authentication filter for development mode using the system property user.name
 */
@Bean
public class DevelopmentAuthenticator {
  private static final IScoutLogger LOG = ScoutLogManager.getLogger(DevelopmentAuthenticator.class);

  private boolean m_showWarning = true;
  private boolean m_active;

  public void init(FilterConfig config) throws ServletException {
    m_active = Platform.get().inDevelopmentMode();
  }

  public void destroy() {
  }

  public boolean isActive() {
    return m_active;
  }

  /**
   * @return true if the request was handled (caller returns), false if nothing was done (caller continues)
   */
  public boolean handle(HttpServletRequest req, HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    if (!isActive()) {
      return false;
    }
    if (m_showWarning) {
      LOG.warn("+++Development security: Create Subject based on System.getProperty('user.name')");
      m_showWarning = false;
    }
    Principal principal = createDevelopmentPrincipal();

    ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    helper.putPrincipalOnSession(req, principal);
    helper.continueChainAsSubject(principal, req, resp, chain);
    return true;
  }

  protected Principal createDevelopmentPrincipal() {
    return new SimplePrincipal(System.getProperty("user.name"));
  }
}
