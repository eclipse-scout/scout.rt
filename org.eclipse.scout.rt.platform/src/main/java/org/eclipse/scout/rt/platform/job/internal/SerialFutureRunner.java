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

import java.util.concurrent.FutureTask;

import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.job.JobState;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runner on a 'per-future' basis which prevents concurrent Future execution, meaning that {@link #accept()} returns
 * <code>false</code> as long as a Future is currently executing. But, any execution attempt is remembered, and
 * execution started anew upon completion of the current execution. Thereby, multiple execution attempts are
 * consolidated to one to not stack executions. Concurrent attempts may happen, if the trigger of a repeatedly job fires
 * but the previous execution did not finish yet.
 * <p>
 * If {@link #accept()} returns <code>true</code>, the caller must invoke {@link #run()} or {@link #reject()} in order
 * to release the mutex.
 * <p>
 * <em>
 * If concurrent execution for the same Future would be allowed, that could result in a state and permit inconsistency,
 * and is further not allowed by Java {@link FutureTask}.</em>
 *
 * @since 5.2
 */
@Internal
public class SerialFutureRunner implements IFutureRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SerialFutureRunner.class);

  private final Scheduler m_quartz;
  private final JobFutureTask<?> m_futureTask;
  private final P_Mutex m_mutex;

  public SerialFutureRunner(final Scheduler quartz, final JobFutureTask<?> futureTask) {
    m_quartz = quartz;
    m_futureTask = futureTask;
    m_mutex = new P_Mutex();
  }

  @Override
  public boolean accept() {
    return !m_futureTask.isDone() && m_mutex.tryAcquireElseRemember();
  }

  @Override
  public void reject() {
    m_futureTask.reject();
    releaseMutexAndFire(false); // no 'firing' because the Future is 'done'.
  }

  @Override
  public void run() {
    // The following invariants applies:
    // - this method is never invoked concurrently for the same future, which is ensured by 'P_Mutex';
    // - if the future is assigned to an execution semaphore, the future owns a permit;
    // - after JobFutureTask.run(), the future's permit is released, meaning that the future is no longer a permit owner;

    m_futureTask.run();

    if (m_futureTask.isDone()) {
      releaseMutexAndFire(false); // no 'firing' because the Future is 'done'.
    }
    else {
      m_futureTask.changeState(JobState.PENDING);
      releaseMutexAndFire(true); // schedule a potential consolidated re-run.
    }
  }

  @Override
  public JobFutureTask<?> getFutureTask() {
    return m_futureTask;
  }

  private void releaseMutexAndFire(final boolean fire) {
    final boolean concurrentRunAttempt = m_mutex.release();
    if (!concurrentRunAttempt || !fire) {
      return;
    }

    // Submit a consolidated 'one-shot' trigger to re-run the job once again.
    try {
      m_quartz.scheduleJob(TriggerBuilder.newTrigger()
          .forJob(QuartzExecutorJob.IDENTITY)
          .usingJobData(QuartzExecutorJob.newTriggerData(m_futureTask, this))
          .startNow()
          .build());
    }
    catch (final SchedulerException e) {
      LOG.error("Failed to re-run job [future={}]", m_futureTask, e);
      m_futureTask.reject();
    }
  }

  /**
   * Guarantees no concurrent execution of a job.
   */
  @Internal
  protected static class P_Mutex {

    private boolean m_running;
    private boolean m_acquisitionAttempt;

    /**
     * Tries to acquire the mutex, and returns <code>true</code> on success.
     */
    public synchronized boolean tryAcquireElseRemember() {
      if (m_running) {
        m_acquisitionAttempt = true;
        return false;
      }
      else {
        m_running = true;
        return true;
      }
    }

    /**
     * Releases the mutex, and returns <code>true</code> in case of a concurrent run attempt.
     */
    public synchronized boolean release() {
      final boolean acquisitionAttempt = m_acquisitionAttempt;
      m_running = false;
      m_acquisitionAttempt = false;
      return acquisitionAttempt;
    }
  }
}
