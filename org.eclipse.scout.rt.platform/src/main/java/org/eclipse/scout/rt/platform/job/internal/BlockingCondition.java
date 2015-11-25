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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.commons.StringUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.IMutex.QueuePosition;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IBlockingCondition}.
 */
public class BlockingCondition implements IBlockingCondition {

  private static final Logger LOG = LoggerFactory.getLogger(BlockingCondition.class);

  private volatile boolean m_blocking;
  private final String m_name;

  private final Lock m_lock;
  private final Condition m_unblockedCondition;
  private final Set<JobFutureTask<?>> m_blockedJobFutures;

  private final JobManager m_jobManager;

  protected BlockingCondition(final String name, final boolean blocking, final JobManager jobManager) {
    m_name = StringUtility.nvl(name, "n/a");
    m_blocking = blocking;
    m_jobManager = jobManager;

    m_lock = new ReentrantLock();
    m_unblockedCondition = m_lock.newCondition();
    m_blockedJobFutures = Collections.synchronizedSet(new HashSet<JobFutureTask<?>>()); // synchronized because modified/read by different threads.
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public boolean isBlocking() {
    return m_blocking;
  }

  @Override
  public void setBlocking(final boolean blocking) {
    if (m_blocking == blocking) {
      return;
    }

    m_lock.lock();
    try {
      if (m_blocking == blocking) { // check again with the monitor owned.
        return;
      }

      if (!(m_blocking = blocking)) {
        // Unset blocking state so it is in correct state once this method returns.
        // That is crucial, if the invoker in turn waits for not-blocked jobs to complete, and expects just released jobs to be unblocked.
        // Otherwise, jobs that are unblocked by this invocation might be ignored, because still in blocked state.
        for (final JobFutureTask<?> blockedJobFuture : new HashSet<>(m_blockedJobFutures)) {
          unregisterAndMarkAsUnblocked(blockedJobFuture);
        }

        // Wake-up blocked threads.
        m_unblockedCondition.signalAll();
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  @Override
  public void waitFor(final String... executionHints) {
    waitFor(-1L, TimeUnit.MILLISECONDS, executionHints);
  }

  @Override
  public boolean waitFor(final long timeout, final TimeUnit unit, final String... executionHints) {
    final JobFutureTask<?> currentTask = (JobFutureTask<?>) IFuture.CURRENT.get();
    if (currentTask != null) {
      return blockManagedThread(currentTask, timeout, unit, executionHints);
    }
    else {
      return blockArbitraryThread(timeout, unit);
    }
  }

  /**
   * Blocks the current thread if being managed by {@link IJobManager}. That is if the thread as a {@link JobFutureTask}
   * associated.
   *
   * @return <code>false</code> if the timeout elapsed, <code>false</code> otherwise.
   * @throws ProcessingException
   *           if the waiting thread was interrupted.
   */
  protected boolean blockManagedThread(final JobFutureTask<?> jobTask, final long timeout, final TimeUnit unit, final String... executionHints) {
    final IMutex mutex = jobTask.getMutex();
    final Set<String> associatedExecutionHints = new HashSet<>();

    m_lock.lock();
    try {
      if (!m_blocking) {
        return true;
      }

      // Associate the current IFuture with execution hints.
      for (final String executionHint : executionHints) {
        if (jobTask.addExecutionHint(executionHint)) {
          associatedExecutionHints.add(executionHint);
        }
      }

      registerAndMarkAsBlocked(jobTask);
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.BLOCKED)
          .withFuture(jobTask)
          .withBlockingCondition(this));

      // If being a mutual exclusive task, release the mutex.
      if (mutex != null) {
        mutex.release(jobTask);
      }

      blockUntilSignaledOrTimeout(timeout, unit, new IBlockingGuard() {

        @Override
        public boolean shouldBlock() {
          // This method is called once the condition is signaled or a spurious wake-up occurs.
          // However, even if being signaled, this BlockingCondition might be armed anew the time this thread finally acquires the monitor lock.
          // For that reason, the blocking state must be set anew.
          if (m_blocking && !jobTask.isBlocked()) {
            registerAndMarkAsBlocked(jobTask);
          }

          return m_blocking;
        }
      });
    }
    catch (final InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      unregisterAndMarkAsUnblocked(jobTask);
      throw new ProcessingException(String.format("Interrupted while waiting for a blocking condition to fall. [blockingCondition=%s, thread=%s]", m_name, Thread.currentThread().getName()), e);
    }
    catch (final TimeoutException e) {
      unregisterAndMarkAsUnblocked(jobTask);
      LOG.debug(String.format("Timeout elapsed while waiting for a blocking condition to fall. [blockingCondition=%s, thread=%s]", m_name, Thread.currentThread().getName()), e);
      return false;
    }
    finally {
      // Restore to the previous execution hints.
      for (final String executionHint : associatedExecutionHints) {
        jobTask.removeExecutionHint(executionHint);
      }

      // Note: If released gracefully, the job's blocking-state is unset by the releaser.
      m_lock.unlock();
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.UNBLOCKED)
          .withFuture(jobTask)
          .withBlockingCondition(this));
    }

    // Acquire the mutex anew if being a mutual exclusive task. If not free, the current thread is blocked until available.
    if (mutex != null) {
      mutex.acquire(jobTask, QueuePosition.HEAD);
    }

    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.RESUMED)
        .withFuture(jobTask)
        .withBlockingCondition(this));

    return true;
  }

  /**
   * Blocks the current thread if not being managed by {@link IJobManager}.
   *
   * @return <code>false</code> if the timeout elapsed, <code>false</code> otherwise.
   * @throws ProcessingException
   *           if the waiting thread was interrupted.
   */
  protected boolean blockArbitraryThread(final long timeout, final TimeUnit unit) {
    m_lock.lock();
    try {
      if (!m_blocking) {
        return true;
      }

      blockUntilSignaledOrTimeout(timeout, unit, new IBlockingGuard() {

        @Override
        public boolean shouldBlock() {
          return m_blocking; // This method is called once the condition is signaled or a spurious wake-up occurs.
        }
      });
      return true;
    }
    catch (final InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      throw new ProcessingException(String.format("Interrupted while waiting for a blocking condition to fall. [blockingCondition=%s, thread=%s]", m_name, Thread.currentThread().getName()), e);
    }
    catch (final TimeoutException e) {
      LOG.debug(String.format("Timeout elapsed while waiting for a blocking condition to fall. [blockingCondition=%s, thread=%s]", m_name, Thread.currentThread().getName()), e);
      return false;
    }
    finally {
      m_lock.unlock();
    }
  }

  protected void unregisterAndMarkAsUnblocked(final JobFutureTask<?> futureTask) {
    futureTask.setBlocked(false);
    m_blockedJobFutures.remove(futureTask);
  }

  protected void registerAndMarkAsBlocked(final JobFutureTask<?> futureTask) {
    futureTask.setBlocked(true);
    m_blockedJobFutures.add(futureTask);
  }

  /**
   * Blocks the current thread until being signaled and {@link IBlockingGuard#shouldBlock()} evaluates to
   * <code>false</code>, or the timeout elapses.
   */
  protected void blockUntilSignaledOrTimeout(final long timeout, final TimeUnit unit, final IBlockingGuard guard) throws InterruptedException, TimeoutException {
    if (timeout == -1L) {
      while (guard.shouldBlock()) {
        m_unblockedCondition.await();
      }
    }
    else {
      long nanos = unit.toNanos(timeout);
      while (guard.shouldBlock() && nanos > 0L) {
        nanos = m_unblockedCondition.awaitNanos(nanos);
      }

      if (nanos <= 0L) {
        throw new TimeoutException();
      }
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("name", m_name);
    builder.attr("blocking", m_blocking);
    return builder.toString();
  }

  /**
   * Guard to protect against spurious wake-ups and to ensure the condition to be still <code>true</code> once being
   * unblocked.
   */
  public static interface IBlockingGuard {

    /**
     * @return <code>true</code> to keep the waiting thread blocked, or <code>false</code> to release it.
     */
    boolean shouldBlock();
  }
}
