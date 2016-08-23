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
package org.eclipse.scout.rt.server.commons.servlet.logging;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
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
@ApplicationScoped
public class HttpRequestUriContextValueProvider implements IDiagnosticContextValueProvider, IServletRunContextDiagnostics {

  public static final String KEY = "http.request.uri";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    return IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get().getRequestURI();
  }
}
