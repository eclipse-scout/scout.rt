/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.UriUtility;
import org.eclipse.scout.rt.server.commons.servlet.ContentSecurityPolicy;
import org.eclipse.scout.rt.server.commons.servlet.HttpClientInfo;
import org.eclipse.scout.rt.server.commons.servlet.HttpServletControl;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;
import org.eclipse.scout.rt.ui.html.IUiSession;

public class BrowserFieldContentHttpResponseInterceptor implements IHttpResponseInterceptor {
  private static final long serialVersionUID = 1L;

  private final URI m_browserUri;

  public BrowserFieldContentHttpResponseInterceptor(IUiSession uiSession) {
    m_browserUri = uiSession.getClientSession().getBrowserURI();
  }

  @Override
  public void intercept(HttpServletRequest req, HttpServletResponse resp) {
    ContentSecurityPolicy csp = BEANS.get(ContentSecurityPolicy.class).appendScriptSrc("'unsafe-inline'");

    HttpClientInfo httpClientInfo = HttpClientInfo.get(req);
    String baseUri = UriUtility.toBaseUri(m_browserUri);

    if (baseUri != null) {
      // Normally, the csp report url is relative. Because documents inside the browser field are
      // loaded from a "/dynamic/..." URL, the relative url has to be converted to an absolute url.
      csp.withReportUri(baseUri + HttpServletControl.CSP_REPORT_URL);

      // Bug in Chrome: CSP 'self' is not interpreted correctly in sandboxed iframes, see https://bugs.chromium.org/p/chromium/issues/detail?id=443444
      // Workaround: Add resolved URI to image and style CSP directive to allow loading of images and styles from same origin as nested iframe in browser field
      if (httpClientInfo.isWebkit()) {
        csp.appendImgSrc(baseUri);
        csp.appendStyleSrc(baseUri);
      }
    }

    String cspToken = csp.toToken();
    if (httpClientInfo.isMshtml()) {
      resp.setHeader(HttpServletControl.HTTP_HEADER_CSP_LEGACY, cspToken);
    }
    else {
      resp.setHeader(HttpServletControl.HTTP_HEADER_CSP, cspToken);
    }
  }
}
