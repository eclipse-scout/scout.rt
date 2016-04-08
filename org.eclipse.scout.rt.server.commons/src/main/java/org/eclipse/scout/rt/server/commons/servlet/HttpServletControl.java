/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.server.commons.servlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * Add default (security) handling to servlets
 * <p>
 * Make sure to call {@link #doDefaults(HttpServletRequest, HttpServletResponse)} in every servlet at the beginning of
 * each doGet and doPost
 *
 * @since 5.2
 */
@ApplicationScoped
public class HttpServletControl {
  public static final String HTTP_HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
  public static final String SAMEORIGIN = "SAMEORIGIN";

  public static final String HTTP_HEADER_X_XSS_PROTECTION = "X-XSS-Protection";
  public static final String XSS_MODE_BLOCK = "1; mode=block";

  public static final String HTTP_HEADER_CSP = "Content-Security-Policy";//final version, mozilla und firefox
  public static final String HTTP_HEADER_CSP_LEGACY_CHROME = "X-WebKit-CSP";//chrome
  public static final String HTTP_HEADER_CSP_LEGACY_IE = "X-Content-Security-Policy";//ie
  private static final String TYPICAL_CSP_RULE = "allow self; object-src 'self'; options inlinescript; report-uri csp.cgi";//see ContentSecurityPolicyReportHandler

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
    UrlHints.updateHints(req);
  }

  protected void setResponseHeaders(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader(HTTP_HEADER_X_FRAME_OPTIONS, SAMEORIGIN);
    resp.setHeader(HTTP_HEADER_X_XSS_PROTECTION, XSS_MODE_BLOCK);
    resp.setHeader(HTTP_HEADER_CSP, cspRule());
    resp.setHeader(HTTP_HEADER_CSP_LEGACY_IE, cspRule());
    resp.setHeader(HTTP_HEADER_CSP_LEGACY_CHROME, cspRule());
  }

  /**
   * see also ContentSecurityPolicyReportHandler
   */
  protected String cspRule() {
    return TYPICAL_CSP_RULE;
  }

}
