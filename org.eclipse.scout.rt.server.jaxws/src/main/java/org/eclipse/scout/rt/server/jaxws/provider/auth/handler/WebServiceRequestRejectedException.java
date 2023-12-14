/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.jaxws.provider.auth.handler;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.http.HTTPException;

/**
 * Exception to be thrown, if the call chain should be exit with a {@link HTTPException}.
 * <p>
 * This is used because some JAX-WS implementors (like METRO v2.2.10) do not exit the call chain if the {@link Handler}
 * returns with <code>false</code>. That happens for one-way communication requests. As a result, the endpoint operation
 * is still invoked.
 *
 * @since 5.2
 */
public class WebServiceRequestRejectedException extends Exception {

  private static final long serialVersionUID = 1L;

  private final int m_httpStatusCode;

  /**
   * @param httpStatusCode
   *          HTTP status code to be set in {@link HTTPException}.
   */
  public WebServiceRequestRejectedException(final int httpStatusCode) {
    m_httpStatusCode = httpStatusCode;
  }

  public int getHttpStatusCode() {
    return m_httpStatusCode;
  }
}
