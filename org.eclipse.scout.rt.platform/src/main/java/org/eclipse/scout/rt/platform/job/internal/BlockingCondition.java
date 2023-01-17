/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.rt.platform.job.IBlockingCondition;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.QueuePosition;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IBlockingCondition}.
 *
 * @see Jobs#newBlockingCondition(boolean)
 */
public class BlockingCondition implements IBlockingCondition {

  private static final Logger LOG = LoggerFactory.getLogger(BlockingCondition.class);

  protected static final long TIMEOUT_INDEFINITELY = -1;

  protected volatile boolean m_blocking;

  protected final Lock m_lock = new ReentrantLock();
  protected final Condition m_unblockedCondition = m_lock.newCondition();

  protected final Set<IRegistrationHandle> m_waitForHints = new HashSet<>();

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

      // Unset the 'wait-for' hints, which were associated for the time of being blocked.
      // Do this immediately and not only upon waking up the blocked threads, so that it is reflected immediately and can be evaluated by the caller.
      final Iterator<IRegistrationHandle> iterator = m_waitForHints.iterator();
      while (iterator.hasNext()) {
        iterator.next().dispose();
        iterator.remove();
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  @Override
  public void waitFor(final String... executionHints) {
    waitFor(TIMEOUT_INDEFINITELY, TimeUnit.NANOSECONDS, executionHints);
  }

  @Override
  public void waitFor(final long timeout, final TimeUnit unit, final String... executionHints) {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture instanceof JobFutureTask) {
      blockJobThread((JobFutureTask<?>) currentFuture, timeout, unit, executionHints);
    }
    else {
      blockRegularThread(timeout, unit);
    }
  }

