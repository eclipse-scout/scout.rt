/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.shared.job.filter.event;

import org.eclipse.scout.rt.platform.filter.IFilter;
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
public class SessionJobEventFilter implements IFilter<JobEvent>, IAdaptable {

  private final FutureFilterWrapperJobEventFilter m_futureFilterDelegate;

  public SessionJobEventFilter(final ISession session) {
    m_futureFilterDelegate = new FutureFilterWrapperJobEventFilter(new SessionFutureFilter(session));
  }

  @Override
  public boolean accept(final JobEvent event) {
    return m_futureFilterDelegate.accept(event);
  }

  @Override
  public <T> T getAdapter(final Class<T> type) {
    return m_futureFilterDelegate.getAdapter(type);
  }
}
