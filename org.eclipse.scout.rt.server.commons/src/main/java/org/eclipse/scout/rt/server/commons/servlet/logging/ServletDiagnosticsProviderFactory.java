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

import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Provides a list of diagnostic context value providers for the given HTTP servlet request and response. Those values
 * are static, i.e. they are strings that remain readable after the request has been completed. No reference to the
 * original request or response object must be held by the diagnostic context value providers.
 */
@ApplicationScoped
public class ServletDiagnosticsProviderFactory {

  public List<IDiagnosticContextValueProvider> getProviders(HttpServletRequest request, HttpServletResponse response) {
    List<IDiagnosticContextValueProvider> providers = new ArrayList<>();
    providers.add(new HttpRequestMethodContextValueProvider(request.getMethod()));
    providers.add(new HttpRequestQueryStringContextValueProvider(request.getQueryString()));
    providers.add(new HttpRequestUriContextValueProvider(request.getRequestURI()));
    providers.add(new HttpSessionIdContextValueProvider(getHttpSessionIdContextValue(request.getSession(false))));
    return providers;
  }

  protected String getHttpSessionIdContextValue(HttpSession session) {
    if (session == null) {
      return null;
    }
    return BEANS.get(HttpSessionIdLogHelper.class).getSessionIdForLogging(session);
  }
}
