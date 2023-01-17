/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.job.filter.future;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.util.IAdaptable;
import org.eclipse.scout.rt.shared.ISession;

/**
 * Filter to accept Futures which have a specific {@link ISession} set in their running context.
 *
 * @since 5.2
 */
public class SessionFutureFilter implements Predicate<IFuture<?>>, IAdaptable {

  private final ISession m_session;

  public SessionFutureFilter(final ISession session) {
    m_session = session;
  }

  @Override
  public boolean test(final IFuture<?> future) {
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
