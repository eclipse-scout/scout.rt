/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest.jersey;

/**
 * Request parameter names used by {@link RestClientHttpProxyServlet}.
 */
public final class ProxyServletParameters {

  private ProxyServletParameters() {
  }

  /**
   * Boolean parameter controlling whether to require proxy authentication.
   */
  public static final String REQUIRE_AUTH = "requireAuth";

  /**
   * String parameter providing proxy username.
   */
  public static final String PROXY_USER = "proxyUser";

  /**
   * String parameter providing proxy password.
   */
  @SuppressWarnings("squid:S2068")
  public static final String PROXY_PASSWORD = "proxyPassword";
}
