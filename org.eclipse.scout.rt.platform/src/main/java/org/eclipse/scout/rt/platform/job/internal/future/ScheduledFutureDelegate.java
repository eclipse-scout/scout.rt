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
package org.eclipse.scout.rt.platform.job.internal.future;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConditionAwaiter;
import org.eclipse.scout.commons.annotations.Internal;

/**
 * This class acts as delegator to the {@link ScheduledFuture} created by the {@link ScheduledThreadPoolExecutor}. As
 * long as no delegate is set, 'get' and 'cancel' requests are handled by this class and passed to the delegate once
 * being set.
 * <p/>
 * This class is only necessary due to visibility restrictions of {@link ScheduledFutureTask}, which is unlike
 * {@link FutureTask} for {@link ThreadPoolExecutor}.
 *
 * @since 5.1
 */
@Internal
public class ScheduledFutureDelegate<RESULT> implements RunnableScheduledFuture<RESULT> {

  private volatile RunnableScheduledFuture<RESULT> m_delegate;

  private final Lock m_changedLock;
  private final Condition m_changedCondition;

  private final AtomicBoolean m_cancelled = new AtomicBoolean(false);

  public ScheduledFutureDelegate() {
    m_changedLock = new ReentrantLock();
    m_changedCondition = m_changedLock.newCondition();
  }

  /**
   * Invoke this method once the delegate is known. Thereby, the current cancellation status is applied onto the
   * delegate and pending requests passed to the delegate.
   */
  public void setDelegate(final RunnableScheduledFuture<RESULT> delegate) {
    m_changedLock.lock();
    try {
      m_delegate = delegate;
      if (m_cancelled.get()) {
        m_delegate.cancel(true);
      }
      m_changedCondition.signalAll();
    }
    finally {
      m_changedLock.unlock();
    }
  }

  /**
   * @return delegate; is <code>null</code> if not set yet.
   */
  public RunnableScheduledFuture<RESULT> getDelegate() {
    return m_delegate;
  }

  @Override
  public long getDelay(final TimeUnit unit) {
    Assertions.assertNotNull(m_delegate, "Unexpected call to 'ScheduledFuture.getDelay': Delegate is not set yet.");
    return m_delegate.getDelay(unit);
  }

  @Override
  public void run() {
    Assertions.assertNotNull(m_delegate, "Unexpected call to 'ScheduledFuture.run': Delegate is not set yet.");
    m_delegate.run();
  }

  @Override
  public boolean isPeriodic() {
    Assertions.assertNotNull(m_delegate, "Unexpected call to 'ScheduledFuture.isPeriodic': Delegate is not set yet.");
    return m_delegate.isPeriodic();
  }

  @Override
  public int compareTo(final Delayed o) {
    Assertions.assertNotNull(m_delegate, "Unexpected call to 'ScheduledFuture.compareTo': Delegate is not set yet.");
    return m_delegate.compareTo(o);

  }

  @Override
  public boolean isCancelled() {
    return (m_delegate != null ? m_delegate.isCancelled() : m_cancelled.get());
  }

  @Override
  public boolean isDone() {
    return (m_delegate != null ? m_delegate.isDone() : m_cancelled.get()); // 'isDone' evaluates to false once being cancelled.
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    if (m_delegate != null) {
      return m_delegate.cancel(interruptIfRunning);
    }
    else {
      // Future delegate not set yet. Remember the cancel state and notify about the change.
      if (m_cancelled.compareAndSet(false, true)) {
        m_changedLock.lock();
        try {
          m_changedCondition.signalAll();
          return true; // only true the first time being cancelled.
        }
        finally {
          m_changedLock.unlock();
        }
      }
      else {
        return false;
      }
    }
  }

  @Override
  public RESULT get() throws InterruptedException, ExecutionException {
    createAwaitCondition().await(); // Wait until the delegate is set or cancelled. That is crucial for mutex tasks which might be scheduled delayed if waiting to become mutex owner.
    return m_delegate.get();
  }

  @Override
  public RESULT get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
    final long nanosRemaining = createAwaitCondition().await(timeout, unit); // Wait until the delegate is set or cancelled, or the timeout elapses. That is crucial for mutex tasks which might be scheduled delayed if waiting to become mutex owner.
    if (m_delegate == null) {
      throw new TimeoutException();
    }
    else {
      return m_delegate.get(nanosRemaining, TimeUnit.NANOSECONDS);
    }
  }

  /**
   * Creates a {@link ConditionAwaiter} to block the current thread until the delegate is set or the Future cancelled.
   */
  private ConditionAwaiter createAwaitCondition() {
    return new ConditionAwaiter(m_changedLock, m_changedCondition) {

      @Override
      protected boolean evaluateCondition() {
        return m_delegate != null || m_cancelled.get();
      }
    };
  }
}
