/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.internal.future.IFutureTask;

/**
 * Runnable to run the given {@link IFutureTask} periodically with the given 'fixed-delay' upon completion of its
 * execution.
 * <p/>
 * This class is necessary because {@link ScheduledThreadPoolExecutor} is not applicable for {@link JobManager} due to
 * its fixed-size thread pool. That means, that once the <code>core-pool-size</code> is exceeded, the creation of
 * on-demand threads up to a <code>maximum-pool-size</code> would not be supported.
 *
 * @since 5.1
 * @see ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
 * @see FixedRateRunnable
 */
class FixedDelayRunnable implements Runnable {

  private final DelayedExecutor m_delayedExecutor;
  private final IFutureTask<Void> m_futureTask;
  private final long m_delay;
  private final TimeUnit m_unit;

  public FixedDelayRunnable(final DelayedExecutor delayedExecutor, final IFutureTask<Void> futureTask, final long delay, final TimeUnit unit) {
    m_delayedExecutor = delayedExecutor;
    m_futureTask = futureTask;
    m_delay = delay;
    m_unit = unit;
  }

  @Override
  public void run() {
    // check whether the task is in 'done-state', either due to cancellation or an unhandled exception.
    if (m_futureTask.isDone()) {
      return;
    }

    m_futureTask.run();

    // re-schedule the task if still in 'done-state'.
    if (!m_futureTask.isDone()) {
      m_delayedExecutor.schedule(this, m_delay, m_unit);
    }
  }
}
