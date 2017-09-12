package org.eclipse.scout.rt.server.commons.servlet.logging;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Provides a list of diagnostic context value providers for the given HTTP servlet request and response. Those values
 * are static, i.e. they are strings that remain readable after the request has been completed. No reference to the
 * original request or response object must be held by the diagnostic context value providers.
 */
@ApplicationScoped
public class ServletDiagnosticsProviderFactory {

  public List<? extends IDiagnosticContextValueProvider> getProviders(HttpServletRequest request, HttpServletResponse response) {
    List<IDiagnosticContextValueProvider> providers = new ArrayList<>();
    providers.add(new HttpRequestMethodContextValueProvider(request.getMethod()));
    providers.add(new HttpRequestQueryStringContextValueProvider(request.getQueryString()));
    providers.add(new HttpRequestUriContextValueProvider(request.getQueryString()));
    HttpSession session = request.getSession(false);
    providers.add(new HttpSessionIdContextValueProvider(session != null ? session.getId() : null));
    return providers;
  }
}
