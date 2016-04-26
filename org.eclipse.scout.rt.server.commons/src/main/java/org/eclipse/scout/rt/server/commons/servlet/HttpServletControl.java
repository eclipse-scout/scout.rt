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

  public static final String HTTP_HEADER_CSP = "Content-Security-Policy";
  public static final String HTTP_HEADER_CSP_LEGACY = "X-Content-Security-Policy";

  /**
   * <ul>
   * <li><b>default-src 'self'</b><br>
   * Only accept 'self' sources by default.</li>
   * <li><b>script-src 'self' 'unsafe-inline' 'unsafe-eval'</b><br>
   * Unsafe-inline is necessary for the bootstrapping process (index.html uses an inline script block).<br>
   * Unsafe-eval is necessary for the number field.</li>
   * <li><b>style-src 'self' 'unsafe-inline'</b><br>
   * Without inline styling many widgets would not work as expected.</li>
   * <li><b>frame-src *; child-src *</b><br>
   * Everything is allowed because the iframes created by the browser field run in the sandbox mode and therefore handle
   * the security policy by their own.</li>
   * <li><b>report-uri csp.cgi</b><br>
   * Report errors to csp.cgi, see ContentSecurityPolicyReportHandler</li>
   * </ul>
   */
  public static final String DEFAULT_CSP_RULE = "default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; frame-src *; child-src *; report-uri csp.cgi";

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
    resp.setHeader(HTTP_HEADER_CSP_LEGACY, cspRule());
  }

  /**
   * see also ContentSecurityPolicyReportHandler
   */
  protected String cspRule() {
    return DEFAULT_CSP_RULE;
  }

}
