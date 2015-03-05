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

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJobInput;
import org.eclipse.scout.commons.job.internal.Futures;
import org.eclipse.scout.commons.job.internal.IProgressMonitorProvider;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.ClientJobInput;

/**
 * Future to be scheduled once the model-mutex is acquired.
 *
 * @since 5.1
 */
@Internal
public class ModelFutureTask<RESULT> extends FutureTask<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelFutureTask.class);

  private final IFuture<RESULT> m_future;
  private final ClientJobInput m_jobInput;
  private final Long m_expirationDate;

  private volatile boolean m_running; // indicates that this Future started running.
  private volatile IMutexAcquiredListener m_mutexAcquiredListener;

  public ModelFutureTask(final Callable<RESULT> callable, final ClientJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
    super(callable);
    m_jobInput = input;
    m_future = Futures.iFuture(this, input, progressMonitorProvider);

    final long expirationTime = input.getExpirationTimeMillis();
    m_expirationDate = (expirationTime != IJobInput.INFINITE_EXPIRATION ? System.currentTimeMillis() + expirationTime : null);
  }

  /**
   * @return {@link IFuture} associated with this {@link FutureTask}.
   */
  public final IFuture<RESULT> getFuture() {
    return m_future;
  }

  /**
   * @return {@link ClientJobInput} associated with this {@link FutureTask}.
   */
  public ClientJobInput getJobInput() {
    return m_jobInput;
  }

  @Override
  @Internal
  public void run() {
    notifyMutexAcquiredListeners();

    // If this task is already in 'running'-state, the task was waiting for a blocking condition to fall and now tries to re-acquire the mutex.
    // If so, do not run the task again. By notifying the listeners about the mutex-acquisition, the blocked task is now continuing its work.
    if (!m_running) {
      notifyBefore();
      try {
        m_running = true;
        super.run(); // delegate control to the FutureTask which in turn calls 'Callable.call'.
      }
      finally {
        notifyAfter();
      }
    }
  }

  /**
   * @return <code>true</code> if the expiration time of this Future has elapsed and should be discarded by the job
   *         manager without running, <code>false</code> otherwise.
   */
  protected boolean isExpired() {
    if (m_running) {
      return false;
    }
    else {
      return (m_expirationDate == null ? false : System.currentTimeMillis() > m_expirationDate);
    }
  }

  /**
   * Invoke this method if the Future was rejected for execution.
   */
  public final void reject() {
    notifyMutexAcquiredListeners();

    // If this task is already in 'running'-state, the task was waiting for a blocking condition to fall and now tries to re-acquire the mutex.
    // If so, ignore rejection because the blocked task lives in its own thread.
    if (!m_running) {
      rejected(m_future);
    }
  }

  /**
   * Method invoked if the executor rejected this task from being scheduled. This may occur when no more threads or
   * queue slots are available because their bounds would be exceeded, or upon shutdown of the executor. This method is
   * invoked from the thread that scheduled this task. When being invoked, this task is the mutex-owner.
   *
   * @param future
   *          {@link IFuture} representing this task.
   */
  protected void rejected(final IFuture<RESULT> future) {
  }

  /**
   * Method invoked if this task was accepted by the executor immediately before this task is executed. This method is
   * also invoked for <code>cancelled</code> tasks. This method is invoked by the thread that will execute this task.
   * When being invoked, this task is the mutex-owner.
   *
   * @param future
   *          {@link IFuture} representing this task.
   */
  protected void beforeExecute(final IFuture<RESULT> future) {
  }

  /**
   * Method invoked after executing this task. If the task was <code>cancelled</code> before running, this method is
   * called immediately after {@link #beforeExecute()}. This method is invoked by the thread that executed this task.
   * When being invoked, this task is still the mutex-owner.
   *
   * @param future
   *          {@link IFuture} representing this task.
   */
  protected void afterExecute(final IFuture<RESULT> future) {
  }

  /**
   * Sets the given listener to be notified once the mutex is acquired.
   */
  void setMutexAcquiredListener(final IMutexAcquiredListener listener) {
    m_mutexAcquiredListener = listener;
  }

  private void notifyBefore() {
    try {
      beforeExecute(m_future);
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected error in 'beforeExecute'", e);
    }
  }

  private void notifyAfter() {
    try {
      afterExecute(m_future);
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected error in 'afterExecute'", e);
    }
  }

  private void notifyMutexAcquiredListeners() {
    final IMutexAcquiredListener listener = m_mutexAcquiredListener;

    if (listener != null) {
      try {
        listener.onMutexAcquired();
      }
      catch (final RuntimeException e) {
        LOG.error("Unexpected error while notifying listener about mutex-acquisition", e);
      }
    }
    else if (m_running) {
      LOG.error("Unexpected: Job re-acquired model-mutex but no {} is installed. [job={}]", IMutexAcquiredListener.class.getSimpleName(), this);
    }
  }

  /**
   * Listener to be notified once acquiring the model-mutex.
   */
  interface IMutexAcquiredListener {

    /**
     * Method invoked once the model-mutex is acquired.
     */
    void onMutexAcquired();
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_jobInput.getIdentifier());
    return builder.toString();
  }
}
