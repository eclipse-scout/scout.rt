package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * This class provides the {@link UiSession#getClientSessionId()} to be set into the <code>diagnostic context map</code>
 * for logging purpose.
 *
 * @see #KEY
 * @see DiagnosticContextValueProcessor
 * @see MDC
 */
@ApplicationScoped
public class JobNameContextValueProvider implements IDiagnosticContextValueProvider {

  public static final String KEY = "scout.job.name";

  @Override
  public String key() {
    return KEY;
  }

  @Override
  public String value() {
    final IFuture<?> future = IFuture.CURRENT.get();
    return future != null ? future.getJobInput().getName() : null;
  }
}
