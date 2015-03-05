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
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * Provides a thread-safe implementation of a non-blocking 1-permit-per-session semaphore backed with a fair queue. For
 * each session which jobs are scheduled for, a separate {@link MutexSemaphore} is created to hold the session's mutex
 * state.
 *
 * @since 5.1
 * @see MutexSemaphore
 */
@Internal
class MutexSemaphores {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MutexSemaphores.class);

  private static final boolean POSITION_TAIL = true;
  private static final boolean POSITION_HEAD = false;

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  protected final Lock m_mutexChangedLock;
  protected final Condition m_mutexChangedCondition;

  private final Map<IClientSession, MutexSemaphore> m_mutexSemaphores;

  private volatile boolean m_invalidated = false; // once being invalidated, tasks offered are rejected.

  MutexSemaphores() {
    m_mutexSemaphores = new HashMap<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();

    m_mutexChangedLock = new ReentrantLock();
    m_mutexChangedCondition = m_mutexChangedLock.newCondition();
  }

  /**
   * @param session
   *          {@link IClientSession} to get the mutex-owner for; must not be <code>null</code>.
   * @return the task currently owning the model-mutex for the given session.
   */
  ModelFutureTask<?> getMutexOwner(final IClientSession session) {
    Assertions.assertNotNull(session, "Session must not be null");

    m_readLock.lock();
    try {
      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(session);
      return (mutexSemaphore != null ? mutexSemaphore.getMutexOwner() : null);
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Tries to acquire the model-mutex for the given session. If not available at the time of invocation, the task is put
   * into the queue of pending tasks and will compete for the model-mutex once all queued tasks of that session
   * acquired/released the mutex.
   *
   * @param task
   *          the model-task to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   */
  boolean tryAcquireElseOfferTail(final ModelFutureTask<?> task) {
    return tryAcquireElseOffer(task, POSITION_TAIL);
  }

  /**
   * Tries to acquire the model-mutex for the given session. If not available at the time of invocation, the task is put
   * into the queue of pending tasks and will compete for the model-mutex of the given session as the very next task.<br/>
   * If this semaphore is invalidated, offered tasks are cancelled and rejected.
   *
   * @param task
   *          the task to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   */
  boolean tryAcquireElseOfferHead(final ModelFutureTask<?> task) {
    return tryAcquireElseOffer(task, POSITION_HEAD);
  }

  boolean tryAcquireElseOffer(final ModelFutureTask<?> task, final boolean position) {
    final IClientSession session = Assertions.assertNotNull(task.getJobInput().getSession(), "Session must not be null");

    m_writeLock.lock();
    try {
      if (m_invalidated) {
        task.cancel(true);
        return false;
      }

      MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(session);
      if (mutexSemaphore == null) {
        mutexSemaphore = new MutexSemaphore();
        m_mutexSemaphores.put(session, mutexSemaphore);
      }
      return mutexSemaphore.tryAcquireElseOffer(task, position);
    }
    finally {
      m_writeLock.unlock();
      signalMutexChanged(); // signal outside the monitor to not enter a deadlock.
    }
  }

  /**
   * Passes the session-mutex to the first task in the session-queue.
   *
   * @param task
   *          task which currently is the mutex-owner.
   * @return task which is the new mutex-owner, <code>null</code> if the queue was empty.
   */
  ModelFutureTask<?> releaseAndPoll(final ModelFutureTask<?> task) {
    final IClientSession session = Assertions.assertNotNull(task.getJobInput().getSession(), "Session must not be null");

    m_writeLock.lock();
    try {
      if (m_invalidated) {
        return null;
      }

      final MutexSemaphore mutexSemaphore = m_mutexSemaphores.get(session);
      if (mutexSemaphore == null) {
        LOG.error("Unexpected inconsistency while releasing model mutex: session-mutex-semaphore must not be null.");
        return null;
      }

      if (!mutexSemaphore.isMutexOwner(task)) {
        LOG.error("Unexpected inconsistency while releasing model mutex: wrong mutex owner [expected=%s, actual=%s].", task, mutexSemaphore.getMutexOwner());
      }

      final ModelFutureTask<?> nextTask = mutexSemaphore.releaseAndPoll();
      if (nextTask == null) {
        m_mutexSemaphores.remove(session);
      }
      return nextTask;
    }
    finally {
      m_writeLock.unlock();
      signalMutexChanged(); // signal outside the monitor to not enter a deadlock.
    }
  }

  /**
   * @param session
   *          {@link IClientSession} to get the permit count for; must not be <code>null</code>.
   * @return the number of tasks currently competing for the model-mutex of the given session - this is the mutex-owner
   *         plus all pending tasks; if <code>0</code>, the mutex is not acquired.
   */
  int getPermitCount(final IClientSession session) {
    Assertions.assertNotNull(session, "Session must not be null");

    m_readLock.lock();
    try {
      final MutexSemaphore sessionMutex = m_mutexSemaphores.get(session);
      return (sessionMutex != null ? sessionMutex.getPermitCount() : 0);
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Clear the queue. The semaphore cannot be used afterwards and offered tasks are rejected.
   */
  void invalidate() {
    m_writeLock.lock();
    try {
      for (final MutexSemaphore mutexSemaphore : m_mutexSemaphores.values()) {
        mutexSemaphore.reset();
      }
      m_mutexSemaphores.clear();
      m_invalidated = true;
    }
    finally {
      m_writeLock.unlock();
      signalMutexChanged(); // signal outside the monitor to not enter a deadlock.
    }
  }

  /**
   * @return all Futures which are currently registered.
   */
  Set<IFuture<?>> getFutures() {
    final Set<IFuture<?>> futures = new HashSet<>();
    m_readLock.lock();
    try {
      for (final MutexSemaphore mutexSemaphore : m_mutexSemaphores.values()) {
        futures.addAll(mutexSemaphore.getFutures());
      }
    }
    finally {
      m_readLock.unlock();
    }
    return futures;
  }

  /**
   * @return Lock used to signal changes to the mutex-semaphores.
   * @see #getMutexChangedCondition()
   */
  Lock getMutexChangedLock() {
    return m_mutexChangedLock;
  }

  /**
   * @return Condition used to signal changes to the mutex-semaphores.
   * @see #getMutexChangedLock()
   */
  Condition getMutexChangedCondition() {
    return m_mutexChangedCondition;
  }

  private void signalMutexChanged() {
    m_mutexChangedLock.lock();
    try {
      m_mutexChangedCondition.signalAll();
    }
    finally {
      m_mutexChangedLock.unlock();
    }
  }

  // === Mutex-Semaphore per session ===

  private static class MutexSemaphore {

    private final Deque<ModelFutureTask<?>> m_pendingQueue;

    private int m_permits;
    private ModelFutureTask<?> m_mutexOwner;

    private MutexSemaphore() {
      m_permits = 0;
      m_pendingQueue = new ArrayDeque<>();
    }

    private int getPermitCount() {
      return m_permits;
    }

    private boolean isMutexOwner(final ModelFutureTask<?> task) {
      return m_mutexOwner == task;
    }

    private ModelFutureTask<?> getMutexOwner() {
      return m_mutexOwner;
    }

    private boolean tryAcquireElseOffer(final ModelFutureTask<?> task, final boolean tail) {
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

    private ModelFutureTask<?> releaseAndPoll() {
      m_mutexOwner = m_pendingQueue.poll();

      m_permits--;

      if (m_permits < 0) {
        LOG.error("Unexpected inconsistency while releasing mutex: permit count must not be '0'.", m_permits);
        m_permits = 0;
      }

      return m_mutexOwner;
    }

    private List<IFuture<?>> getFutures() {
      final List<IFuture<?>> futures = new ArrayList<>();
      if (m_mutexOwner != null) {
        futures.add(m_mutexOwner.getFuture());
      }
      for (final ModelFutureTask<?> futureTask : m_pendingQueue) {
        futures.add(futureTask.getFuture());
      }
      return futures;
    }

    private void reset() {
      m_permits = 0;
      m_pendingQueue.clear();
      m_mutexOwner = null;
    }

    @Override
    public String toString() {
      final ToStringBuilder builder = new ToStringBuilder(this);
      builder.attr("mutexOwner", m_mutexOwner);
      builder.attr("pendingQueue", m_pendingQueue);
      builder.attr("permits", m_permits);
      return builder.toString();
    }
  }
}
