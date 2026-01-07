/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html.res.loader;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletResponse;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.security.csp.ConfigurableContentSecurityPolicy;
import org.eclipse.scout.rt.security.csp.ContentSecurityPolicy;

@ApplicationScoped
public class HtmlContentSecurityPolicyApplier {

  private final Map<String /* pathInfo */, ContentSecurityPolicy> m_cspCache = new ConcurrentHashMap<>();

  public void applyCsp(String pathInfo, String nonce, HttpServletResponse response) {
    if (!BEANS.get(ContentSecurityPolicy.class).isEnabled(pathInfo)) {
      return;
    }
    if (response.isCommitted()) {
      return;
    }
    response.setHeader(ContentSecurityPolicy.HTTP_HEADER, buildToken(pathInfo, nonce));
  }

  public String buildToken(String pathInfo, String nonce) {
    return build(pathInfo, nonce).toToken();
  }

  public ContentSecurityPolicy build(String pathInfo, String nonce) {
    return getConfiguredCspForPath(pathInfo).appendScriptSrc("'nonce-" + nonce + "'");
  }

  protected ContentSecurityPolicy getConfiguredCspForPath(String pathInfo) {
    return m_cspCache.computeIfAbsent(pathInfo, this::computeConfiguredCspForPath).copy();
  }

  protected ContentSecurityPolicy computeConfiguredCspForPath(String pathInfo) {
    return BEANS.get(ConfigurableContentSecurityPolicy.class)
        .initForPath(pathInfo) // load config for current path
        .removeExpression(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, ContentSecurityPolicy.EXPRESSION_SELF) // 'self' makes no sense if using nonces
        .removeExpression(ContentSecurityPolicy.DIRECTIVE_SCRIPT_SRC, ContentSecurityPolicy.EXPRESSION_UNSAFE_INLINE); // If a directive contains nonce expressions, then the unsafe-inline keyword is ignored by browsers
  }
}
