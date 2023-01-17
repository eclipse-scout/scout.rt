/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.job.filter.event;

import java.util.function.Predicate;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.job.filter.future.ModelJobFutureFilter;
import org.eclipse.scout.rt.platform.job.filter.event.FutureFilterWrapperJobEventFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Filter to accept events only related to model jobs.
 *
 * @see ModelJobs
 * @since 5.2
 */
public final class ModelJobEventFilter implements Predicate<JobEvent>, IAdaptable {

  public static final Predicate<JobEvent> INSTANCE = new ModelJobEventFilter();

  private final FutureFilterWrapperJobEventFilter m_futureFilterDelegate;

  private ModelJobEventFilter() {
    m_futureFilterDelegate = new FutureFilterWrapperJobEventFilter(ModelJobFutureFilter.INSTANCE);
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
