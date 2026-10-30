/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.IPlatform;
import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter that rejects requests while the platform is not fully started.
 * <p>
 * Requests are only forwarded if the current {@link IPlatform} is in state {@link IPlatform.State#PlatformStarted}.
 * Otherwise, this filter responds with HTTP status {@link HttpServletResponse#SC_SERVICE_UNAVAILABLE} (503).
 * <p>
 * In the regular lifecycle this filter has no effect: {@code Application} starts the server only after the platform has
 * been started and stops the server before the platform is stopped. This filter is only a safeguard for rare cases in
 * which the platform has been stopped early while the server is still serving requests (e.g. by a direct call to
 * {@link IPlatform#stop()}), or in which the platform became {@link IPlatform.State#PlatformInvalid} because of an
 * error.
 */
public class PlatformStateFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(PlatformStateFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletResponse resp = (HttpServletResponse) response;

    final IPlatform platform = Platform.peek();
    final State state = platform != null ? platform.getState() : null;
    if (State.PlatformStarted != state) {
      LOG.debug("Abort HTTP request with status code {}, because platform state {} is not {}. Request={}", HttpServletResponse.SC_SERVICE_UNAVAILABLE, state, State.PlatformStarted, request);
      resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
      return;
    }

    chain.doFilter(request, response);
  }
}
