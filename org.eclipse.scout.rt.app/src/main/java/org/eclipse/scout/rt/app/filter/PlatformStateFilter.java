/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.app.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter that rejects requests while the platform is not fully started.
 * <p>
 * Requests are only forwarded if the current {@link IPlatform} is in state {@link IPlatform.State#PlatformStarted}.
 * Otherwise, this filter responds with HTTP status {@link HttpServletResponse#SC_SERVICE_UNAVAILABLE} (503).
 */
public class PlatformStateFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(PlatformStateFilter.class);

  @Override
  public void init(FilterConfig filterConfig) {
    // nothing to initialize
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletResponse resp = (HttpServletResponse) response;
    final IPlatform platform = Platform.peek();
    if (platform == null || platform.getState() != IPlatform.State.PlatformStarted) {
      LOG.debug("Platform is not running");
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }

    chain.doFilter(request, response);
  }
}
