/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.CspEnabledProperty;

/**
 * Add default (security) handling to servlets
 * <p>
 * Make sure to call {@link #doDefaults(HttpServletRequest, HttpServletResponse)} in every servlet at the beginning of
 * each doGet and doPost
 *
 * @since 5.2
 */
@ApplicationScoped
public class HttpServletControl implements Serializable {

  private static final long serialVersionUID = 1L;

  public static final String HTTP_HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
  public static final String SAMEORIGIN = "SAMEORIGIN";

  public static final String HTTP_HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
  public static final String XSS_MODE_BLOCK = "1; mode=block";

  public static final String HTTP_HEADER_CSP = "Content-Security-Policy";

  /** Legacy header for content security policy used by Internet Explorer */
  public static final String HTTP_HEADER_CSP_LEGACY = "X-Content-Security-Policy";

  public static final String CSP_REPORT_URL = "csp-report";

  // Content Security Policy (CSP) token (built only once to eliminate overhead with each request)
  private String m_cspToken;

  @PostConstruct
  protected void buildCspPolicyToken() {
    setCspToken(BEANS.get(ContentSecurityPolicy.class).toToken());
  }

  protected final String getCspToken() {
    return m_cspToken;
  }

  protected final void setCspToken(String cspToken) {
    m_cspToken = cspToken;
  }

  /**
   * Every servlet should call this method to make sure the defaults are applied
   * <p>
   * This includes setting default security response headers, parsing default request attributes etc.
   */
  public void doDefaults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    parseRequest(servlet, req, resp);
    setResponseHeaders(servlet, req, resp);
  }

  protected void parseRequest(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    UrlHints.updateHints(req, resp);
  }

  protected void setResponseHeaders(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    if (!"GET".equals(req.getMethod())) {
      return;
    }
    resp.setHeader(HTTP_HEADER_X_FRAME_OPTIONS, SAMEORIGIN);
    resp.setHeader(HTTP_HEADER_X_XSS_PROTECTION, XSS_MODE_BLOCK);

    if (CONFIG.getPropertyValue(CspEnabledProperty.class)) {
      if (HttpClientInfo.get(req).isMshtml()) {
        resp.setHeader(HTTP_HEADER_CSP_LEGACY, getCspToken());
      }
      else {
        resp.setHeader(HTTP_HEADER_CSP, getCspToken());
      }
    }
  }
}
