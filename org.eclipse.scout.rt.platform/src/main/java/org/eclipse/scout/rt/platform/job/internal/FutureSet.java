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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.config.PlatformConfigProperties.JobManagerCorePoolSizeProperty;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;

/**
 * Thread-safe implementation of a {@link Set} to contain {@link IFuture}s.
 *
 * @since 5.1
 */
@Bean
public class FutureSet {

  private final Set<JobFutureTask<?>> m_futures;

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;
  private final Condition m_changedCondition;

  private IRegistrationHandle m_jobListenerRegistration;

  public FutureSet() {
    m_futures = new HashSet<>(CONFIG.getPropertyValue(JobManagerCorePoolSizeProperty.class));

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
    m_changedCondition = m_writeLock.newCondition();
  }

  /**
   * Invoke to initialize this {@link FutureSet}.
   */
  public void init(final IJobManager jobManager) {
    m_jobListenerRegistration = jobManager.addListener(newSignalingFilter(), event -> {
      m_writeLock.lock();
      try {
        m_changedCondition.signalAll();
      }
      finally {
        m_writeLock.unlock();
      }
    });
  }

  /**
   * Invoke to destroy this {@link FutureSet} and to cancel all contained Futures.
   */
  public void dispose() {
    m_jobListenerRegistration.dispose();

    // Clear and cancel all futures.
    final List<JobFutureTask<?>> runningFutures;
    m_writeLock.lock();
    try {
      runningFutures = copyFutures();
      m_futures.clear();
      m_changedCondition.signalAll();
    }
    finally {
      m_writeLock.unlock();
    }

    for (final JobFutureTask<?> runningFuture : runningFutures) {
      runningFuture.cancel(true);
    }
  }

