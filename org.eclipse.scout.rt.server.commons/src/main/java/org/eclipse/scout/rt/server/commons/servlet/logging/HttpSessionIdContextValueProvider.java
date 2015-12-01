package org.eclipse.scout.rt.server.commons.servlet.logging;

import javax.servlet.http.HttpSession;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.slf4j.MDC;

/**
 * This class provides the {@link HttpSession#getId()} to be set into the <code>diagnostic context map</code> for
 * logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 * @since 5.1
 */
@ApplicationScoped
public class HttpSessionIdContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "http.session.id";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final HttpSession session = IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get().getSession(false);
    return session != null ? session.getId() : null;
  }
}
