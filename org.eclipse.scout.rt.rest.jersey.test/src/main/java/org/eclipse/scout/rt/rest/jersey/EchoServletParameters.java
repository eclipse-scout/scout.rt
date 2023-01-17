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
 * Request parameter names used by {@link RestClientTestEchoServlet}.
 */
public final class EchoServletParameters {

  private EchoServletParameters() {
  }

  /**
   * HTTP status code to be returned.
   */
  public static final String STATUS = "status";

  /**
   * HTTP request identifier used for synchronization (i.e. {@link RequestSynchronizer}).
   */
  public static final String REQUEST_ID = "requestId";

  /**
   * Number of seconds to sleep before request is actually processed.
   */
  public static final String SLEEP_SEC = "sleepSec";

  /**
   * Boolean parameter controlling whether to return an empty body.
   */
  public static final String EMPTY_BODY = "emptyBody";

  /**
   * Boolean parameter controlling whether to return a large response message. Only supported for successful responses
   * (i.e. requested {@link #STATUS} &lt; 300).
   */
  public static final String LARGE_MESSAGE = "largeMessage";

  /**
   * String parameter, if available and not null, its value is added as cookie to the response.
   */
  public static final String COOKIE_VALUE = "cookieValue";

  /**
   * String parameter controlling whether to return a redirection to given URL.
   */
  public static final String REDIRECT_URL = "redirectUrl";
}
