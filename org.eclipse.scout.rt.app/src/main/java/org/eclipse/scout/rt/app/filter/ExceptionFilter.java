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
import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.jetty.io.QuietException;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.CorrelationId;
import org.eclipse.scout.rt.platform.context.CorrelationIdContextValueProvider;
import org.eclipse.scout.rt.platform.exception.DefaultExceptionTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Filter that catches Exceptions thrown by consecutive filters or servlet and that logs them along with the correlation
 * id if available. Logging the correlation id is the main difference to the logging otherwise performed by Jetty.
 */
public class ExceptionFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(ExceptionFilter.class);

  @Override
  public void init(FilterConfig filterConfig) {
    // nothing to initialize
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    try {
      chain.doFilter(request, response);
    }
    catch (Exception e) {
      if (isCausedByQuietException(e)) {
        throw e;
      }

      HttpServletRequest req = (HttpServletRequest) request;
      MDC.put(CorrelationIdContextValueProvider.KEY, req.getHeader(CorrelationId.HTTP_HEADER_NAME));
      try {
        LOG.warn(req.getRequestURI(), e);
      }
      finally {
        MDC.remove(CorrelationIdContextValueProvider.KEY);
      }
      throw new JettyQuietExceptionWrapper(e);
    }
  }

  /**
   * @return {@code true} if the given Throwable is a {@link QuietException} or wraps one. Otherwise {@code false}.
   */
  protected boolean isCausedByQuietException(Throwable t) {
    return BEANS.get(DefaultExceptionTranslator.class).throwableCausesAccept(t, e -> e instanceof QuietException);
  }

  @Override
  public void destroy() {
  }

  /**
   * {@link QuietException}s are not logged by Jetty unless the log level of {@link HttpChannel} is DEBUG.
   */
  public static class JettyQuietExceptionWrapper extends RuntimeException implements QuietException {
    private static final long serialVersionUID = 1L;

    public JettyQuietExceptionWrapper(Throwable cause) {
      super(cause);
    }
  }
}
