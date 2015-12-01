package org.eclipse.scout.rt.server.commons.servlet.logging;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.server.commons.servlet.IHttpServletRoundtrip;
import org.slf4j.MDC;

/**
 * This class provides the {@link HttpServletRequest#getMethod()} to be set into the <code>diagnostic context map</code>
 * for logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 * @since 5.1
 */
@ApplicationScoped
public class HttpRequestUriContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "http.request.method";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    return IHttpServletRoundtrip.CURRENT_HTTP_SERVLET_REQUEST.get().getMethod();
  }
}
