/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.servicetunnel.http;

import org.eclipse.scout.rt.platform.exception.RemoteSystemUnavailableException;

/**
 * Exception thrown in case of an unsuccessful communication through {@link HttpServiceTunnel}, e.g. HTTP status code
 * not between 200 and 299.
 */
public class HttpServiceTunnelException extends RemoteSystemUnavailableException {

  private static final long serialVersionUID = 1L;

  /**
   * HTTP status code
   */
  private final int m_httpStatus;

  public HttpServiceTunnelException(String message, Object... args) {
    this(0, message, args);
  }

  public HttpServiceTunnelException(int httpStatus, String message, Object... args) {
    super(message, args);
    m_httpStatus = httpStatus;
  }

  public int getHttpStatus() {
    return m_httpStatus;
  }
}
