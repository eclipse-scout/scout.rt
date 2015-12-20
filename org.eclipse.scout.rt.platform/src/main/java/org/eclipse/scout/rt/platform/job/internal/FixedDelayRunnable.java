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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.job.ISchedulingSemaphore;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.internal.SchedulingSemaphore.IPermitAcquiredCallback;
import org.eclipse.scout.rt.platform.job.internal.SchedulingSemaphore.QueuePosition;

/**
 * Runnable to run the given {@link JobFutureTask} periodically with the given 'fixed-delay' upon completion of its
 * execution.
 * <p>
 * This class is necessary because {@link ScheduledThreadPoolExecutor} is not applicable for {@link JobManager} due to
 * its fixed-size thread pool. That means, that once the <code>core-pool-size</code> is exceeded, the creation of
 * on-demand threads up to a <code>maximum-pool-size</code> would not be supported.
 * <p>
 * This class supports the periodic job to be assigned to a {@link ISchedulingSemaphore}.
 *
 * @since 5.1
 * @see ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
 * @see FixedRateRunnable
 */
class FixedDelayRunnable implements IRejectableRunnable {

  private final ExecutorService m_executor;
  private final DelayedExecutor m_delayedExecutor;
  private final JobFutureTask<?> m_futureTask;
  private final long m_delayMillis;

  public FixedDelayRunnable(final ExecutorService executor, final DelayedExecutor delayedExecutor, final JobFutureTask<?> futureTask, final long delayMillis) {
    m_executor = executor;
    m_delayedExecutor = delayedExecutor;
    m_futureTask = futureTask;
    m_delayMillis = delayMillis;
  }

  @Override
  public void run() {
    // check whether the task is in 'done-state', either due to cancellation or an unhandled exception.
    if (m_futureTask.isDone()) {
      return;
    }

    final SchedulingSemaphore semaphore = m_futureTask.getSchedulingSemaphore();
    if (semaphore == null) {
      m_futureTask.run();
      scheduleNextExecution();
    }
    else {
      m_futureTask.changeState(JobState.WAITING_FOR_PERMIT);
      semaphore.compete(m_futureTask, QueuePosition.TAIL, new IPermitAcquiredCallback() {

        @Override
        public void onPermitAcquired() {
          m_executor.execute(new IRejectableRunnable() {

            @Override
            public void run() {
              m_futureTask.run();
              scheduleNextExecution();
            }

            @Override
            public void reject() {
              m_futureTask.reject();
            }
          });
        }
      });
    }
  }

  @Override
  public void reject() {
    m_futureTask.reject();
  }

  private void scheduleNextExecution() {
    // re-schedule the task if still in 'done-state'.
    if (!m_futureTask.isDone()) {
      m_futureTask.changeState(JobState.PENDING);
      m_delayedExecutor.schedule(this, m_delayMillis, TimeUnit.MILLISECONDS);
    }
  }
}
