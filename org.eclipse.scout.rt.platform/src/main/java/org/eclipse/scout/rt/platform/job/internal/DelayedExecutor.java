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

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.JobState;
import org.eclipse.scout.rt.platform.job.internal.NamedThreadFactory.ThreadInfo;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides functionality to execute Runnables some time in the future on behalf of a
 * {@link ThreadPoolExecutor}. Thereto, this executor schedules a 'Dispatch-Loop-Runnable' to wait for expired Runnables
 * to be executed.
 * <p/>
 * This class is necessary because {@link ScheduledThreadPoolExecutor} is not applicable for {@link JobManager} due to
 * its fixed-size thread pool. That means, that once the <code>core-pool-size</code> is exceeded, the creation of
 * on-demand threads up to a <code>maximum-pool-size</code> would not be supported.
 *
 * @since 5.1
 */
class DelayedExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(DelayedExecutor.class);

  private final ExecutorService m_executor;
  private final String m_threadName;
  private final AtomicLong m_sequencer = new AtomicLong();
  private final DelayQueue<P_DelayedTask> m_delayedTaskQueue = new DelayQueue<>();

  /**
   * @param executor
   *          executor to run the 'Dispatch-Loop-Runnable' and execute offered Runnables.
   * @param threadName
   *          the thread-name of the 'Dispatch-Loop-Runnable'.
   * @param dispatcherThreadCount
   *          the number of threads used to dispatch delayed jobs; must be > 0.
   */
  public DelayedExecutor(final ExecutorService executor, final String threadName, final int dispatcherThreadCount) {
    Assertions.assertGreater(dispatcherThreadCount, 0);
    m_executor = executor;
    m_threadName = threadName;

    for (int i = 0; i < dispatcherThreadCount; i++) {
      m_executor.execute(new P_DispatchLoop());
    }
  }

  /**
   * Runs the given Runnable asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller
   * of this method continues to run in parallel.
   *
   * @param runnable
   *          the Runnable to be executed some time in the future.
   * @param delay
   *          the delay after which the Runnable should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   */
  public void schedule(final Runnable runnable, final long delay, final TimeUnit delayUnit) {
    m_delayedTaskQueue.put(new P_DelayedTask(runnable, delayUnit.toNanos(delay)));
  }

  /**
   * Dispatch loop to wait for expired Runnables to be executed. This Runnable runs as long as the executor is not
   * shutdown.
   */
  private class P_DispatchLoop implements Runnable {

    @Override
    public void run() {
      ThreadInfo.CURRENT.get().updateNameAndState(null, m_threadName, JobState.Running);
      try {
        while (!m_executor.isShutdown()) {
          try {
            m_executor.execute(m_delayedTaskQueue.take()); // 'DelayQueue#take' blocks until a Runnable's delay expired to be executed.
          }
          catch (final InterruptedException e) {
            if (!m_executor.isShutdown()) {
              LOG.error("Interrupted while waiting for 'delayed runnables' to be executed; ignored interruption because the executor is still running", e);
            }
          }
          catch (final Throwable t) {
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
    private final long m_executionNanoTime;
    private final long m_sequenceNumber;

    P_DelayedTask(final Runnable runnable, final long delayNano) {
      m_runnable = runnable;
      m_executionNanoTime = System.nanoTime() + delayNano;
      m_sequenceNumber = m_sequencer.incrementAndGet();
    }

    @Override
    public void run() {
      m_runnable.run();
    }

    @Override
    public long getDelay(final TimeUnit unit) {
      return unit.convert(m_executionNanoTime - System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    @Override
    public int compareTo(final Delayed other) {
      if (other == this) {
        return 0;
      }

      final P_DelayedTask otherDelayedTask = (P_DelayedTask) other;
      final int signum = Long.signum(m_executionNanoTime - otherDelayedTask.m_executionNanoTime);
      if (signum != 0) {
        return signum;
      }
      else {
        return Long.signum(m_sequenceNumber - otherDelayedTask.m_sequenceNumber);
      }
    }
  }
}
