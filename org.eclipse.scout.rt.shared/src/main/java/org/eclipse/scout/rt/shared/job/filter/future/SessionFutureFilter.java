package org.eclipse.scout.rt.shared.job.filter.future;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.IAdaptable;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Filter to accept Futures which have a specific {@link ISession} set in their running context.
 *
 * @since 5.2
 */
public class SessionFutureFilter implements IFilter<IFuture<?>>, IAdaptable {

  private final ISession m_session;

  public SessionFutureFilter(final ISession session) {
    m_session = session;
  }

  @Override
  public boolean accept(final IFuture<?> future) {
    final RunContext runContext = future.getJobInput().getRunContext();
    if (runContext == null) {
      return false;
    }

    final ISession session = runContext.getAdapter(ISession.class);
    if (session == null) {
      return false;
    }

    return m_session == session;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (ISession.class.isAssignableFrom(type)) {
      return (T) m_session;
    }
    return null;
  }
}
