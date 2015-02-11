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
package org.eclipse.scout.commons.job.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobVisitor;
import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Map that contains running jobs and their associated {@link Future}.
 * <p/>
 * This class is thread-safe.
 *
 * @since 5.0
 */
public class JobMap {

  private final Map<IJob<?>, Future<?>> m_jobMap = new HashMap<>();
  private final Map<Future<?>, IJob<?>> m_futureMap = new HashMap<>();

  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
  private final WriteLock writeLock = lock.writeLock();
  private final ReadLock readLock = lock.readLock();

  /**
   * Puts the given job into the map of running jobs if not contained yet and if the {@link Future} returned by
   * {@link IPutCallback#onAbsent()} is not 'cancelled' nor 'done'.
   *
   * @param job
   *          the job to be put into the map.
   * @param callback
   *          callback invoked if the job was not contained in the map.
   * @return {@link Future} returned by {@link IPutCallback#onAbsent()}
   * @throws JobExecutionException
   *           if the given job is already contained in the map.
   */
  public <R, F extends Future<R>> F putIfAbsentElseReject(final IJob<R> job, final IPutCallback<R, F> callback) throws JobExecutionException {
    writeLock.lock();
    try {
      if (m_jobMap.containsKey(job)) {
        throw new JobExecutionException(String.format("Job rejected because already running [job=%s]", job.getName()), new RejectedExecutionException());
      }

      final F future = callback.onAbsent();
      Assertions.assertNotNull(future);

      // Register the future only if being accepted by the executor; otherwise it would never be removed.
      if (!future.isCancelled() && !future.isDone()) {
        m_jobMap.put(job, future);
        m_futureMap.put(future, job);
      }
      return future;
    }
    finally {
      writeLock.unlock();
    }
  }

  /**
   * Removes the job for the given {@link Future}.
   *
   * @param future
   *          {@link Future} to be removed.
   * @return <code>true</code> if removed, <code>false</code> otherwise.
   */
  public boolean remove(final Future<?> future) {
    writeLock.lock();
    try {
      return (m_jobMap.remove(m_futureMap.remove(future)) != null);
    }
    finally {
      writeLock.unlock();
    }
  }

  /**
   * Removes all contained jobs and futures.
   *
   * @return {@link Set} of the removed futures.
   */
  public Set<Future<?>> clear() {
    readLock.lock();
    try {
      final Set<Future<?>> futures = new HashSet<>(m_futureMap.keySet());

      m_jobMap.clear();
      m_futureMap.clear();

      return futures;
    }
    finally {
      readLock.unlock();
    }
  }

  /**
   * To visit all jobs which did not complete yet.
   *
   * @param visitor
   *          {@link IJobVisitor} called for each {@link IJob}.
   */
  public void visit(final IJobVisitor visitor) {
    final Map<IJob<?>, Future<?>> map = copyJobMap();

    for (final Entry<IJob<?>, Future<?>> entry : map.entrySet()) {
      final IJob<?> job = entry.getKey();
      final Future<?> future = entry.getValue();
      if (future.isDone()) {
        continue;
      }
      if (!visitor.visit(job)) {
        return;
      }
    }
  }

  /**
   * Attempts to cancel execution of the given job.
   *
   * @param job
   *          the job to be canceled.
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this job should be interrupted; otherwise, in-progress jobs
   *          are allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally;
   *         <code>true</code> otherwise.
   * @see Future#cancel(boolean)
   */
  public boolean cancel(final IJob<?> job, final boolean interruptIfRunning) {
    readLock.lock();
    try {
      final Future<?> future = getFuture(job);
      if (future != null) {
        return future.cancel(interruptIfRunning);
      }
      return false;
    }
    finally {
      readLock.unlock();
    }
  }

  /**
   * @return <code>true</code> if this {@link IJob} was cancelled before it completed normally.
   */
  public boolean isCancelled(final IJob<?> job) {
    readLock.lock();
    try {
      final Future<?> future = getFuture(job);
      return future != null && future.isCancelled();
    }
    finally {
      readLock.unlock();
    }
  }

  /**
   * @return the {@link Future} for the given job or <code>null</code> if not contained.
   */
  public Future<?> getFuture(final IJob<?> job) {
    return m_jobMap.get(job);
  }

  /**
   * @return a copy of the backing map.
   */
  public Map<IJob<?>, Future<?>> copyJobMap() {
    readLock.lock();
    try {
      return new HashMap<>(m_jobMap);
    }
    finally {
      readLock.unlock();
    }
  }

  /**
   * @return <code>true</code> if there are no jobs contained.
   */
  public boolean isEmpty() {
    readLock.lock();
    try {
      return m_jobMap.isEmpty();
    }
    finally {
      readLock.unlock();
    }
  }

  /**
   * @return number of contained jobs.
   */
  public int size() {
    readLock.lock();
    try {
      return m_jobMap.size();
    }
    finally {
      readLock.unlock();
    }
  }

  /**
   * Callback which is invoked if a job is not contained in the map yet.
   */
  public interface IPutCallback<R, F extends Future<R>> {
    /**
     * Method is invoked if the job is not contained in the map yet.
     *
     * @return {@link Future} that is associated with the job; must not be <code>null</code>; if being 'cancelled' or
     *         'done', the job is not put into the map.
     */
    F onAbsent();
  }
}
