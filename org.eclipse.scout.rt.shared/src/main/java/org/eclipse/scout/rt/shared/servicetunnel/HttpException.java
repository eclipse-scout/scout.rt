/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.servicetunnel;

public class HttpException extends Exception {
  private static final long serialVersionUID = 1L;

  private int m_statusCode;

  public static String getNameFor(int statusCode) {
    switch (statusCode) {
      case 400:
        return "BAD_REQUEST";
      case 401:
        return "UNAUTHORIZED";
      case 402:
        return "PAYMENT_REQUIRED";
      case 403:
        return "FORBIDDEN";
      case 404:
        return "NOT_FOUND";
      case 405:
        return "METHOD_NOT_ALLOWED";
      case 406:
        return "NOT_ACCEPTABLE";
      case 407:
        return "PROXY_AUTHENTICATION_REQUIRED";
      case 408:
        return "REQUEST_TIMEOUT";
      case 409:
        return "CONFLICT";
      case 410:
        return "GONE";
      case 411:
        return "LENGTH_REQUIRED";
      case 412:
        return "PRECONDITION_FAILED";
      case 413:
        return "REQUEST_ENTITY_TOO_LARGE";
      case 414:
        return "REQUEST_URI_TOO_LONG";
      case 415:
        return "UNSUPPORTED_MEDIA_TYPE";
      case 416:
        return "REQUESTED_RANGE_NOT_SATISFIABLE";
      case 417:
        return "EXPECTATION_FAILED";
      case 500:
        return "INTERNAL_SERVER_ERROR";
      case 501:
        return "NOT_IMPLEMENTED";
      case 502:
        return "BAD_GATEWAY";
      case 503:
        return "SERVICE_UNAVAILABLE";
      case 504:
        return "GATEWAY_TIMEOUT";
      case 505:
        return "HTTP_VERSION_NOT_SUPPORTED";
      default:
        return "?";
    }
  }

  public HttpException(int statusCode) {
    super("" + statusCode + " - " + getNameFor(statusCode));
    m_statusCode = statusCode;
  }

  public int getStatusCode() {
    return m_statusCode;
  }
}
