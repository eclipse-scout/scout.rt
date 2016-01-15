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
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore.QueuePosition;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedRuntimeException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link IBlockingCondition}.
 */
public class BlockingCondition implements IBlockingCondition {

  private static final Logger LOG = LoggerFactory.getLogger(BlockingCondition.class);

  private static final long TIMEOUT_INDEFINITELY = -1;

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
    waitFor(TIMEOUT_INDEFINITELY, TimeUnit.NANOSECONDS, true /* interruptible */, executionHints);
  }

  @Override
  public void waitFor(final long timeout, final TimeUnit unit, final String... executionHints) {
    waitFor(timeout, unit, true /* interruptible */, executionHints);
  }

  @Override
  public void waitForUninterruptibly(final long timeout, final TimeUnit unit, final String... executionHints) {
    waitFor(timeout, unit, false /* uninterruptible */, executionHints);
  }

  @Override
  public void waitForUninterruptibly(final String... executionHints) {
    waitFor(TIMEOUT_INDEFINITELY, TimeUnit.NANOSECONDS, false /* uninterruptible */, executionHints);
  }

  protected void waitFor(final long timeout, final TimeUnit unit, final boolean awaitInterruptibly, final String... executionHints) {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture instanceof JobFutureTask) {
      blockJobThread((JobFutureTask<?>) currentFuture, timeout, unit, awaitInterruptibly, executionHints);
    }
    else {
      blockNonJobThread(timeout, unit, awaitInterruptibly);
    }
  }

  protected void blockNonJobThread(final long timeout, final TimeUnit unit, final boolean awaitInterruptibly) {
    if (!m_blocking) {
      return;
    }

    m_lock.lock();
    try {
      if (!m_blocking) {
        return; // double-checked locking
      }

      awaitUntilSignaledOrTimeout(timeout, unit, awaitInterruptibly);
    }
    finally {
      m_lock.unlock();
    }
  }

  protected void blockJobThread(final JobFutureTask<?> futureTask, final long timeout, final TimeUnit unit, final boolean awaitInterruptibly, final String... executionHints) {
    if (!m_blocking) {
      return;
    }

    IExecutionHintRegistration executionHintRegistration = IExecutionHintRegistration.NULL_INSTANCE;
    RuntimeException exceptionWhileWaiting = null;
    Error errorWhileWaiting = null;

    m_lock.lock();
    try {
      if (!m_blocking) {
        return; // double-checked locking
      }

      // Associate the future with execution hints.
      executionHintRegistration = registerExecutionHints(futureTask, executionHints);

      // Change job state.
      futureTask.changeState(new JobEventData()
          .withState(JobState.WAITING_FOR_BLOCKING_CONDITION)
          .withFuture(futureTask)
          .withBlockingCondition(this));

      // Release the permit if being a semaphore aware task, but only if currently being a permit owner.
      futureTask.releasePermit();
      try {
        awaitUntilSignaledOrTimeout(timeout, unit, awaitInterruptibly);
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
    executionHintRegistration.dispose();
    futureTask.changeState(JobState.RUNNING);

    if (errorWhileWaiting != null) {
      throw errorWhileWaiting;
    }
    if (exceptionWhileWaiting != null) {
      throw exceptionWhileWaiting;
    }
  }

  /**
   * Waits until signaled or the timeout elapses. If <code>awaitInterruptibly</code> is set to <code>true</code>, this
   * method returns with an {@link InterruptedRuntimeException} upon interruption. For either case, when this method
   * finally returns, the thread's interrupted status will still be set.
   */
  protected void awaitUntilSignaledOrTimeout(final long timeout, final TimeUnit unit, final boolean awaitInterruptibly) {
    boolean interrupted = false;
    m_lock.lock();
    try {
      if (timeout == TIMEOUT_INDEFINITELY) {
        while (m_blocking) { // while-loop to address spurious wake-ups
          if (awaitInterruptibly) {
            m_unblockedCondition.await();
          }
          else {
            m_unblockedCondition.awaitUninterruptibly();
          }
        }
      }
      else {
        long nanos = unit.toNanos(timeout);
        while (m_blocking && nanos > 0L) { // while-loop to address spurious wake-ups
          if (awaitInterruptibly) {
            nanos = m_unblockedCondition.awaitNanos(nanos);
          }
          else {
            try {
              nanos = m_unblockedCondition.awaitNanos(nanos);
            }
            catch (final InterruptedException e) {
              interrupted = true; // remember interruption
              Thread.interrupted(); // clear the interrupted status to continue waiting
            }
          }
        }

        if (nanos <= 0L) {
          throw new TimeoutException("Timeout elapsed while waiting for a blocking condition to fall")
              .withContextInfo("blockingCondition", this)
              .withContextInfo("timeout", "{}ms", unit.toMillis(timeout))
              .withContextInfo("thread", Thread.currentThread().getName());
        }
      }
    }
    catch (final InterruptedException e) {
      interrupted = true;
      throw new InterruptedRuntimeException("Interrupted while waiting for a blocking condition to fall")
          .withContextInfo("blockingCondition", this)
          .withContextInfo("thread", Thread.currentThread().getName());
    }
    finally {
      m_lock.unlock();

      if (interrupted) {
        Thread.currentThread().interrupt(); // restore interruption status
      }
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
      catch (final InterruptedRuntimeException e) {
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
