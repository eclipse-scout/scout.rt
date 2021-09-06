/*
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.server.commons.servlet.logging;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    providers.add(new HttpRequestUriContextValueProvider(request.getQueryString()));
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
