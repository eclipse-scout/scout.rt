/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.shared.job.filter.event;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.filter.event.FutureFilterWrapperJobEventFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.IAdaptable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;

/**
 * Filter to accept all events for jobs which have a specific {@link ISession} set in their running context.
 *
 * @since 5.2
 */
public class SessionJobEventFilter implements Predicate<JobEvent>, IAdaptable {

  private final FutureFilterWrapperJobEventFilter m_futureFilterDelegate;

  public SessionJobEventFilter(final ISession session) {
    m_futureFilterDelegate = new FutureFilterWrapperJobEventFilter(new SessionFutureFilter(session));
  }

  @Override
  public boolean test(final JobEvent event) {
    return m_futureFilterDelegate.test(event);
  }

  @Override
  public <T> T getAdapter(final Class<T> type) {
    return m_futureFilterDelegate.getAdapter(type);
  }
}
