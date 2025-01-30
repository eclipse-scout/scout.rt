/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;

/**
 * Filter which prevents creating a HTTP session for requests, e.g. for stateless REST resources
 */
public class EnforceNoHttpSessionFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) {
    // nothing to initialize
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    chain.doFilter(wrapRequest(request), response);
  }

  @Override
  public void destroy() {
    // nothing to destroy
  }

  protected HttpServletRequest wrapRequest(ServletRequest request) {
    return new SessionlessHttpServletRequestWrapper((HttpServletRequest) request);
  }

  public static class SessionlessHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public SessionlessHttpServletRequestWrapper(HttpServletRequest request) {
      super(request);
    }

    @Override
    public HttpSession getSession() {
      throw new UnsupportedOperationException("HTTP session not allowed");
    }

    @Override
    public HttpSession getSession(boolean create) {
      if (!create) {
        return null;
      }
      throw new UnsupportedOperationException("HTTP session not allowed");
    }
  }
}
