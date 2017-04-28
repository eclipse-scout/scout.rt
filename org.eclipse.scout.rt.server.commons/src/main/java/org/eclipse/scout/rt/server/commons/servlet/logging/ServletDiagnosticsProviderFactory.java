package org.eclipse.scout.rt.server.commons.servlet.logging;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * <h3>{@link ServletDiagnosticsProviderFactory}</h3>
 *
 * @author aho
 */
@ApplicationScoped
public class ServletDiagnosticsProviderFactory {

  public List<? extends IDiagnosticContextValueProvider> getProviders(HttpServletRequest request, HttpServletResponse response) {
    List<IDiagnosticContextValueProvider> providers = new ArrayList<IDiagnosticContextValueProvider>();
    providers.add(new HttpRequestMethodContextValueProvider(request.getMethod()));
    providers.add(new HttpRequestQueryStringContextValueProvider(request.getQueryString()));
    providers.add(new HttpRequestUriContextValueProvider(request.getQueryString()));
    HttpSession session = request.getSession(false);
    providers.add(new HttpSessionIdContextValueProvider(session != null ? session.getId() : null));
    return providers;
  }
}
