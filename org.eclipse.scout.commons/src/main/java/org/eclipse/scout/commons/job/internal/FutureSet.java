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
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.Executables;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IFutureVisitor;
import org.eclipse.scout.commons.job.JobExecutionException;

/**
 * Thread-Safe implementation of a {@link Set} to contain {@link Future}s.
 *
 * @since 5.1
 */
@Internal
public class FutureSet {

  private final Set<Future<?>> m_futures;

  private final ReadLock m_readLock;
  private final WriteLock m_writeLock;

  public FutureSet() {
    m_futures = new HashSet<>();

    final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    m_readLock = lock.readLock();
    m_writeLock = lock.writeLock();
  }

  /**
   * Adds the {@link Future} supplied by the given {@link FutureSupplier} to this {@link FutureSet}. The Future is only
   * added if not being 'cancelled' or 'done'. Typically, the supplier obtains the Future by a {@link ExecutorService}.<br/>
   * This {@link FutureSet} is locked exclusively during the time of supplying and adding the Future to this
   * {@link FutureSet}.
   *
   * @param identifier
   *          the identifier of the associated task used to create the {@link IFuture} to be returned.
   * @param futureSupplier
   *          supplier to obtain the {@link Future} to be added to this {@link FutureSet}.
   * @return {@link IFuture} that represents the added {@link Future}.
   * @throws JobExecutionException
   *           thrown if {@link FutureSupplier#get()} throws a {@link RejectedExecutionException}.
   */
  public <RESULT, FUTURE extends IFuture<RESULT>> IFuture<RESULT> add(final String identifier, final FutureSupplier<RESULT> futureSupplier) throws JobExecutionException {
    m_writeLock.lock();
    try {
      final Future<RESULT> future;
      try {
        future = Assertions.assertNotNull(futureSupplier.get());
      }
      catch (final RejectedExecutionException e) {
        throw new JobExecutionException(e.getMessage(), e); // Task was rejected for execution.
      }

      // Depending of the implementation of the Executor, the Future might be marked cancelled if not being accepted for execution.
      if (!future.isCancelled() && !future.isDone()) {
        m_futures.add(future);
      }

      return Executables.future(future, identifier);
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * Removes the given {@link Future} from this {@link FutureSet}; during removal, the {@link FutureSet} is locked
   * exclusively.
   *
   * @param future
   *          {@link Future} to be removed.
   */
  public void remove(final Future<?> future) {
    m_writeLock.lock();
    try {
      m_futures.remove(future);
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
  public Set<Future<?>> clear() {
    m_writeLock.lock();
    try {
      final Set<Future<?>> futures = copy();
      m_futures.clear();
      return futures;
    }
    finally {
      m_writeLock.unlock();
    }
  }

  /**
   * @return <code>true</code> if empty, <code>false</code> otherwise.
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
  public Set<Future<?>> copy() {
    m_readLock.lock();
    try {
      return new HashSet<>(m_futures);
    }
    finally {
      m_readLock.unlock();
    }
  }

  /**
   * To visit all {@link Future}s which did not complete yet.
   *
   * @param visitor
   *          {@link IFutureVisitor} called for each {@link Future}.
   */
  public void visit(final IFutureVisitor visitor) {
    for (final Future<?> future : copy()) {
      if (future.isDone()) {
        continue; // in case the job completed in the meantime.
      }
      if (!visitor.visit(future)) {
        return;
      }
    }
  }

  /**
   * Supplies the {@link FutureSet} with a {@link Future}.
   *
   * @param <RESULT>
   *          the result-type of the {@link Future}.
   */
  public interface FutureSupplier<RESULT> {

    /**
     * This method is invoked to supply this {@link FutureSet} with a {@link Future}. Typically, the implementation
     * obtains the {@link Future} by scheduling the task on behalf of an {@link ExecutorService}. The {@link Future}
     * returned must not be <code>null</code>.
     *
     * @return {@link Future} to be added to the {@link FutureSet}; is not added if being 'cancelled' or 'done'; must
     *         not be <code>null</code>.
     * @throws RejectedExecutionException
     *           if the task was not accepted for execution and therefore is not to be added to the {@link FutureSet}.
     */
    Future<RESULT> get();
  }
}
