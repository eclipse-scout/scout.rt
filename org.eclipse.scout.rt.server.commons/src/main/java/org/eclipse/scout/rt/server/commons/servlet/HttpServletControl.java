/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.security.csp.BlockAllContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
  private static final Logger LOG = LoggerFactory.getLogger(HttpServletControl.class);

  public static final String HTTP_HEADER_X_FRAME_OPTIONS = "X-Frame-Options";
  public static final String SAMEORIGIN = "SAMEORIGIN";
  public static final int MAX_CSP_REPORT_DATA_LENGTH = 8 * 1024;

  /**
   * The X-Content-Type-Options response HTTP header is a marker used by the server to indicate that the MIME types
   * advertised in the Content-Type headers should not be changed and be followed. This allows to opt-out of MIME type
   * sniffing, or, in other words, it is a way to say that the webmasters knew what they were doing.
   * See <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Content-Type-Options">MDN</a>.
   */
  public static final String HTTP_HEADER_X_CONTENT_TYPE_OPTIONS = "X-Content-Type-Options";
  public static final String CONTENT_TYPE_OPTION_NO_SNIFF = "nosniff";

  // Content Security Policy (CSP) token (built only once to eliminate overhead with each request)
  private String m_cspToken;

  @PostConstruct
  protected void buildCspPolicyToken() {
    setCspToken(BEANS.get(BlockAllContentSecurityPolicy.class).toToken());
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
   *     might be {@code null}
   * @param req
   *     must not be {@code null}
   * @param resp
   *     must not be {@code null}
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

    if ("GET".equals(req.getMethod())) {
      resp.setHeader(HTTP_HEADER_X_FRAME_OPTIONS, SAMEORIGIN);
    }

    if (isCspEnabled(req)) {
      // apply csp for all methods in case text/html is returned
      resp.setHeader(ContentSecurityPolicy.HTTP_HEADER, getCspToken());
    }
  }

  public boolean isCspEnabled(HttpServletRequest req) {
    String pathInfo = req.getPathInfo();
    return BEANS.get(ContentSecurityPolicy.class).isEnabled(pathInfo);
  }

  /**
   * @see #getCspReport(HttpServletRequest, int)
   */
  public String getCspReport(HttpServletRequest req) throws IOException {
    return getCspReport(req, MAX_CSP_REPORT_DATA_LENGTH);
  }

  /**
   * Reads a CSP report JSON from the given {@link HttpServletRequest}.
   *
   * @param req
   *     The request to read the JSON (sent by browsers using POST).
   * @param maxLen
   *     The max length of the data to read.
   * @return The JSON sent by the browser.
   * @throws IOException
   *     In case there is an I/O error reading from the request.
   */
  public String getCspReport(HttpServletRequest req, int maxLen) throws IOException {
    try (Reader in = req.getReader()) {
      String cspReportData = IOUtility.readString(in, maxLen);
      if (in.read() != -1) {
        cspReportData += "... [only first " + maxLen + " bytes shown]";
      }
      else {
        // Format JSON if json is complete
        try {
          JSONObject json = new JSONObject(cspReportData);
          cspReportData = json.toString(2);
        }
        catch (RuntimeException e) {
          LOG.trace("Error while converting CSP report to JSON", e);
        }
      }
      return cspReportData;
    }
  }
}
