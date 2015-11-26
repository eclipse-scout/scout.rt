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
package org.eclipse.scout.rt.testing.commons;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A synchronization aid similar to {@link CountDownLatch} but blocks the current thread when invoking
 * {@link #countDownAndBlock()}.
 */
public final class BlockingCountDownLatch {

  private final long m_defaultAwaitTime;
  private final TimeUnit m_defaultAwaitUnit;

  private final ReentrantLock m_lock;
  private final Condition m_unblockCondition;
  private final Condition m_zeroCountCondition;

  private boolean m_blocking;
  private int m_count;

  /**
   * Creates a {@link BlockingCountDownLatch} with default timeouts of 30 seconds.
   *
   * @param count
   *          the number of times {@link #countDown} must be invoked before threads can pass through {@link #await}
   */
  public BlockingCountDownLatch(final int count) {
    this(count, 30, TimeUnit.SECONDS);
  }

  /**
   * Creates a {@link BlockingCountDownLatch} with default timeouts of 30 seconds.
   *
   * @param count
   *          the number of times {@link #countDown} must be invoked before threads can pass through {@link #await}
   * @param defaultAwaitTime
   *          default timeout to wait for in {@link #await()} and {@link #countDownAndBlock()}.
   * @param defaultAwaitUnit
   *          unit of the timeout.
   */
  public BlockingCountDownLatch(final int count, final long defaultAwaitTime, final TimeUnit defaultAwaitUnit) {
    m_count = count;
    m_blocking = true;

    m_defaultAwaitTime = defaultAwaitTime;
    m_defaultAwaitUnit = defaultAwaitUnit;

    m_lock = new ReentrantLock();
    m_unblockCondition = m_lock.newCondition();
    m_zeroCountCondition = m_lock.newCondition();
  }

  /**
   * Waits until the latch is count down to <code>0</code>.<br/>
   * This method blocks interruptible until the default timeout expires.
   *
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   */
  public boolean await() throws InterruptedException {
    final Date deadline = calculateDeadline(m_defaultAwaitTime, m_defaultAwaitUnit);

    m_lock.lock();
    try {
      while (m_count > 0) { // spurious wakeup
        if (!m_zeroCountCondition.awaitUntil(deadline)) {
          return false; // timeout expired
        }
        checkInterrupted();
      }
      return true;
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Releases all waiting threads blocked by {@link #await()}.
   */
  public void release() {
    m_lock.lock();
    try {
      m_count = 0;
      m_zeroCountCondition.signalAll();
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Releases all waiting threads blocked by {@link #countDownAndBlock()}.
   */
  public void unblock() {
    m_lock.lock();
    try {
      m_blocking = false;
      m_unblockCondition.signalAll();
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Counts the latch down.
   */
  public void countDown() {
    m_lock.lock();
    try {
      if (--m_count == 0) {
        m_zeroCountCondition.signalAll();
      }
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Counts the latch down and blocks the current thread until {@link #unblock()} is called.<br/>
   * This method blocks interruptible until the specified timeout expires.
   *
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   */
  public boolean countDownAndBlock(long time, TimeUnit unit) throws InterruptedException {
    final Date deadline = calculateDeadline(time, unit);

    m_lock.lock();
    try {
      countDown();
      while (m_blocking) { // spurious wakeup
        if (!m_unblockCondition.awaitUntil(deadline)) {
          return false; // timeout expired
        }
        checkInterrupted();
      }
      return true;
    }
    finally {
      m_lock.unlock();
    }
  }

  /**
   * Counts the latch down and blocks the current thread until {@link #unblock()} is called.<br/>
   * This method blocks interruptible until the default timeout expires.
   *
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   */
  public boolean countDownAndBlock() throws InterruptedException {
    return countDownAndBlock(m_defaultAwaitTime, m_defaultAwaitUnit);
  }

  /**
   * @return maximal deadline to wait.
   */
  private Date calculateDeadline(long time, TimeUnit unit) {
    return new Date(System.currentTimeMillis() + unit.toMillis(time));
  }

  /**
   * Throws an {@link InterruptedException} if the current thread is interrupted.
   * <p>
   * That is because a thread's interruption is asynchronous, meaning that once a thread (a) interrupts another threads
   * (b), and in turn thread (a) unblocks the condition which thread (b) is waiting for, it is not ensured that thread
   * (b) exists with an {@link InterruptedException}. However, many tests expect exactly that behavior.
   * <p>
   * Example of reproducer:
   *
   * <pre>
   * &#64;Test
   * &#64;Times(100)
   * public void test() throws InterruptedException {
   *   final AtomicBoolean interrupted = new AtomicBoolean();
   *
   *   final AtomicBoolean readyFlag = new AtomicBoolean(false);
   *   final CountDownLatch finishedLatch = new CountDownLatch(1);
   *
   *   Future<?> future = Executors.newFixedThreadPool(1).submit(new Runnable() {
   *
   *     &#64;Override
   *     public void run() {
   *       try {
   *         synchronized (readyFlag) {
   *           readyFlag.set(true);
   *           readyFlag.wait();
   *         }
   *       }
   *       catch (InterruptedException e) {
   *         interrupted.set(true);
   *       }
   *       finally {
   *         finishedLatch.countDown();
   *       }
   *     }
   *   });
   *
   *   // Wait until the task is waiting for the condition to fall
   *   while (!readyFlag.get()) {
   *     Thread.yield();
   *   }
   *
   *   // Cancel the task and notify immediately
   *   synchronized (readyFlag) {
   *     future.cancel(true);
   *     readyFlag.notify();
   *   }
   *
   *   finishedLatch.await();
   *   assertTrue(interrupted.get());
   * }
   * </pre>
   */
  private static void checkInterrupted() throws InterruptedException {
    if (Thread.currentThread().isInterrupted()) {
      throw new InterruptedException();
    }
  }
}
