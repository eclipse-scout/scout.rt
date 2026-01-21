/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.csp;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.Order;
import org.eclipse.scout.rt.platform.util.StringUtility;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.ui.html.AbstractUiServletRequestHandler;
import org.eclipse.scout.rt.ui.html.UiServlet;
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

  private static final String HANDLER_PATH = "/" + ContentSecurityPolicy.REPORT_URL;

  @Override
  public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    // serve if ending with /csp-report (so that it automatically works in subfolders)
    if (!StringUtility.endsWith(req.getPathInfo(), HANDLER_PATH)) {
      return false;
    }
    logReport(req);
    return true;
  }

  protected void logReport(HttpServletRequest req) throws IOException {
    if (LOG.isInfoEnabled()) {
      LOG.info("CSP-REPORT: {}", getReport(req));
    }
  }

  protected String getReport(HttpServletRequest req) throws IOException {
    return BEANS.get(HttpServletControl.class).getCspReport(req);
  }
}