  /**
   * Blocks a regular thread not associated with a job.
   */
  protected void blockRegularThread(final long timeout, final TimeUnit unit) {
    if (!m_blocking) {
      return;
    }

    m_lock.lock();
    try {
      if (!m_blocking) {
        return; // double-checked locking
      }

      awaitUntilSignaledOrTimeout(timeout, unit);
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Blocks a thread associated with a job.
   */
  protected void blockJobThread(final JobFutureTask<?> futureTask, final long timeout, final TimeUnit unit, final String... executionHints) {
    if (!m_blocking) {
      return;
    }

    IRegistrationHandle waitForHints = IRegistrationHandle.NULL_HANDLE;
    RuntimeException exceptionWhileWaiting = null;
    Error errorWhileWaiting = null;

    m_lock.lock();
    try {
      if (!m_blocking) {
        return; // double-checked locking
      }

      // Associate the future with execution hints.
      waitForHints = registerWaitForHints(futureTask, executionHints);

      // Change job state.
      futureTask.changeState(new JobEventData()
          .withState(JobState.WAITING_FOR_BLOCKING_CONDITION)
          .withFuture(futureTask)
          .withBlockingCondition(this));

      // Release the permit if being a semaphore aware task, but only if currently being a permit owner.
      futureTask.releasePermit();
      try {
        awaitUntilSignaledOrTimeout(timeout, unit);
      }
      catch (final RuntimeException e) {
        exceptionWhileWaiting = e;
      }
      catch (final Error e) { // NOSONAR
        errorWhileWaiting = e;
      }
    }
    finally {
      m_lock.unlock();
    }

    // re-acquire the permit (must be outside the lock)
    acquirePermitUninterruptibly(futureTask);

    // prepare to continue execution
    waitForHints.dispose(); // if released via 'setBlocking(false)', the 'wait-for' hints are already disposed, but not if interrupted or timed out.
    futureTask.changeState(JobState.RUNNING);

    if (errorWhileWaiting != null) { // NOSONAR
      throw errorWhileWaiting;
    }
    if (exceptionWhileWaiting != null) {
      throw exceptionWhileWaiting;
    }
  }

  /**
   * Waits until signaled or the timeout elapses. This method returns with an {@link ThreadInterruptedError} upon
   * interruption. When this method finally returns, the thread's interrupted status will still be set.
   */
  protected void awaitUntilSignaledOrTimeout(final long timeout, final TimeUnit unit) {
    InterruptedException interrupted = null;
    m_lock.lock();
    try {//lock
      try {//interruption
        if (timeout == TIMEOUT_INDEFINITELY) {
          while (m_blocking) { // while-loop to address spurious wake-ups
            m_unblockedCondition.await();
          }
        }
        else {
          long nanos = unit.toNanos(timeout);
          while (m_blocking && nanos > 0L) { // while-loop to address spurious wake-ups
            nanos = m_unblockedCondition.awaitNanos(nanos);
          }
          if (nanos <= 0L) {
            throw new TimedOutError("Timeout elapsed while waiting for a blocking condition to fall")
                .withContextInfo("blockingCondition", this)
                .withContextInfo("timeout", "{}ms", unit.toMillis(timeout))
                .withContextInfo("thread", Thread.currentThread().getName());
          }
        }
      }
      catch (final InterruptedException e) {
        interrupted = e;
        Thread.currentThread().interrupt(); // restore interruption status
      }
      if (interrupted != null || Thread.currentThread().isInterrupted()) {
        throw new ThreadInterruptedError("Interrupted while waiting for a blocking condition to fall", interrupted)
            .withContextInfo("blockingCondition", this)
            .withContextInfo("thread", Thread.currentThread().getName());
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Waits until acquired a permit for the given task. This method returns immediately if the specified task is not a
   * semaphore aware task.
   * <p>
   * If the current thread's interrupted status is set when it enters this method, or it is interrupted while waiting,
   * it will continue to wait until acquired the permit. When it finally returns from this method its interrupted status
   * will still be set.
   */
  protected void acquirePermitUninterruptibly(final JobFutureTask<?> futureTask) {
    final ExecutionSemaphore semaphore = futureTask.getExecutionSemaphore();
    if (semaphore == null) {
      return;
    }

    futureTask.changeState(JobState.WAITING_FOR_PERMIT);

    boolean interrupted = Thread.interrupted(); // clear interruption status to acquire the permit.
    while (!semaphore.isPermitOwner(futureTask)) {
      try {
        semaphore.acquire(futureTask, QueuePosition.HEAD);
      }
      catch (final ThreadInterruptedError e) {
        interrupted = true;
        Thread.interrupted(); // clear interruption status to re-acquire the permit.
        LOG.info("Interrupted while acquiring semaphore permit. Continue to re-acquire the permit. [future={}, semaphore={}]", futureTask, semaphore, e);
      }
    }

    if (interrupted) {
      Thread.currentThread().interrupt(); // restore interruption status
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("blocking", m_blocking);
    return builder.toString();
  }

  /**
   * Registers the given 'wait-for' execution hints with the Future, and returns its registration handle.
   */
  protected IRegistrationHandle registerWaitForHints(final IFuture<?> future, final String... executionHints) {
    if (executionHints.length == 0) {
      return IRegistrationHandle.NULL_HANDLE;
    }

    // Register the execution hints, and remember the hints registered. That are hints not already associated with the future.
    final Set<String> associatedExecutionHints = new HashSet<>(executionHints.length);
    for (final String executionHint : executionHints) {
      if (future.addExecutionHint(executionHint)) {
        associatedExecutionHints.add(executionHint);
      }
    }

    // Return the registration handle to unregister the registered execution hints.
    final AtomicBoolean disposed = new AtomicBoolean(false);
    final IRegistrationHandle registrationHandle = () -> {
      if (!disposed.compareAndSet(false, true)) {
        return; // already disposed, e.g. due to timeout or interruption
      }

      for (final String associatedExecutionHint : associatedExecutionHints) {
        future.removeExecutionHint(associatedExecutionHint);
      }
    };
    m_waitForHints.add(registrationHandle);
    return registrationHandle;
  }
}
