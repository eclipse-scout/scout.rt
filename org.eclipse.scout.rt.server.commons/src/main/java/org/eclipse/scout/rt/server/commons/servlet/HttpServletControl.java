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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.util.StringUtility;

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

  public static final String CSP_REPORT_URL = "csp.cgi";

  private String m_cspValue;

  @PostConstruct
  protected void buildCspValue() {
    // build csp rule only once to eliminate overhead with each request
    List<String> cspDirectives = new ArrayList<>();
    for (Entry<String, String> entry : getCspDirectives().entrySet()) {
      cspDirectives.add(StringUtility.join(" ", entry.getKey(), entry.getValue()));
    }

    m_cspValue = StringUtility.join("; ", cspDirectives);
  }

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
   * Override this method to add new or change / remove existing directives.
   */
  protected Map<String, String> getCspDirectives() {
    Map<String, String> cspDirectives = new LinkedHashMap<>();
    cspDirectives.put("default-src", "'self'");
    cspDirectives.put("script-src", "'self' 'unsafe-inline' 'unsafe-eval'");
    cspDirectives.put("style-src", "'self' 'unsafe-inline'");
    cspDirectives.put("frame-src", "*");
    cspDirectives.put("child-src", "*");
    cspDirectives.put("report-uri", CSP_REPORT_URL); // see also ContentSecurityPolicyReportHandler
    return cspDirectives;
  }

  /**
   * Override {@link #getCspDirectives()} to add new or change / remove existing directives.
   */
  public String getCspValue() {
    return cspRule();
  }

  /**
   * @deprecated Use {@link #getCspValue()} to get value, override {@link #getCspDirectives()} to add own directives.
   *             Will be removed with 6.1.
   */
  @Deprecated
  protected String cspRule() {
    return m_cspValue;
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
    UrlHints.updateHints(req);
  }

  protected void setResponseHeaders(HttpServlet servlet, HttpServletRequest req, HttpServletResponse resp) {
    resp.setHeader(HTTP_HEADER_X_FRAME_OPTIONS, SAMEORIGIN);
    resp.setHeader(HTTP_HEADER_X_XSS_PROTECTION, XSS_MODE_BLOCK);

    resp.setHeader(HTTP_HEADER_CSP, getCspValue());
    resp.setHeader(HTTP_HEADER_CSP_LEGACY, getCspValue());
  }
}
