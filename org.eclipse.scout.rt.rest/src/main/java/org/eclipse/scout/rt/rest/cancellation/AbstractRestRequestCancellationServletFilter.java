/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.cancellation;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.rest.RestHttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet filter that registers and unregisters requests currently being executed so that they can be cancelled. The
 * identification of a user (see {@link #resolveUserId(HttpServletRequest)}) must be implemented by sub classes.
 */
public abstract class AbstractRestRequestCancellationServletFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractRestRequestCancellationServletFilter.class);

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // nothing to init
  }

  @Override
  public void destroy() {
    // nothing to destroy
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final IRegistrationHandle handle = registerRunMonitor((HttpServletRequest) request);
    try {
      chain.doFilter(request, response);
    }
    finally {
      if (handle != null) {
        handle.dispose();
      }
    }
  }

  protected IRegistrationHandle registerRunMonitor(HttpServletRequest request) {
    String requestId = request.getHeader(RestHttpHeaders.REQUEST_ID);
    if (requestId == null) {
      LOG.trace("cancellation not supported by this request: HTTP header '" + RestHttpHeaders.REQUEST_ID + "' is missing");
      return null;
    }

    RunContext runContext = RunContext.CURRENT.get();
    if (runContext == null) {
      LOG.trace("cancellation not supported by this request: not running within a run context");
      return null;
    }

    Object userId = resolveUserId(request);
    return getCancellationRegistry().register(requestId, userId, runContext.getRunMonitor());
  }

  /**
   * Returns the user id of the given request. May be {@code null}.
   */
  protected abstract Object resolveUserId(HttpServletRequest request);

  /**
   * Returns the cancellation registry that manages requests passing this filter (e.g. if different entry points are
   * managed independently). The default is {@link RestRequestCancellationRegistry}.
   */
  protected RestRequestCancellationRegistry getCancellationRegistry() {
    return BEANS.get(RestRequestCancellationRegistry.class);
  }
}
