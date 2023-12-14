/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.context;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;

/**
 * {@link Filter} that creates a Scout {@link RunContext} based on the {@link HttpServletRequest}.
 *
 * @see HttpRunContextProducer
 * @since 9.0
 */
public class HttpRunContextFilter implements Filter {

  private HttpRunContextProducer m_producer;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_producer = createRunContextProducer();
  }

  protected HttpRunContextProducer createRunContextProducer() {
    return BEANS.get(HttpRunContextProducer.class);
  }

  protected HttpRunContextProducer getRunContextProducer() {
    return m_producer;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest req = (HttpServletRequest) request;
    final HttpServletResponse resp = (HttpServletResponse) response;
    buildRunContext(req, resp).run(() -> chain.doFilter(request, response));
  }

  protected RunContext buildRunContext(HttpServletRequest req, HttpServletResponse resp) {
    return getRunContextProducer().produce(req, resp);
  }

  @Override
  public void destroy() {
    m_producer = null;
  }
}
