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
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.LazyValue;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.csrf.AntiCsrfHelper;

public class AntiCsrfFilter implements Filter {
  private final LazyValue<AntiCsrfHelper> m_requestWithHelper = new LazyValue<>(AntiCsrfHelper.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    HttpServletResponse httpResponse = (HttpServletResponse) response;

    boolean isValid = m_requestWithHelper.get().isValidRequest(
        headerName -> StringUtility.hasText(httpRequest.getHeader(headerName)),
        httpRequest.getMethod(),
        httpRequest.getPathInfo()
    );

    if (isValid) {
      chain.doFilter(request, response);
    }
    else {
      httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
  }
}
