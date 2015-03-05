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
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Provides a thread-safe implementation of a non-blocking 1-permit semaphore backed with a fair queue.
 *
 * @since 5.1
 */
@Internal
class MutexSemaphore {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MutexSemaphore.class);

  private final Deque<ModelFutureTask<?>> m_pendingQueue;

  private volatile int m_permits; // volatile because being read by different threads.
  private volatile ModelFutureTask<?> m_mutexOwner; // volatile because being read by different threads.

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  protected final Lock m_changedLock;
  protected final Condition m_changedCondition;

  MutexSemaphore() {
    m_permits = 0;
    m_pendingQueue = new ArrayDeque<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();

    m_changedLock = new ReentrantLock();
    m_changedCondition = m_changedLock.newCondition();
  }

  int getPermitCount() {
    return m_permits;
  }

  boolean isMutexOwner(final ModelFutureTask<?> task) {
    return m_mutexOwner == task;
  }

  ModelFutureTask<?> getMutexOwner() {
    return m_mutexOwner;
  }

  boolean tryAcquireElseOffer(final ModelFutureTask<?> task, final boolean tail) {
    m_writeLock.lock();
    try {
      if (m_permits++ == 0) {
        m_mutexOwner = task;
        return true;
      }
      else {
        if (tail) {
          m_pendingQueue.offerLast(task);
        }
        else {
          m_pendingQueue.offerFirst(task);
        }
        return false;
      }
    }
    finally {
      m_writeLock.unlock();
      signalChanged(); // signal outside the monitor.
    }
  }

  ModelFutureTask<?> releaseAndPoll() {
    m_writeLock.lock();
    try {
      m_mutexOwner = m_pendingQueue.poll();

      m_permits--;

      if (m_permits < 0) {
        LOG.error("Unexpected inconsistency while releasing model mutex: permit count must not be 0.");
        m_permits = 0;
      }
      return m_mutexOwner;
    }
    finally {
      m_writeLock.unlock();
      signalChanged(); // signal outside the monitor.
    }

  }

  List<IFuture<?>> getFutures() {
    m_readLock.lock();
    try {
      final List<IFuture<?>> futures = new ArrayList<>();
      if (m_mutexOwner != null) {
        futures.add(m_mutexOwner.getFuture());
      }
      for (final ModelFutureTask<?> futureTask : m_pendingQueue) {
        futures.add(futureTask.getFuture());
      }
      return futures;
    }
    finally {
      m_readLock.unlock();
    }
  }

  public boolean isEmpty(final IFilter<IFuture<?>> filter) {
    m_readLock.lock();
    try {
      for (final IFuture<?> future : getFutures()) {
        if (filter.accept(future)) {
          return false;
        }
      }
      return true;
    }
    finally {
      m_readLock.unlock();
    }
  }

  public boolean waitUntilEmpty(final IFilter<IFuture<?>> filter, final Date deadline) throws InterruptedException {
    if (isEmpty(filter)) {
      return true;
    }

    // Wait until empty or the deadline is passed.
    m_changedLock.lockInterruptibly();
    try {
      while (!isEmpty(filter)) { // spurious-wakeup-safe
        if (!m_changedCondition.awaitUntil(deadline)) {
          return false; // timeout expired
        }
      }
      return true;
    }
    finally {
      m_changedLock.unlock();
    }
  }

  public void reset() {
    m_writeLock.lock();
    try {
      m_permits = 0;
      m_mutexOwner = null;
      m_pendingQueue.clear();
    }
    finally {
      m_writeLock.unlock();
      signalChanged(); // signal outside the monitor.
    }
  }

  private void signalChanged() {
    m_changedLock.lock();
    try {
      m_changedCondition.signalAll();
    }
    finally {
      m_changedLock.unlock();
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
