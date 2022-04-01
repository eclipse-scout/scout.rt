/*
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
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
  public static final String PROXY_PASSWORD = "proxyPassword";
}
