/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.cache;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor that is called before the HTTP response is sent back to the client. This can be useful to alter the
 * answer, e.g. to add HTTP response headers.
 */
@FunctionalInterface
public interface IHttpResponseInterceptor extends Serializable {

  /**
   * Called before the HTTP response is sent back to the client.
   */
  void intercept(HttpServletRequest req, HttpServletResponse resp);
}
