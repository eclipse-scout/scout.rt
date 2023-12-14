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

import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.slf4j.MDC;

/**
 * This class provides a HTTP session identifier to be set into the <code>diagnostic context map</code> for logging
 * purpose.
 * <p>
 * <b>Caution!</b> Writing the actual {@linkplain HttpSession#getId() HTTP session ID} to the log file might pose a
 * security risk, since knowledge of the session id can enable attackers to hijack an active session. Consider
 * obfuscating the id first, e.g. using {@link HttpSessionIdLogHelper}.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 * @since 5.1
 */
public class HttpSessionIdContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "http.session.id";
  private final String m_sessionId;

  public HttpSessionIdContextValueProvider(String sessionId) {
    m_sessionId = sessionId;
  }

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    return m_sessionId;
  }
}
