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
package org.eclipse.scout.rt.platform.job.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneCallback;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;

/**
 * This class listens for the Future's completion and notifies registered callbacks. This class has the semantic of a
 * <code>promise</code>, meaning that a callback is also notified if the Future is already in 'done' state.
 *
 * @since 5.1
 */
class DonePromise<RESULT> {

  private final ReadWriteLock m_lock;

  private final IFuture<RESULT> m_future;
  private final List<IDoneCallback<RESULT>> m_callbacks;

  private volatile DoneEvent<RESULT> m_doneEvent;

  public DonePromise(final IFuture<RESULT> future) {
    m_future = future;
    m_lock = new ReentrantReadWriteLock();
    m_callbacks = new ArrayList<>();
  }

  /**
   * Method invoked once the Future is in 'done'-state.
   */
  void onDone() {
    m_lock.writeLock().lock();
    try {
      // Event creation
      try {
        m_doneEvent = new DoneEvent<>(m_future.awaitDoneAndGet(BEANS.get(ExceptionTranslator.class)), null, false);
      }
      catch (final Exception e) {
        if (e instanceof CancellationException) {
          m_doneEvent = new DoneEvent<>(null, null, true);
        }
        else {
          m_doneEvent = new DoneEvent<>(null, e, false);
        }
      }
      catch (final Throwable t) {
        m_doneEvent = new DoneEvent<>(null, new Exception("Unexpected exception while querying the Future's result", t), m_future.isCancelled());
      }

      // Callback notification
      final Iterator<IDoneCallback<RESULT>> iterator = m_callbacks.iterator();
      while (iterator.hasNext()) {
        final IDoneCallback<RESULT> callback = iterator.next();
        iterator.remove();

        Jobs.schedule(new IRunnable() {

          @Override
          public void run() throws Exception {
            callback.onDone(m_doneEvent);
          }
        }, Jobs.newInput().withName("'Future#whenDone' notification"));
      }
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * Registers the given <code>callback</code> to be notified once the Future enters 'done' state. That is once the
   * associated job completes successfully or with an exception, or was cancelled. Thereby, the callback is invoked in
   * any thread with no {@code RunContext} set. If the job is already in 'done' state when the callback is registered,
   * the callback is invoked immediately.
   */
  public void whenDone(final IDoneCallback<RESULT> callback) {
    m_lock.readLock().lock();
    try {
      if (m_doneEvent != null) {
        // Future is already in 'done' state.
        callback.onDone(m_doneEvent);
      }
      else {
        // Future not in 'done' state yet.
        m_callbacks.add(callback);
      }
    }
    finally {
      m_lock.readLock().unlock();
    }
  }
}
