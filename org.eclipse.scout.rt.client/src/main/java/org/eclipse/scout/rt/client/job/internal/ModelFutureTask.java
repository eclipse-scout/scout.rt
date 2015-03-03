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

import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.internal.Futures;
import org.eclipse.scout.commons.job.internal.IProgressMonitorProvider;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.client.job.ClientJobInput;

/**
 * {@link Callable} to be scheduled once the model-mutex is acquired.
 *
 * @since 5.1
 */
@Internal
public class ModelFutureTask<RESULT> extends FutureTask<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(ModelFutureTask.class);

  private final IFuture<RESULT> m_future;

  public ModelFutureTask(final Callable<RESULT> callable, final ClientJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
    super(callable);
    m_future = Futures.iFuture(this, input, progressMonitorProvider);
  }

  public ModelFutureTask(final ClientJobInput input, final IProgressMonitorProvider progressMonitorProvider) {
    this(new Callable<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return null; // NOOP
      }
    }, input, progressMonitorProvider);
  }

  /**
   * @return {@link IFuture} associated with this {@link FutureTask}.
   */
  public final IFuture<RESULT> getFuture() {
    return m_future;
  }

  @Override
  @Internal
  public void run() {
    try {
      beforeExecute(m_future);
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected error in 'beforeExecute'", e);
    }

    try {
      super.run(); // delegate control to the FutureTask which in turn calls 'Callable.call'.
    }
    finally {
      try {
        afterExecute(m_future);
      }
      catch (final RuntimeException e) {
        LOG.error("Unexpected error in 'afterExecute'", e);
      }
    }
  }

  /**
   * Invoke this method if the Future was rejected for execution.
   */
  public final void reject() {
    rejected(m_future);
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
}
