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
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convenience authenticator if running in development mode using the system property 'user.name'.
 */
@Bean
public class DevelopmentAuthenticator implements IAuthenticator {

  private static final Logger LOG = LoggerFactory.getLogger(DevelopmentAuthenticator.class);

  private boolean m_showWarning = true;

  private DevAuthConfig m_config;

  public void init() {
    init(new DevAuthConfig());
  }

  public void init(final DevAuthConfig config) {
    m_config = config;
  }

  @Override
  public boolean handle(final HttpServletRequest req, final HttpServletResponse resp, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    if (m_showWarning) {
      LOG.warn("+++Development security: run with subject {}", m_config.getUsername());
      m_showWarning = false;
    }

    final Principal principal = m_config.getPrincipalProducer().produce(m_config.getUsername());
    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    helper.putPrincipalOnSession(req, principal);
    helper.continueChainAsSubject(principal, req, resp, chain);
    return true;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * Configuration for {@link DevelopmentAuthenticator}.
   */
  public static class DevAuthConfig {

    private boolean m_enabled = Platform.get().inDevelopmentMode();
    private IPrincipalProducer m_principalProducer = BEANS.get(SimplePrincipalProducer.class);
    private String m_username = System.getProperty("user.name");

    public boolean isEnabled() {
      return m_enabled;
    }

    public DevAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    public DevAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }

    public String getUsername() {
      return m_username;
    }

    public DevAuthConfig withUsername(final String username) {
      m_username = username;
      return this;
    }
  }
}
