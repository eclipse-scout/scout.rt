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
package org.eclipse.scout.commons;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Helper class to await for a {@link Condition} to become <code>true</code>.
 *
 * @since 5.1
 */
public abstract class ConditionAwaiter {

  private final Lock m_lock;
  private final Condition m_condition;

  /**
   * @param lock
   *          the {@link Lock} associated with the {@link Condition}.
   * @param condition
   *          the {@link Condition} to await to become <code>true</code>.
   */
  public ConditionAwaiter(final Lock lock, final Condition condition) {
    m_lock = lock;
    m_condition = condition;
  }

  /**
   * Blocks the current thread until the condition evaluates to <code>true</code>, or the thread is interrupted.
   *
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting.
   */
  public void await() throws InterruptedException {
    m_lock.lockInterruptibly();
    try {
      while (!evaluateCondition()) {
        m_condition.await();
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Blocks the current thread until the condition evaluates to <code>true</code>, or the given timeout elapses, or the
   * thread is interrupted.
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @return an estimate of the remaining time in nanoseconds. A positive value indicates, that the condition evaluated
   *         to <code>true</code> within the timeout specified, a value less to zero that the timeout elapsed.
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting.
   */
  public long await(final long timeout, final TimeUnit unit) throws InterruptedException {
    m_lock.lockInterruptibly();
    try {
      long nanos = unit.toNanos(timeout);
      while (!evaluateCondition() && nanos > 0L) {
        nanos = m_condition.awaitNanos(nanos);
      }
      return nanos;
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Method invoked to evaluate the condition.
   *
   * @return <code>true</code> to indicate, that the condition is <code>true</code> and waiting threads should be
   *         released, <code>false</code> to keep them blocked.
   */
  protected abstract boolean evaluateCondition();
}
