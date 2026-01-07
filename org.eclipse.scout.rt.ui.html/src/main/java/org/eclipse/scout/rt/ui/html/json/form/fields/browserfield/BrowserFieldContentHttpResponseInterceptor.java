/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.json.form.fields.browserfield;

import static org.eclipse.scout.rt.platform.util.Assertions.assertNotNull;

import java.io.Serial;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserField;
import org.eclipse.scout.rt.client.ui.form.fields.browserfield.IBrowserFieldUIFacade;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.resource.BinaryResource;
import org.eclipse.scout.rt.security.csp.ConfigurableContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;
import org.eclipse.scout.rt.server.commons.servlet.cache.IHttpResponseInterceptor;

public class BrowserFieldContentHttpResponseInterceptor implements IHttpResponseInterceptor {
  @Serial
  private static final long serialVersionUID = 1L;

  private final BinaryResource m_res;
  private final IBrowserField m_browserField;

  public BrowserFieldContentHttpResponseInterceptor(IBrowserField browserField, BinaryResource res) {
    m_res = assertNotNull(res);
    m_browserField = assertNotNull(browserField);
  }

  @Override
  public void intercept(HttpServletRequest req, HttpServletResponse resp) {
    ContentSecurityPolicy policy = getContentSecurityPolicy();
    resp.setHeader(ContentSecurityPolicy.HTTP_HEADER, policy.toToken());
  }

  protected ContentSecurityPolicy getContentSecurityPolicy() {
    // 1. use BinaryResource specific CSP if available
    IBrowserFieldUIFacade uiFacade = m_browserField.getUIFacade();
    ContentSecurityPolicy policy = uiFacade.getContentSecurityPolicy(m_res.getFilename());
    if (policy != null) {
      return policy;
    }

    // 2. use default policy of this BrowserField if available
    policy = uiFacade.getContentSecurityPolicy(null /* customized default csp for all resources of this field */);
    if (policy != null) {
      return policy;
    }

    // 3. use application wide default
    return BEANS.get(ConfigurableContentSecurityPolicy.class);
  }
}
