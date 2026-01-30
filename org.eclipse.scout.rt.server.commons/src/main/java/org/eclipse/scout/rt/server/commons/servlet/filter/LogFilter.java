/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
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
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Filter} that logs all servlet requests after the request has been processed.
 */
public class LogFilter implements Filter {

  private static final Logger LOG = LoggerFactory.getLogger(LogFilter.class);

  /**
   * Name of the attribute that is used by the current request to indicate to skip logging the request. The attribute value is irrelevant.
   * <b>Note:</b> Keep in sync with {@link org.eclipse.scout.rt.rest.log.NoLogFilter#NO_LOG_REQUEST_ATTRIBUTE}
   */
  public static final String NO_LOG_REQUEST_ATTRIBUTE = "scout.noLog";

  @Override
  public void init(FilterConfig filterConfig) {
    // nothing to initialize
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    long t0 = System.nanoTime();
    chain.doFilter(httpRequest, httpResponse);
    if (isLoggable(httpRequest, httpResponse)) {
      log(httpRequest, httpResponse, System.nanoTime() - t0);
    }
  }

  protected boolean isLoggable(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    if (httpRequest == null || httpRequest.getAttribute(NO_LOG_REQUEST_ATTRIBUTE) != null) {
      return false;
    }
    return true;
  }

  protected void log(HttpServletRequest httpRequest, HttpServletResponse httpResponse, long durationNanos) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("[{}] {} {}{} took {} ms [User-Agent: {}; Accept-Language: {}, Referer: {}; Remote-Addr: {}; X-Forwarded-For: {}]",
          httpResponse.getStatus(),
          httpRequest.getMethod(),
          urlDecode(httpRequest.getRequestURL().toString()),
          httpRequest.getQueryString() == null ? "" : "?" + urlDecode(httpRequest.getQueryString()),
          StringUtility.formatNanos(durationNanos),
          ObjectUtility.nvl(httpRequest.getHeader("User-Agent"), "-"),
          ObjectUtility.nvl(httpRequest.getHeader("Accept-Language"), "-"),
          ObjectUtility.nvl(httpRequest.getHeader("Referer"), "-"),
          ObjectUtility.nvl(httpRequest.getRemoteAddr(), "-"),
          ObjectUtility.nvl(httpRequest.getHeader("X-Forwarded-For"), "-"));
    }
    else if (LOG.isInfoEnabled()) {
      LOG.info("[{}] {} {} took {} ms",
          httpResponse.getStatus(),
          httpRequest.getMethod(),
          urlDecode(httpRequest.getRequestURL().toString()),
          StringUtility.formatNanos(durationNanos));
    }
  }

  protected String urlDecode(String s) { // separate method to allow customization
    return IOUtility.urlDecode(s);
  }

  @Override
  public void destroy() {
    // nothing to destroy
  }
}
