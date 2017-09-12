package org.eclipse.scout.rt.ui.html.logging;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.ui.html.IUiSession;
import org.eclipse.scout.rt.ui.html.UiSession;
import org.slf4j.MDC;

/**
 * This class provides the {@link IUiSession#getUiSessionId()} to be set into the <code>diagnostic context map</code>
 * for logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class UiSessionIdContextValueProvider implements IUiRunContextDiagnostics {

  public static final String KEY = "scout.ui.session.id";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final IUiSession uiSession = UiSession.CURRENT.get();
    return uiSession != null ? uiSession.getUiSessionId() : null;
  }
}
