/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.server.commons.servlet.logging;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.slf4j.MDC;

/**
 * This class provides the {@link HttpServletRequest#getRequestURI()} to be set into the
 * <code>diagnostic context map</code> for logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 * @since 5.1
 */
public class HttpRequestUriContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "http.request.uri";
  private final String m_requestUri;

  public HttpRequestUriContextValueProvider(String requestUri) {
    m_requestUri = requestUri;
  }

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    return m_requestUri;
  }
}
