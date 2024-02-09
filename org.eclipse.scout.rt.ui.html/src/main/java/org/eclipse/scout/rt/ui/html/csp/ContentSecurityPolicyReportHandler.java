/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.csp;

import java.io.IOException;
import java.io.Reader;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.server.commons.servlet.ContentSecurityPolicy;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.UiServlet;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This handler contributes to the {@link UiServlet} as the POST handler for <code>/csp-report</code>
 * <p>
 * It is used to collect Content-Security-Policy violations.
 * <p>
 * If you get a violation for content you need, make sure all your content is provided from the same origin. If this is
 * not possible you can change the rules by adjusting {@link ContentSecurityPolicy}.
 *
 * @since 5.2
 */
@Order(5400)
public class ContentSecurityPolicyReportHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ContentSecurityPolicyReportHandler.class);

  private static final String HANDLER_PATH = "/" + HttpServletControl.CSP_REPORT_URL;
  private static final int MAX_CSP_REPORT_DATALENGTH = 4 * 1024;

  @Override
  public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    // serve only /csp-report
    if (!ObjectUtility.equals(req.getPathInfo(), HANDLER_PATH)) {
      return false;
    }
    log(getReport(req));
    return true;
  }

  protected String getReport(final HttpServletRequest req) throws IOException {
    try (Reader in = req.getReader()) {
      String cspReportData = IOUtility.readString(in, MAX_CSP_REPORT_DATALENGTH);
      if (in.read() != -1) {
        cspReportData += "... [only first " + MAX_CSP_REPORT_DATALENGTH + " bytes shown]";
      }
      else {
        // Format JSON
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

  protected void log(final String report) {
    LOG.info("CSP-REPORT: {}", report);
  }
}
