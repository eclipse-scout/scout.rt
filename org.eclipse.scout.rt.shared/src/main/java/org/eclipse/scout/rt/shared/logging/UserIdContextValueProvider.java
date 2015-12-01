package org.eclipse.scout.rt.shared.logging;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.slf4j.MDC;

/**
 * This class provides the {@link ISession#getUserId()} to be set into the <code>diagnostic context map</code> for
 * logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class UserIdContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "scout.user.name";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final ISession currentSession = ISession.CURRENT.get();
    return currentSession != null ? currentSession.getUserId() : null;
  }
}
