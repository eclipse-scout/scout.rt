/*
 * Copyright (c) 2010, 2025 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.rest;

public final class RestHttpHeaders {

  private RestHttpHeaders() {
  }

  /**
   * HTTP header name for the request Id.
   */
  public static final String REQUEST_ID = "X-ScoutRequestId";

  /**
   * HTTP header used to mark requests originating from ScoutJS (i.e. proxied REST call).
   * The header value {@code true} (case-insensitive!) is considered proxied. All other
   * values or the absence of the header are considered not-proxied.
   */
  public static final String PROXIED_REQUEST_HTTP_HEADER = "X-ScoutProxiedRequest";
}
