package org.eclipse.scout.rt.platform.job.internal;

import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor;
import org.eclipse.scout.rt.platform.logger.DiagnosticContextValueProcessor.IDiagnosticContextValueProvider;

/**
 * Provides the job name to be set into the <code>diagnostic context map</code> for logging purpose. This value provider
 * is expected to be invoked from within a job, meaning that {@link IFuture} is present.
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
    return IFuture.CURRENT.get().getJobInput().getName();
  }
}
