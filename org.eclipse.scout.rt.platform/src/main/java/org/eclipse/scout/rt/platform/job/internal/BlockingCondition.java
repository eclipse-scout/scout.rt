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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.IMutex.QueuePosition;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;

/**
 * Implementation of {@link IBlockingCondition}.
 */
public class BlockingCondition implements IBlockingCondition {

  private static final long TIMEOUT_INFINITE = -1;

  private volatile boolean m_blocking;

  private final Lock m_lock = new ReentrantLock();
  private final Condition m_unblockedCondition = m_lock.newCondition();

  protected BlockingCondition(final boolean blocking) {
    m_blocking = blocking;
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

      m_blocking = blocking;
      if (!m_blocking) {
        m_unblockedCondition.signalAll(); // Wake-up waiting threads.
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  @Override
  public void waitFor(final String... executionHints) {
    waitFor(TIMEOUT_INFINITE, TimeUnit.MILLISECONDS, executionHints);
  }

  @Override
  public void waitFor(final long timeout, final TimeUnit unit, final String... executionHints) {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture instanceof JobFutureTask) {
      blockFutureTask((JobFutureTask<?>) currentFuture, timeout, unit, executionHints);
    }
    else {
      blockThread(timeout, unit);
    }
  }

  protected void blockFutureTask(final JobFutureTask<?> futureTask, final long timeout, final TimeUnit unit, final String... executionHints) {
    IExecutionHintRegistration executionHintRegistration = IExecutionHintRegistration.NULL_INSTANCE;

    m_lock.lock();
    try {
      if (!m_blocking) {
        return;
      }

      // Associate the future with execution hints.
      executionHintRegistration = registerExecutionHints(futureTask, executionHints);

      // Change job state.
      futureTask.changeState(new JobEventData()
          .withState(JobState.WAITING_FOR_BLOCKING_CONDITION)
          .withFuture(futureTask)
          .withBlockingCondition(this));

      // Release the mutex if being a mutually exclusive task.
      futureTask.releaseMutex();
      try {
        blockUntilSignaledOrTimeout(timeout, unit); // Wait until the condition falls
      }
      catch (final InterruptedException | TimeoutException e) {
        executionHintRegistration.dispose();
        futureTask.changeState(JobState.RUNNING);
        throw e;
      }
    }
    finally {
      m_lock.unlock();
    }

    // Acquire the mutex if being a mutually exclusive task.
    final IMutex mutex = futureTask.getMutex();
    if (mutex != null) {
      try {
        futureTask.changeState(JobState.WAITING_FOR_MUTEX);
        mutex.acquire(futureTask, QueuePosition.HEAD);
      }
      catch (final InterruptedException e) {
        executionHintRegistration.dispose();
        futureTask.changeState(JobState.RUNNING);
        throw e;
      }
    }

    executionHintRegistration.dispose();
    futureTask.changeState(JobState.RUNNING);
  }

  protected void blockThread(final long timeout, final TimeUnit unit) {
    m_lock.lock();
    try {
      if (!m_blocking) {
        return;
      }

      blockUntilSignaledOrTimeout(timeout, unit);
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Blocks the current thread until being signaled, or the timeout elapses.
   *
   * @param timeout
   *          the maximal time to wait, or {@link #TIMEOUT_INFINITE} to wait infinitely.
   * @param unit
   *          unit of the given timeout.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws TimeoutException
   *           if the wait timed out.
   */
  protected void blockUntilSignaledOrTimeout(final long timeout, final TimeUnit unit) {
    m_lock.lock();
    try {
      if (timeout == TIMEOUT_INFINITE) {
        while (m_blocking) { // while-loop to address spurious wake-ups.
          m_unblockedCondition.await();
        }
      }
      else {
        long nanos = unit.toNanos(timeout);
        while (m_blocking && nanos > 0L) { // while-loop to address spurious wake-ups.
          nanos = m_unblockedCondition.awaitNanos(nanos);
        }

        if (nanos <= 0L) {
          throw new TimeoutException("Timeout elapsed while waiting for a blocking condition to fall")
              .withContextInfo("blockingCondition", this)
              .withContextInfo("timeout", "{}ms", unit.toMillis(timeout))
              .withContextInfo("thread", Thread.currentThread().getName());
        }
      }
    }
    catch (final java.lang.InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore the interrupted status because cleared by catching InterruptedException.
      throw new InterruptedException("Interrupted while waiting for a blocking condition to fall")
          .withContextInfo("blockingCondition", this)
          .withContextInfo("thread", Thread.currentThread().getName());
    }
    finally {
      m_lock.unlock();
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("blocking", m_blocking);
    return builder.toString();
  }

  /**
   * Registers the given execution hints with the Future, and returns its registration handle.
   */
  protected IExecutionHintRegistration registerExecutionHints(final IFuture<?> future, final String... executionHints) {
    if (executionHints.length == 0) {
      return IExecutionHintRegistration.NULL_INSTANCE;
    }

    // Register the execution hints, and remember the hints registered. That are hints not already associated with the future.
    final Set<String> associatedExecutionHints = new HashSet<>(executionHints.length);
    for (final String executionHint : executionHints) {
      if (future.addExecutionHint(executionHint)) {
        associatedExecutionHints.add(executionHint);
      }
    }

    // Return the registration handle to unregister the registered execution hints.
    return new IExecutionHintRegistration() {

      @Override
      public void dispose() {
        for (final String associatedExecutionHint : associatedExecutionHints) {
          future.removeExecutionHint(associatedExecutionHint);
        }
      }
    };
  }

  /**
   * Represents the execution hint registration.
   */
  protected interface IExecutionHintRegistration {

    /**
     * Unregisters the execution hints represented by this registration.
     */
    void dispose();

    /**
     * Registration that does nothing on {@link #dispose()}.
     */
    IExecutionHintRegistration NULL_INSTANCE = new IExecutionHintRegistration() {

      @Override
      public void dispose() {
        // NOOP
      }
    };
  }
}
