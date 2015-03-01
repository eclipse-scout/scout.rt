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
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Provides a thread-safe implementation of a non-blocking 1-permit {@link Semaphore} backed with a fair {@link Queue}.
 *
 * @since 5.1
 */
@Internal
public class MutexSemaphore {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MutexSemaphore.class);

  private static final boolean POSITION_TAIL = true;
  private static final boolean POSITION_HEAD = false;

  private final AtomicInteger m_permits = new AtomicInteger(0);
  private final Deque<Task<?>> m_pendingQueue = new ArrayDeque<>();

  final Lock m_idleLock = new ReentrantLock();
  final Condition m_idleCondition = m_idleLock.newCondition();

  private Task<?> m_mutexOwner; // The task currently owning the mutex.

  /**
   * @return the task currently owning the mutex.
   */
  public Task<?> getMutexOwner() {
    return m_mutexOwner;
  }

  /**
   * Tries to acquire the mutex if available at the time of invocation. Otherwise, the task is put into the queue of
   * pending tasks and will compete for the mutex once all queued tasks acquired/released the mutex.
   *
   * @param task
   *          the model-task to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   * @see #releaseAndPoll()
   */
  public boolean tryAcquireElseOfferTail(final Task<?> task) {
    return tryAcquireElseOffer(task, POSITION_TAIL);
  }

  /**
   * Tries to acquire the mutex if available at the time of invocation. Otherwise, the task is put into the queue of
   * pending tasks and will compete for the mutex as the very next task.
   *
   * @param task
   *          the task to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   * @see #releaseAndPoll()
   */
  public boolean tryAcquireElseOfferHead(final Task<?> task) {
    return tryAcquireElseOffer(task, POSITION_HEAD);
  }

  protected boolean tryAcquireElseOffer(final Task<?> task, final boolean position) {
    synchronized (m_pendingQueue) {
      if (m_permits.getAndIncrement() == 0) {
        m_mutexOwner = task;
        return true;
      }
      else {
        if (position == POSITION_TAIL) {
          m_pendingQueue.offerLast(task);
        }
        else {
          m_pendingQueue.offerFirst(task);
        }
        return false;
      }
    }
  }

  /**
   * Passes the mutex to the first task in the queue.
   *
   * @return task which is the new mutex-owner, <code>null</code> if the queue was empty.
   * @see #tryAcquireElseOffer(Object)
   */
  public Task<?> releaseAndPoll() {
    synchronized (m_pendingQueue) {
      if (m_mutexOwner == null) {
        LOG.error("Unexpected inconsistency while releasing model mutex: mutex owner must not be null.");
      }

      m_mutexOwner = m_pendingQueue.poll();

      if (m_permits.get() > 0) {
        m_permits.decrementAndGet();
      }
      else {
        LOG.error("Unexpected inconsistency while releasing model mutex: permit count must not be 0.");
      }

      if (m_permits.get() == 0) {
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
   * @return the number of tasks currently competing for the mutex - this is the mutex-owner plus all pending
   *         tasks; if <code>0</code>, the mutex is not acquired.
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
    final Date deadline = new Date(System.currentTimeMillis() + unit.toMillis(timeout));

    // Wait until idle or the deadline is passed.
    m_idleLock.lockInterruptibly();
    try {
      while (!isIdle()) { // spurious-wakeup-safe
        if (!m_idleCondition.awaitUntil(deadline)) {
          return false; // timeout expired
        }
      }
      return true;
    }
    finally {
      m_idleLock.unlock();
    }
  }

  /**
   * Cancels the current mutex-owner and cancels and clears all queued tasks.
   */
  public void clearAndCancel() {
    synchronized (m_pendingQueue) {
      if (m_mutexOwner != null) {
        m_mutexOwner.getFuture().cancel(true);
      }

      for (final Task<?> pendingTask : m_pendingQueue) {
        pendingTask.getFuture().cancel(true);
      }
      m_permits.addAndGet(-m_pendingQueue.size()); // do not subtract the mutex-owner.
      m_pendingQueue.clear();
    }
  }

  /**
   * To visit all {@link Future}s which did not complete yet.
   *
   * @param visitor
   *          {@link IFutureVisitor} called for each {@link Future}.
   */
  public void visit(final IFutureVisitor visitor) {
    final List<Task<?>> tasks = new ArrayList<>();

    synchronized (m_pendingQueue) {
      tasks.add(m_mutexOwner);
      tasks.addAll(m_pendingQueue);
    }

    for (final Task<?> task : tasks) {
      if (task == null) {
        continue;
      }
      if (!visitor.visit(task.getFuture())) {
        return;
      }
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
    builder.attr("permits", m_permits);
    return builder.toString();
  }
}
