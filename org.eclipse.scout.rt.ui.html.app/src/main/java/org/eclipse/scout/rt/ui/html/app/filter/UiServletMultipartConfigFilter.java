/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.app.filter;

import java.io.IOException;
import java.util.regex.Matcher;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletHolder.Registration;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiServletMultipartConfigProperty;
import org.eclipse.scout.rt.ui.html.json.UploadRequestHandler;

/**
 * Filter class to add a multipart config for the {@link HttpServletRequest} if this filter determines it is necessary.
 * <p>
 * We must not add this multipart config to all requests (to the servlet holder) as otherwise reading parameters may
 * consume the request payload (to determine parameters) which makes other body reading methods e.g.
 * {@link HttpServletRequest#getInputStream()} unusable.
 *
 * @see <a href="https://github.com/jetty/jetty.project/issues/1175">jetty/jetty.project issue #1175</a>
 * @see HttpServletRequest#getParameter(String)
 * @see Request#extractContentParameters()
 * @see Registration#setMultipartConfig(MultipartConfigElement)
 * @see ServletHolder#prepare(Request, ServletRequest, ServletResponse)
 */
public class UiServletMultipartConfigFilter implements Filter {

  @Override
  public void doFilter(ServletRequest req0, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
    addMultipartConfigIfRequired(req0);
    chain.doFilter(req0, resp);
  }

  protected void addMultipartConfigIfRequired(ServletRequest req0) {
    if (!(req0 instanceof HttpServletRequest)) {
      return;
    }

    HttpServletRequest req = (HttpServletRequest) req0;

    if (isMultipartConfigRequired(req)) {
      req.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, CONFIG.getPropertyValue(UiServletMultipartConfigProperty.class));
    }
  }

  protected boolean isMultipartConfigRequired(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null) {
      return false;
    }

    Matcher matcher = UploadRequestHandler.PATTERN_UPLOAD_ADAPTER_RESOURCE_PATH.matcher(pathInfo);
    return matcher.matches();
  }
}
