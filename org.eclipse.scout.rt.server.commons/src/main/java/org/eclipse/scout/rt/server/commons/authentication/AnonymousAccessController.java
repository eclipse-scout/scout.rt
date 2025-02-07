/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.authentication;

import java.io.IOException;
import java.security.Principal;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractBooleanConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.security.IPrincipalProducer;
import org.eclipse.scout.rt.platform.security.SimplePrincipalProducer;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access controller to always continue filter-chain with a fixed user. By default, the user's name is 'anonymous'.
 * <p>
 * Typically, this access controller is used when having an application that does not require user authentication.
 *
 * @since 5.2
 */
public class AnonymousAccessController implements IAccessController {
  private static final Logger LOG = LoggerFactory.getLogger(AnonymousAccessController.class);

  private AnonymousAuthConfig m_config;

  public AnonymousAccessController init() {
    init(new AnonymousAuthConfig());
    return this;
  }

  public AnonymousAccessController init(final AnonymousAuthConfig config) {
    m_config = config;
    Assertions.assertNotNull(m_config.getPrincipalProducer(), "PrincipalProducer must not be null");
    return this;
  }

  @Override
  public boolean handle(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) throws IOException, ServletException {
    if (!m_config.isEnabled()) {
      return false;
    }

    final Principal principal = m_config.getPrincipalProducer().produce(m_config.getUsername());
    final ServletFilterHelper helper = BEANS.get(ServletFilterHelper.class);
    if (m_config.isPutPrincipalOnSession()) {
      if (request.getSession(false) == null && m_config.getAbortOnNoSessionFilter().accepts(request.getPathInfo())) {
        // Don't create a http session and abort filter chain if resource is listed in getAbortOnNoSessionFilter()
        LOG.debug("Aborting filter chain for request {}", request.getPathInfo());
        return false;
      }
      helper.putPrincipalOnSession(request, principal);
    }
    helper.continueChainAsSubject(principal, request, response, chain);
    return true;
  }

  @Override
  public void destroy() {
    // NOOP
  }

  /**
   * Configuration for {@link AnonymousAccessController}.
   */
  public static class AnonymousAuthConfig {

    private boolean m_enabled = CONFIG.getPropertyValue(EnabledProperty.class);
    private IPrincipalProducer m_principalProducer = BEANS.get(SimplePrincipalProducer.class);
    private String m_username = "anonymous";
    private boolean m_putPrincipalOnSession = true;
    private PathInfoFilter m_abortOnNoSessionFilter;
    // api-servlet may be on the same server (/ui-notifications) or on a remote server (e.g. /api/ui-notifications)
    private String m_defaultAbortOnNoSessionFilter = "/ui-notifications,/*/ui-notifications";

    public AnonymousAuthConfig() {
      withAbortOnNoSessionFilter(""); // Initialize with default values
    }

    public boolean isEnabled() {
      return m_enabled;
    }

    public AnonymousAuthConfig withEnabled(final boolean enabled) {
      m_enabled = enabled;
      return this;
    }

    public IPrincipalProducer getPrincipalProducer() {
      return m_principalProducer;
    }

    public AnonymousAuthConfig withPrincipalProducer(final IPrincipalProducer principalProducer) {
      m_principalProducer = principalProducer;
      return this;
    }

    public String getUsername() {
      return m_username;
    }

    public AnonymousAuthConfig withUsername(final String username) {
      m_username = username;
      return this;
    }

    public boolean isPutPrincipalOnSession() {
      return m_putPrincipalOnSession;
    }

    public AnonymousAuthConfig withPutPrincipalOnSession(final boolean putPrincipalOnSession) {
      m_putPrincipalOnSession = putPrincipalOnSession;
      return this;
    }

    public PathInfoFilter getAbortOnNoSessionFilter() {
      return m_abortOnNoSessionFilter;
    }

    /**
     * If {@link #isPutPrincipalOnSession()} is true, a http session will be created if there is none yet.
     * With this filter it is possible to exclude some resources from creating a http session and to abort the filter chain in that case.
     * <p>
     * The paths listed by {@link #getDefaultAbortOnNoSessionFilter()} are always added.
     * <p>
     * Filter format: separate resources by comma, newline or whitespace; usage of wildcard (*) character is supported;
     */
    public AnonymousAuthConfig withAbortOnNoSessionFilter(String abortFilter) {
      Assertions.assertNotNull(abortFilter);
      m_abortOnNoSessionFilter = new PathInfoFilter(StringUtility.join(",", abortFilter, getDefaultAbortOnNoSessionFilter()));
      return this;
    }

    public String getDefaultAbortOnNoSessionFilter() {
      return m_defaultAbortOnNoSessionFilter;
    }

    public AnonymousAuthConfig withDefaultAbortOnNoSessionFilter(String defaultAbortOnNoSessionFilter) {
      m_defaultAbortOnNoSessionFilter = defaultAbortOnNoSessionFilter;
      return this;
    }
  }

  public static class EnabledProperty extends AbstractBooleanConfigProperty {

    @Override
    public String getKey() {
      return "scout.auth.anonymousEnabled";
    }

    @Override
    public String description() {
      return String.format("Specifies if the '%s' is enabled. Therefore, if a security filter uses this controller no login is required.", AnonymousAccessController.class.getSimpleName());
    }

    @Override
    public Boolean getDefaultValue() {
      return true;
    }
  }
}
