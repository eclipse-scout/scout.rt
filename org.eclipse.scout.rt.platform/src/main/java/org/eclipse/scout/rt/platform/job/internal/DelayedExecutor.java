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

import java.util.Date;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides functionality to execute Runnables some time in the future on behalf of a
 * {@link ThreadPoolExecutor}. Thereto, this executor schedules a 'Dispatch-Loop-Runnable' to wait for expired Runnables
 * to be executed.
 * <p>
 * This class is necessary because {@link ScheduledThreadPoolExecutor} is not applicable for {@link JobManager} due to
 * its fixed-size thread pool. That means, that once the <code>core-pool-size</code> is exceeded, the creation of
 * on-demand threads up to a <code>maximum-pool-size</code> would not be supported.
 * <p>
 * Also, this executor is used over Quartz Scheduler because of its better performance when having more than 10'000 jobs
 * running simultaneously.
 *
 * @since 5.1
 */
class DelayedExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DelayedExecutor.class);

  private final ExecutorService m_executor;
  private final String m_threadName;
  private final AtomicLong m_sequencer = new AtomicLong(-Long.MAX_VALUE);
  private final DelayQueue<P_DelayedTask> m_delayedTaskQueue = new DelayQueue<>();

  /**
   * @param executor
   *          executor to run the 'Dispatch-Loop-Runnable' and execute offered Runnables.
   * @param threadName
   *          the thread-name of the 'Dispatch-Loop-Runnable'.
   */
  DelayedExecutor(final ExecutorService executor, final String threadName) {
    m_executor = executor;
    m_threadName = threadName;
    m_executor.execute(new P_DispatchLoop());
  }

  /**
   * Runs the given Runnable asynchronously once the given fire time elapses. The caller of this method continues to run
   * in parallel.
   * <p>
   * The run method of the {@link Runnable} is invoked from within the scheduler thread. Hence, any long running
   * operation should be done asynchronously within a separate job.
   *
   * @param runnable
   *          the Runnable to be executed some time in the future.
   * @param fireTime
   *          the time the Runnable should commence execution. Must not be <code>null</code>.
   */
  public void schedule(final Runnable runnable, final Date fireTime) {
    Assertions.assertNotNull(fireTime, "FireTime must not be null");
    m_delayedTaskQueue.put(new P_DelayedTask(runnable, fireTime));
  }

  /**
   * Returns <code>-1</code> if <code>value1</code> is less than <code>value2</code>, or <code>+1</code> if
   * <code>value1</code> is greater than <code>value2</code>, or <code>0</code> if the two values are equals.
   */
  private int signum(final long value1, final long value2) {
    // do not subtract because of potential overflow.
    // do not subtract because of potential overflow.
    return Long.compare(value1, value2);
  }

  /**
   * Dispatch loop to wait for expired Runnables to be executed. This Runnable runs as long as the executor is not
   * shutdown.
   */
  private class P_DispatchLoop implements Runnable {

    @Override
    public void run() {
      ThreadInfo.CURRENT.get().updateThreadName(m_threadName, null);
      try {
        while (!m_executor.isShutdown()) {
          try {
            m_delayedTaskQueue.take().run(); // 'DelayQueue#take' blocks until a Runnable's delay expired to be executed.
          }
          catch (final InterruptedException e) {
            Thread.interrupted(); // ensure the interrupted status to be cleared.
          }
          catch (final RuntimeException | Error t) { // NOSONAR
            LOG.error("Unexpected exception while waiting for expired runnables to be executed.", t);
          }
        }
      }
      finally {
        ThreadInfo.CURRENT.get().reset();
      }
    }
  }

  /**
   * Represents a task to be executed some time in the future.
   */
  private class P_DelayedTask implements Delayed, Runnable {

    private final Runnable m_runnable;
    private final long m_executionTime;
    private final long m_sequenceNumber;

    P_DelayedTask(final Runnable runnable, final Date fireTime) {
      m_runnable = runnable;
      m_executionTime = fireTime.getTime();
      m_sequenceNumber = m_sequencer.incrementAndGet();
    }

    @Override
    public void run() {
      m_runnable.run();
    }

    @Override
    public long getDelay(final TimeUnit unit) {
      return unit.convert(m_executionTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(final Delayed other) {
      if (other == this) { // NOSONAR
        return 0;
      }

      final P_DelayedTask otherDelayedTask = (P_DelayedTask) other;
      final int signum = signum(m_executionTime, otherDelayedTask.m_executionTime);
      if (signum != 0) {
        return signum;
      }
      else {
        return signum(m_sequenceNumber, otherDelayedTask.m_sequenceNumber);
      }
    }
  }
}
