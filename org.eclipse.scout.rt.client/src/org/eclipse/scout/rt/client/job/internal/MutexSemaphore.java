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
package org.eclipse.scout.rt.client.job.internal;

import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.commons.ToStringBuilder;

/**
 * Provides a thread-safe implementation of a non-blocking 1-permit {@link Semaphore} backed with a fair {@link Queue}.
 *
 * @param <T>
 *          the type of elements competing for the mutex.
 * @since 5.1
 */
public class MutexSemaphore<T> {

  private static final boolean POSITION_TAIL = true;
  private static final boolean POSITION_HEAD = false;

  private final AtomicInteger m_permits = new AtomicInteger(0);
  private final Deque<T> m_pendingQueue = new ArrayDeque<T>();

  final Lock m_idleLock = new ReentrantLock();
  final Condition m_idleCondition = m_idleLock.newCondition();

  private Thread m_modelThread; // The one thread currently representing the model-thread.
  private T m_mutexOwner; // The element currently owning the mutex.

  /**
   * Registers the current thread as model-thread.
   */
  public void registerAsModelThread() {
    synchronized (m_pendingQueue) {
      m_modelThread = Thread.currentThread();
    }
  }

  /**
   * @return <code>true</code> if the calling thread is the model-thread.
   */
  public boolean isModelThread() {
    return Thread.currentThread() == m_modelThread;
  }

  /**
   * @return the element currently owning the mutex.
   */
  public T getMutexOwner() {
    return m_mutexOwner;
  }

  /**
   * Tries to acquire the mutex if available at the time of invocation. Otherwise, the element is put into the queue of
   * pending elements and will compete for the mutex once all queued elements acquired/released the mutex.
   *
   * @param element
   *          the element to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   * @see #pollElseRelease()
   */
  public boolean tryAcquireElseOfferTail(final T element) {
    return tryAcquireElseOffer(element, POSITION_TAIL);
  }

  /**
   * Tries to acquire the mutex if available at the time of invocation. Otherwise, the element is put into the queue of
   * pending elements and will compete for the mutex as the very next element.
   *
   * @param element
   *          the element to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   * @see #pollElseRelease()
   */
  public boolean tryAcquireElseOfferHead(final T element) {
    return tryAcquireElseOffer(element, POSITION_HEAD);
  }

  protected boolean tryAcquireElseOffer(final T element, final boolean position) {
    synchronized (m_pendingQueue) {
      if (m_permits.getAndIncrement() == 0) {
        m_mutexOwner = element;
        return true;
      }
      else {
        if (position == POSITION_TAIL) {
          m_pendingQueue.offerLast(element);
        }
        else {
          m_pendingQueue.offerFirst(element);
        }
        return false;
      }
    }
  }

  /**
   * Passes the mutex to the oldest element in the queue. If no such competing element is available, the mutex is
   * released.
   *
   * @return the oldest element in the queue competing for the mutex or <code>null</code> if no such element is
   *         available and therefore the mutex was released.
   * @see #tryAcquireElseOffer(Object)
   */
  public T pollElseRelease() {
    synchronized (m_pendingQueue) {
      m_modelThread = null;
      m_mutexOwner = null;

      if (m_permits.get() == 0) { // the count is 0 if the queue was cleared.
        return null;
      }

      m_mutexOwner = m_pendingQueue.poll();
      if (m_permits.decrementAndGet() == 0) {
        signalIdle();
      }
      return m_mutexOwner;
    }
  }

  /**
   * @return <code>true</code> if the mutex is currently not acquired.
   */
  public boolean isIdle() {
    return m_permits.get() == 0;
  }

  /**
   * @return the number of elements currently competing for the mutex - this is the mutex-owner plus all pending
   *         elements; if <code>0</code>, the mutex is not acquired.
   */
  public int getPermitCount() {
    return m_permits.get();
  }

  /**
   * Blocks the calling thread until the mutex gets available. Does not block if available at time of invocation.
   *
   * @param timeout
   *          the maximal time to wait for the mutex to become available.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   * @see {@link #isIdle()}
   */
  public boolean waitForIdle(final long timeout, final TimeUnit unit) throws InterruptedException {
    if (isIdle()) {
      return true;
    }

    // Determine the absolute deadline.
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(System.currentTimeMillis() + unit.toMillis(timeout));
    final Date deadline = calendar.getTime();

    // Wait until idle or the deadline is passed.
    boolean deadlinePassed = false;
    m_idleLock.lockInterruptibly();
    try {
      while (!isIdle() && !deadlinePassed) { // spurious-wakeup-safe
        deadlinePassed = !m_idleCondition.awaitUntil(deadline);
      }
    }
    finally {
      m_idleLock.unlock();
    }

    return !deadlinePassed;
  }

  /**
   * Releases the mutex and clears all queued elements.
   */
  public void clear() {
    synchronized (m_pendingQueue) {
      m_permits.set(0);
      m_pendingQueue.clear();

      m_mutexOwner = null;
      m_modelThread = null;

      signalIdle();
    }
  }

  private void signalIdle() {
    m_idleLock.lock();
    try {
      m_idleCondition.signalAll();
    }
    finally {
      m_idleLock.unlock();
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("mutexOwner", m_mutexOwner);
    builder.attr("modelThread", m_modelThread);
    builder.attr("permits", m_permits);
    return builder.toString();
  }
}
