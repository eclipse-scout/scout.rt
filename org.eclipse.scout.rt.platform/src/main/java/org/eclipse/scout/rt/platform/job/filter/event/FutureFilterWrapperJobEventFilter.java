/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.filter.event;

import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Use this class to filter {@link JobEvent}s which have a {@link IFuture} associated by future related filter criteria.
 * This class facilitates the reuse of existing future-based filters by wrapping a future filter. Also, this filter
 * supports the {@link IAdaptable} mechanism.
 *
 * @since 5.1
 */
public class FutureFilterWrapperJobEventFilter implements Predicate<JobEvent>, IAdaptable {

  private final Predicate<IFuture<?>> m_futureFilterDelegate;

  public FutureFilterWrapperJobEventFilter(final Predicate<IFuture<?>> futureFilterDelegate) {
    m_futureFilterDelegate = futureFilterDelegate;
  }

  @Override
  public final boolean test(final JobEvent event) {
    final IFuture<?> future = event.getData().getFuture();
    if (future == null) {
      return false;
    }
    else {
      return m_futureFilterDelegate.test(future);
    }
  }

  @Override
  public <T> T getAdapter(final Class<T> type) {
    if (m_futureFilterDelegate instanceof IAdaptable) {
      return ((IAdaptable) m_futureFilterDelegate).getAdapter(type);
    }
    return null;
  }
}
