/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.csp;

import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.util.IOUtility;
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
 * not possible you can adjust the CSP rule to your own needs by replacing {@link HttpServletControl} and overriding
 * {@link HttpServletControl#getCspDirectives()}.
 * <p>
 * see {@link HttpServletControl}
 *
 * @since 5.2
 */
public class ContentSecurityPolicyReportHandler extends AbstractUiServletRequestHandler {
  private static final Logger LOG = LoggerFactory.getLogger(ContentSecurityPolicyReportHandler.class);

  private static final String HANDLER_PATH = "/" + HttpServletControl.CSP_REPORT_URL;

  private static final int MAX_CSP_REPORT_DATALENGTH = 4 * 1024;

  @Override
  public boolean handlePost(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    // serve only if path is ending with /csp-report
    String pathInfo = req.getPathInfo();
    if (pathInfo == null || !pathInfo.endsWith(HANDLER_PATH)) {
      return false;
    }

    String cspReportData;
    try (Reader in = req.getReader()) {
      cspReportData = IOUtility.readString(in, MAX_CSP_REPORT_DATALENGTH);
      if (in.read() != -1) {
        cspReportData += "... [only first " + MAX_CSP_REPORT_DATALENGTH + " bytes shown]";
      }
    }

    LOG.warn("CSP-REPORT: {}", cspReportData);
    return true;
  }

}
