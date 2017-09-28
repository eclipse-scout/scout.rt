/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job.filter.event;

import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.job.filter.future.ModelJobFutureFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.filter.event.FutureFilterWrapperJobEventFilter;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.IAdaptable;

/**
 * Filter to accept events only related to model jobs.
 *
 * @see ModelJobs
 * @since 5.2
 */
public final class ModelJobEventFilter implements IFilter<JobEvent>, IAdaptable {

  public static final IFilter<JobEvent> INSTANCE = new ModelJobEventFilter();

  private final FutureFilterWrapperJobEventFilter m_futureFilterDelegate;

  private ModelJobEventFilter() {
    m_futureFilterDelegate = new FutureFilterWrapperJobEventFilter(ModelJobFutureFilter.INSTANCE);
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
