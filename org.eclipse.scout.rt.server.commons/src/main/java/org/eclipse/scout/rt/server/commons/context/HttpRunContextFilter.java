/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.context;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
