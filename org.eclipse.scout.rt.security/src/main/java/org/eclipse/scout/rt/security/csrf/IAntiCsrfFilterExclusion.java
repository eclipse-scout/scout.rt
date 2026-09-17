/*
 * Copyright (c) 2010, 2026 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.security.csrf;

import org.eclipse.scout.rt.platform.ApplicationScoped;

/**
 * implement an instance of this filter to add custom exclusions
 *
 * @see AntiCsrfHelper#isExcludedRequest(String, String)
 */
@ApplicationScoped
public interface IAntiCsrfFilterExclusion {

  /**
   * Determines whether anti-CSRF validation should be skipped for the
   * specified request.
   *
   * @param method
   *     the HTTP method of the request
   * @param path
   *     the request path
   * @return {@code true} if the request should be excluded from anti-CSRF
   * validation; otherwise {@code false}
   */
  boolean isIgnored(String method, String path);
}
