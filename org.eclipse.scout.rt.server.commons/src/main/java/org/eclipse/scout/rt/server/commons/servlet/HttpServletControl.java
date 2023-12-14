/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.CspEnabledProperty;
import org.eclipse.scout.rt.server.commons.ServerCommonsConfigProperties.CspExclusionsProperty;

/**
 * Add default (security) handling to servlets
 * <p>
 * Make sure to call {@link #doDefaults(HttpServlet, HttpServletRequest, HttpServletResponse)} in every servlet at the
 * beginning of each doGet and doPost
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

  /**
   * The X-Content-Type-Options response HTTP header is a marker used by the server to indicate that the MIME types
   * advertised in the Content-Type headers should not be changed and be followed. This allows to opt-out of MIME type
   * sniffing, or, in other words, it is a way to say that the webmasters knew what they were doing. See
   * https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options
   */
  public static final String HTTP_HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
  public static final String CONTENT_TYPE_OPTION_NO_SNIFF = "nosniff";

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
   *
   * @param servlet
   *          might be {@code null}
   * @param req
   *          must not be {@code null}
   * @param resp
   *          must not be {@code null}
   */
  public void doDefaults(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    parseRequest(servlet, req, resp);
    setResponseHeaders(servlet, req, resp);
  }

  protected void parseRequest(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    UrlHints.updateHints(req, resp);
  }

  protected void setResponseHeaders(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader(HTTP_HEADER_X_CONTENT_TYPE_OPTIONS, CONTENT_TYPE_OPTION_NO_SNIFF); // apply no-sniff header for all http-methods

    if (!"GET".equals(req.getMethod())) {
      return;
    }
    resp.setHeader(HTTP_HEADER_X_FRAME_OPTIONS, SAMEORIGIN);
    resp.setHeader(HTTP_HEADER_X_XSS_PROTECTION, XSS_MODE_BLOCK);

    if (isCspEnabled(req)) {
      if (HttpClientInfo.get(req).isMshtml()) {
        resp.setHeader(HTTP_HEADER_CSP_LEGACY, getCspToken());
      }
      else {
        resp.setHeader(HTTP_HEADER_CSP, getCspToken());
      }
    }
  }

  protected boolean isCspEnabled(HttpServletRequest req) {
    if (!CONFIG.getPropertyValue(CspEnabledProperty.class)) {
      return false;
    }
    List<Pattern> exclusions = CONFIG.getPropertyValue(CspExclusionsProperty.class);
    String pathInfo = req.getPathInfo();
    if (exclusions == null || pathInfo == null) {
      return true;
    }
    for (Pattern exclusion : exclusions) {
      if (exclusion.matcher(pathInfo).matches()) {
        return false;
      }
    }
    return true;
  }
}
