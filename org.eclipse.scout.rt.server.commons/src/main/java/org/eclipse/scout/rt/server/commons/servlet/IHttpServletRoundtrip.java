/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interface to hold information about an ongoing Servlet call.
 */
public interface IHttpServletRoundtrip {

  /**
   * The {@link HttpServletRequest} which is currently associated with the current thread.
   */
  ThreadLocal<HttpServletRequest> CURRENT_HTTP_SERVLET_REQUEST = new ThreadLocal<>();

  /**
   * The {@link HttpServletResponse} which is currently associated with the current thread.
   */
  ThreadLocal<HttpServletResponse> CURRENT_HTTP_SERVLET_RESPONSE = new ThreadLocal<>();
}
