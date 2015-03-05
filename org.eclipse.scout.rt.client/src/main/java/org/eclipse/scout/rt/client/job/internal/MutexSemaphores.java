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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.filter.IFilter;
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
public class MutexSemaphores {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(MutexSemaphores.class);

  private static final boolean POSITION_TAIL = true;
  private static final boolean POSITION_HEAD = false;

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  private final Map<IClientSession, MutexSemaphore> m_mutexSemaphores;

  private boolean m_reject = false; // once the job manager is shutdown, new tasks are rejected and cancelled.

  public MutexSemaphores() {
    m_mutexSemaphores = new HashMap<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
  }

  /**
   * @param session
   *          {@link IClientSession} to get the mutex-owner for; must not be <code>null</code>.
   * @return the task currently owning the model-mutex for the given session.
   */
  public ModelFutureTask<?> getMutexOwner(final IClientSession session) {
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
  public boolean tryAcquireElseOfferTail(final ModelFutureTask<?> task) {
    return tryAcquireElseOffer(task, POSITION_TAIL);
  }

  /**
   * Tries to acquire the model-mutex for the given session. If not available at the time of invocation, the task is put
   * into the queue of pending tasks and will compete for the model-mutex of the given session as the very next task.
   *
   * @param task
   *          the task to acquire the mutex.
   * @return <code>true</code> if the mutex was acquired, <code>false</code> if being queued.
   */
  public boolean tryAcquireElseOfferHead(final ModelFutureTask<?> task) {
    return tryAcquireElseOffer(task, POSITION_HEAD);
  }

  protected boolean tryAcquireElseOffer(final ModelFutureTask<?> task, final boolean position) {
    final IClientSession session = Assertions.assertNotNull(task.getJobInput().getSession(), "Session must not be null");

    m_writeLock.lock();
    try {
      if (m_reject) {
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
    }
  }

  /**
   * Passes the session-mutex to the first task in the session-queue.
   *
   * @param task
   *          task which currently is the mutex-owner.
   * @return task which is the new mutex-owner, <code>null</code> if the queue was empty.
   */
  public ModelFutureTask<?> releaseAndPoll(final ModelFutureTask<?> task) {
    final IClientSession session = Assertions.assertNotNull(task.getJobInput().getSession(), "Session must not be null");

    m_writeLock.lock();
    try {
      if (m_reject) {
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
    }
  }

  /**
   * @param session
   *          {@link IClientSession} to get the permit count for; must not be <code>null</code>.
   * @return the number of tasks currently competing for the model-mutex of the given session - this is the mutex-owner
   *         plus all pending tasks; if <code>0</code>, the mutex is not acquired.
   */
  public int getPermitCount(final IClientSession session) {
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
   * @return <code>true</code> if there is no task matching the given Filter at the time of invocation.
   */
  public boolean isEmpty(final IFilter<IFuture<?>> filter) {
    m_readLock.lock();
    try {
      for (final MutexSemaphore mutexSemaphore : m_mutexSemaphores.values()) {
        if (!mutexSemaphore.isEmpty(filter)) {
          return false;
        }
      }
      return true;
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * Blocks the calling thread until all tasks which match the given Filter are removed from this semaphore, or the
   * given timeout elapses.
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting.
   */
  public boolean waitUntilEmpty(final IFilter<IFuture<?>> filter, final long timeout, final TimeUnit unit) throws InterruptedException {
    // Determine the absolute deadline.
    final Date deadline = new Date(System.currentTimeMillis() + unit.toMillis(timeout));

    while (!isEmpty(filter)) {
      final Set<MutexSemaphore> mutexSemaphores = new HashSet<>();
      m_readLock.lock();
      try {
        mutexSemaphores.addAll(m_mutexSemaphores.values());
      }
      finally {
        m_readLock.unlock();
      }

      for (final MutexSemaphore mutexSemaphore : mutexSemaphores) {
        if (!mutexSemaphore.waitUntilEmpty(filter, deadline)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * To visit Futures which did not complete yet and passes the filter.
   *
   * @param filter
   *          to limit the Futures to be visited.
   * @param visitor
   *          called for each Futures that passed the filter.
   */
  public void visit(final IFilter<IFuture<?>> filter, final IVisitor<IFuture<?>> visitor) {
    final Set<MutexSemaphore> mutexSemaphores = new HashSet<>();
    m_readLock.lock();
    try {
      mutexSemaphores.addAll(m_mutexSemaphores.values());
    }
    finally {
      m_readLock.unlock();
    }

    for (final MutexSemaphore mutexSemaphore : mutexSemaphores) {
      for (final IFuture<?> future : mutexSemaphore.getFutures()) {
        if (future.isDone() || !filter.accept(future)) {
          continue;
        }
        if (!visitor.visit(future)) {
          return;
        }
      }
    }
  }

  /**
   * Clear the queue. The semaphore cannot be used afterwards and offered tasks are rejected.
   */
  public void clear() {
    m_writeLock.lock();
    try {
      for (final MutexSemaphore mutexSemaphore : m_mutexSemaphores.values()) {
        mutexSemaphore.clear();
      }
      m_mutexSemaphores.clear();
      m_reject = true;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    m_readLock.lock();
    try {
      for (final Entry<IClientSession, MutexSemaphore> entry : m_mutexSemaphores.entrySet()) {
        builder.attr(entry.getKey().getUserId(), entry.getValue());
      }
    }
    finally {
      m_readLock.unlock();
    }
    return builder.toString();
  }
}
