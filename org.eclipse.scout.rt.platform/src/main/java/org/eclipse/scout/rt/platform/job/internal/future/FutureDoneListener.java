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
package org.eclipse.scout.rt.platform.job.internal.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.IDoneCallback;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * This class listens for the Future's completion and notifies registered callbacks accordingly. This class has the
 * semantic of a <code>premise</code>, meaning that a callback is also notified if the Future is already in 'done'
 * state.
 *
 * @since 5.1
 */
class FutureDoneListener<RESULT> {

  private final ReadWriteLock m_lock;
  private final List<IDoneCallback<RESULT>> m_callbacks;
  private volatile DoneEvent<RESULT> m_event;

  public FutureDoneListener(final IFuture<RESULT> future) {
    m_lock = new ReentrantReadWriteLock();
    m_callbacks = new ArrayList<>();

    final IJobManager jobManager = Jobs.getJobManager();
    jobManager.addListener(Jobs.newEventFilter().futures(future).eventTypes(JobEventType.DONE), new IJobListener() {

      @Override
      public void changed(final JobEvent event) {
        jobManager.removeListener(this);
        onDone(future);
      }
    });
  }

  /**
   * Method invoked once the Future is in 'done'-state.
   */
  private void onDone(final IFuture<RESULT> future) {
    m_lock.writeLock().lock();
    try {
      // Create the 'done' event.
      try {
        m_event = new DoneEvent<>(future.awaitDoneAndGet(), null, future.isCancelled());
      }
      catch (final ProcessingException e) {
        m_event = new DoneEvent<>(null, e, future.isCancelled());
      }
      catch (final Throwable t) {
        m_event = new DoneEvent<>(null, new ProcessingException("Unexpected exception while querying the Future's result", t), future.isCancelled());
      }

      // Notify registered callbacks asynchronously.
      FutureDoneListener.notifyListenersAsync(new ArrayList<>(m_callbacks), m_event);
      m_callbacks.clear();
    }
    finally {
      m_lock.writeLock().unlock();
    }
  }

  /**
   * Registers the given <code>callback</code> to be notified once the Future enters 'done' state. That is once the
   * associated job completes successfully or with an exception, or was cancelled.
   * <p/>
   * If the job is already in 'done' state when the callback is registered, the callback is invoked immediately.
   * However, the callback is invoked in any thread with no {@code RunContext} set.
   */
  public void whenDone(final IDoneCallback<RESULT> callback) {
    m_lock.readLock().lock();
    try {
      if (m_event != null) {
        // Future is already in 'done' state.
        FutureDoneListener.notifyListenersAsync(Collections.singleton(callback), m_event);
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

  /**
   * Invoke this method to asynchronously notify all given callbacks.
   */
  private static <RESULT> void notifyListenersAsync(final Collection<IDoneCallback<RESULT>> callbacks, final DoneEvent<RESULT> event) {
    for (final IDoneCallback<RESULT> callback : callbacks) {
      Jobs.schedule(new IRunnable() {

        @Override
        public void run() throws Exception {
          callback.onDone(event);
        }
      }, Jobs.newInput(null).name("callback notification"));
    }
  }
}
