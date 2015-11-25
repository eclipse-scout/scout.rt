package org.eclipse.scout.rt.platform.job.filter.future;

import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;

/**
 * Filter to accept Futures which are tagged with a specific execution hint.
 *
 * @since 5.2
 */
public class ExecutionHintFutureFilter implements IFilter<IFuture<?>> {

  private final String m_hint;

  public ExecutionHintFutureFilter(final String hint) {
    m_hint = hint;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    return future.containsExecutionHint(m_hint);
  }
}