  /**
   * Adds the given Future to this {@link FutureSet}.
   */
  public void add(final JobFutureTask<?> future) {
    m_writeLock.lock();
    try {
      m_futures.add(future);
      m_changedCondition.signalAll();
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Removes the given Future from this {@link FutureSet}.
   */
  public void remove(final JobFutureTask<?> future) {
    m_writeLock.lock();
    try {
      m_futures.remove(future);
      m_changedCondition.signalAll();
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Returns <code>true</code>, if all Futures which are accepted by the given filter match the specified matcher.
   *
   * @param filter
   *     to limit the Futures to be matched by the matcher. If <code>null</code>, all contained Futures are
   *     checked.
   * @param matcher
   *     to match the filtered Futures.
   * @return <code>true</code> if all Futures accepted by the specified Filter are successfully matched.
   */
  public boolean matchesEvery(final Predicate<IFuture<?>> filter, final Predicate<JobFutureTask<?>> matcher) {
    for (final JobFutureTask<?> future : copyFutures()) {
      final boolean accepted = (filter == null || filter.test(future));

      if (accepted && !matcher.test(future)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Returns <code>true</code>, if this {@link FutureSet} contains one Future matching the given filter at minimum.
   */
  public boolean containsSome(final Predicate<IFuture<?>> filter) {
    for (final JobFutureTask<?> future : copyFutures()) {
      if (filter == null || filter.test(future)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Waits if necessary for at most the given time for all the futures matching the given filter to complete, or until
   * cancelled, or the timeout elapses.
   *
   * @param filter
   *     filter to limit the Futures to await for. If <code>null</code>, all Futures are awaited.
   * @param timeout
   *     the maximal time to wait.
   * @param unit
   *     unit of the given timeout.
   * @throws InterruptedException
   *     if the current thread was interrupted while waiting.
   * @throws TimeoutException
   *     if the wait timed out.
   */
  public void awaitDone(final Predicate<IFuture<?>> filter, final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
    Assertions.assertGreater(timeout, 0L, "Invalid timeout; must be > 0 [timeout={}]", timeout);

    // Wait until all Futures matching the filter are done, or the deadline elapsed.
    m_writeLock.lockInterruptibly();
    try {
      long nanos = unit.toNanos(timeout);
      while (!matchesEvery(filter, CompletionPromise.PROMISE_DONE_MATCHER) && nanos > 0L) {
        nanos = m_changedCondition.awaitNanos(nanos);
      }

      if (nanos <= 0L) {
        throw new TimeoutException();
      }
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Waits if necessary for at most the given time for all futures matching the given filter to finish, meaning that
   * those jobs either complete normally or by an exception, or that they will never commence execution due to a
   * premature cancellation.
   *
   * @param filter
   *     filter to limit the Futures to await for. If <code>null</code>, all Futures are awaited.
   * @param timeout
   *     the maximal time to wait.
   * @param unit
   *     unit of the given timeout.
   * @throws InterruptedException
   *     if the current thread was interrupted while waiting.
   * @throws TimeoutException
   *     if the wait timed out.
   */
  public void awaitFinished(final Predicate<IFuture<?>> filter, final long timeout, final TimeUnit unit) throws InterruptedException, TimeoutException {
    Assertions.assertGreater(timeout, 0L, "Invalid timeout; must be > 0 [timeout={}]", timeout);

    // Wait until all Futures matching the filter are removed, or the deadline elapsed.
    m_writeLock.lockInterruptibly();
    try {
      long nanos = unit.toNanos(timeout);
      while (containsSome(filter) && nanos > 0L) {
        nanos = m_changedCondition.awaitNanos(nanos);
      }

      if (nanos <= 0L) {
        throw new TimeoutException();
      }
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Returns all Futures accepted by the given {@link Predicate}.
   * <p>
   * A future is contained as long as not finished yet. A job is finished upon its completion, or upon a premature
   * cancellation, meaning that it will never commence execution.
   * <p>
   *
   * @param filter
   *     to limit the Futures to be returned. If <code>null</code>, all Futures are returned.
   * @return futures accepted by the given filter.
   */
  public final Set<IFuture<?>> values(final Predicate<IFuture<?>> filter) {
    final Set<IFuture<?>> futures = new HashSet<>();
    for (final IFuture<?> candidate : copyFutures()) {
      if (filter == null || filter.test(candidate)) {
        futures.add(candidate);
      }
    }
    return futures;
  }

  /**
   * Cancels all Futures which are accepted by the given {@link Predicate}.
   *
   * @param filter
   *     to limit the Futures to be cancelled. If <code>null</code>, all contained Futures are cancelled.
   * @param interruptIfRunning
   *     <code>true</code> to interrupt in-progress jobs.
   * @return <code>true</code> if all Futures matching the Filter are cancelled successfully, or <code>false</code>, if
   * a Future could not be cancelled, typically because already completed normally.
   */
  public boolean cancel(final Predicate<IFuture<?>> filter, final boolean interruptIfRunning) {
    final Set<Boolean> success = new HashSet<>();

    for (final IFuture<?> future : values(filter)) {
      success.add(future.cancel(interruptIfRunning));
    }

    return Collections.singleton(Boolean.TRUE).equals(success);
  }

  protected List<JobFutureTask<?>> copyFutures() {
    m_readLock.lock();
    try {
      return new ArrayList<>(m_futures); // performance hint: creating an ArrayList has much better performance than creating a HashSet.
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Creates the filter to signal waiting threads upon a job event.
   */
  protected Predicate<JobEvent> newSignalingFilter() {
    return event -> {
      switch (event.getType()) {
        case JOB_EXECUTION_HINT_ADDED:
        case JOB_EXECUTION_HINT_REMOVED:
          return true; // manual signaling required
        case JOB_STATE_CHANGED: // NOSONAR
          switch (event.getData().getState()) {
            case PENDING:
            case RUNNING:
            case WAITING_FOR_BLOCKING_CONDITION:
            case WAITING_FOR_PERMIT:
            case DONE:
              return true; // manual signaling required
            default:
              return false; // signaling done by adding/removing the Future
          }
        default:
          return false;
      }
    };
  }
}
