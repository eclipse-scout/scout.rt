/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.context;

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
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextFilter;
import org.eclipse.scout.rt.server.commons.context.HttpRunContextProducer;

/**
 * A {@link Filter} that creates a {@link ServerRunContext}.
 * <p>
 * The init-param "session" specifies if an {@link IServerSession} should be created. The default is {@code true}.<br>
 * If {@code true}, the Scout server session will be stored on the HTTP session and will be automatically stopped and
 * removed if the HTTP session is invalidated. This means if session support is enabled a cookie capable HTTP client is
 * required! Furthermore a class implementing {@link IServerSession} must be present on the class-path.
 * <p>
 * Example of a registration via {@link org.eclipse.scout.rt.jetty.IServletFilterContributor}:
 *
 * <pre>
 * public static class ApiServerRunContextFilterContributor implements IServletFilterContributor {
 *
 *   &#064;Override
 *   public void contribute(ServletContextHandler handler) {
 *     FilterHolder filter = handler.addFilter(HttpServerRunContextFilter.class, "/api/*", null);
 *     filter.setInitParameter("session", "false");
 *   }
 * }
 * </pre>
 * <p>
 * Example config for the web.xml:
 *
 * <pre>
 * &lt;filter&gt;
 *   &lt;filter-name&gt;HttpServerRunContextFilter&lt;/filter-name&gt;
 *   &lt;filter-class&gt;org.eclipse.scout.rt.server.context.HttpServerRunContextFilter&lt;/filter-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;session&lt;/param-name&gt;
 *     &lt;param-value&gt;false&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 * &lt;/filter&gt;
 * </pre>
 *
 * @since 9.0
 * @see HttpServerRunContextProducer
 * @see HttpRunContextProducer
 * @see HttpRunContextFilter
 */
public class HttpServerRunContextFilter implements Filter {

  private HttpServerRunContextProducer m_producer;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    m_producer = createRunContextProducer()
        .withSessionSupport(hasSessionSupport(filterConfig));
  }

  protected boolean hasSessionSupport(FilterConfig filterConfig) {
    return !"false".equalsIgnoreCase(filterConfig.getInitParameter("session"));
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

  protected HttpServerRunContextProducer createRunContextProducer() {
    return BEANS.get(HttpServerRunContextProducer.class);
  }

  protected HttpServerRunContextProducer getRunContextProducer() {
    return m_producer;
  }

  @Override
  public void destroy() {
    m_producer = null;
  }
}
