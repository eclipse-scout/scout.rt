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
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.rt.client.job.ClientJobInput;

/**
 * {@link Callable} to be scheduled once the model-mutex is acquired.
 *
 * @since 5.1
 */
@Internal
public abstract class Task<RESULT> implements Callable<RESULT> {

  private final Executor m_executor;
  private final ClientJobInput m_input;
  private final ModelJobFuture<RESULT> m_runnableFuture;
  private final MutexSemaphore m_mutexSemaphore;

  public Task(final Executor executor, final MutexSemaphore mutexSemaphore, final ClientJobInput input) {
    m_executor = executor;
    m_input = input;
    m_mutexSemaphore = mutexSemaphore;
    m_runnableFuture = Assertions.assertNotNull(interceptFuture(new ModelJobFuture<>(this, input)));
  }

  @Override
  public final RESULT call() throws Exception {
    return execute(m_runnableFuture);
  }

  /**
   * Method invoked to intercept the Future given to the executor for execution.
   *
   * @param future
   *          Future to be adapted.
   * @return adapted Future.
   */
  protected ModelJobFuture<RESULT> interceptFuture(final ModelJobFuture<RESULT> future) {
    return future;
  }

  /**
   * Schedules this task to be executed at the next reasonable opportunity. The invoker must ensure that this task is
   * the mutex-owner.
   */
  public final void schedule() {
    Assertions.assertTrue(m_mutexSemaphore.getMutexOwner() == this, "Unexpected inconsistency: Task rejected because not being the mutex-owner. [task=%s]", this);
    m_executor.execute(m_runnableFuture);
  }

  /**
   * @return the {@link Future} representing this task.
   */
  public ModelJobFuture<RESULT> getFuture() {
    return m_runnableFuture;
  }

  /**
   * Method invoked if the executor rejected this task from being scheduled. This may occur when no more threads or
   * queue slots are available because their bounds would be exceeded, or upon shutdown of the executor. This method is
   * invoked from the thread that scheduled this task. When being invoked, this task is the mutex-owner.
   *
   * @param future
   *          the Future associated with this task.
   */
  protected void rejected(final ModelJobFuture<RESULT> future) {
  }

  /**
   * Method invoked if this task was accepted by the executor immediately before this task is executed. This method is
   * also invoked for <code>cancelled</code> tasks. This method is invoked by the thread that will execute this task.
   * When being invoked, this task is the mutex-owner.
   *
   * @param future
   *          Future associated with this task.
   */
  protected void beforeExecute(final ModelJobFuture<RESULT> future) {
  }

  /**
   * Method invoked to execute the task; is not called if the Future was cancelled.
   *
   * @param future
   *          Future associated with this task.
   * @return return value or <code>null</code>.
   * @throws Exception
   *           exception occurred during task execution.
   */
  protected abstract RESULT execute(ModelJobFuture<RESULT> future) throws Exception;

  /**
   * Method invoked after executing this task. If the task was <code>cancelled</code> before running, this method is
   * called immediately after {@link #beforeExecute()}. This method is invoked by the thread that executed this task.
   * When being invoked, this task is still the mutex-owner.
   *
   * @param future
   *          the Future associated with this task.
   */
  protected void afterExecute(final ModelJobFuture<RESULT> future) {
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input.getIdentifier("n/a"));
    builder.ref("future", m_runnableFuture);
    return builder.toString();
  }
}
