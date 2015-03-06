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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.internal.Futures.JobFuture;

/**
 * Thread-Safe implementation of a {@link Set} to contain {@link IFuture}s.
 *
 * @since 5.1
 */
@Internal
public class FutureSet {

  private final Set<IFuture<?>> m_futures;

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;
  private final Condition m_futureRemovedCondition;

  public FutureSet() {
    m_futures = new HashSet<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
    m_futureRemovedCondition = m_writeLock.newCondition();
  }

  /**
   * Adds the Future supplied by the given {@link IFutureSupplier} to this {@link FutureSet}. Typically, the supplier
   * obtains the Future by a {@link ExecutorService}.<br/>
   * This {@link FutureSet} is locked exclusively during the time of supplying and adding the Future to this
   * {@link FutureSet}.
   *
   * @param futureSupplier
   *          supplier to obtain the {@link IFuture} to be added to this {@link FutureSet}.
   * @return {@link IFuture} that represents the added Future.
   * @throws JobExecutionException
   *           thrown if {@link IFutureSupplier#get()} throws a {@link RejectedExecutionException}.
   */
  public <RESULT, FUTURE extends IFuture<RESULT>> IFuture<RESULT> add(final IFutureSupplier<RESULT> futureSupplier) throws JobExecutionException {
    m_writeLock.lock();
    try {
      final IFuture<RESULT> future;
      try {
        future = Assertions.assertNotNull(futureSupplier.supply()).getFuture();
      }
      catch (final RejectedExecutionException e) {
        throw new JobExecutionException(e.getMessage(), e); // Task was rejected for execution.
      }

      m_futures.add(future);

      return future;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Removes the given {@link IFuture} from this {@link FutureSet}; during removal, the {@link FutureSet} is locked
   * exclusively.
   *
   * @param future
   *          {@link IFuture} to be removed.
   */
  public void remove(final IFuture<?> future) {
    m_writeLock.lock();
    try {
      m_futures.remove(future);
      m_futureRemovedCondition.signalAll();
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Clears this {@link FutureSet} and returns the removed Futures.
   *
   * @return {@link Set} of removed Futures.
   */
  public Set<IFuture<?>> clear() {
    m_writeLock.lock();
    try {
      final Set<IFuture<?>> futures = values();
      m_futures.clear();
      m_futureRemovedCondition.signalAll();
      return futures;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * @return <code>true</code> if there are no Futures contained.
   */
  public boolean isEmpty() {
    m_readLock.lock();
    try {
      return m_futures.isEmpty();
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * @return a copy of the underlying {@link Set}.
   */
  public Set<IFuture<?>> values() {
    m_readLock.lock();
    try {
      return new HashSet<>(m_futures);
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * @return exclusive {@link Lock} to access Futures.
   * @see #getFutureRemovedCondition()
   */
  Lock getLock() {
    return m_writeLock;
  }

  /**
   * @return Condition used to signal once Futures are removed from this Set.
   * @see #getLock()
   */
  Condition getFutureRemovedCondition() {
    return m_futureRemovedCondition;
  }

  /**
   * Supplies the {@link FutureSet} with a {@link JobFuture}.
   *
   * @param <RESULT>
   *          the result-type of the {@link IFuture}.
   */
  public interface IFutureSupplier<RESULT> {

    /**
     * This method is invoked to supply this {@link FutureSet} with a Future. Typically, the implementation obtains the
     * Future by scheduling the task on behalf of an {@link ExecutorService}. The Future returned must not be
     * <code>null</code>.
     *
     * @return Future to be added to the {@link FutureSet}; must not be <code>null</code>.
     * @throws RejectedExecutionException
     *           if the task was not accepted for execution and therefore is not to be added to the {@link FutureSet}.
     */
    JobFuture<RESULT> supply();
  }
}
