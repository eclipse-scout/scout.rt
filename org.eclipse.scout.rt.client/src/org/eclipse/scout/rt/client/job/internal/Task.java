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

import org.eclipse.scout.commons.ToStringBuilder;

/**
 * {@link Callable} to be scheduled once the model-mutex is acquired.
 *
 * @since 5.1
 */
public abstract class Task<R> implements Callable<R> {

  private final String m_jobName;
  private final Executor m_executor;
  private final MutexSemaphore<Task<?>> m_mutexSemaphore;
  private final FutureTaskEx<R> m_futureTask;

  public Task(final String jobName, final Executor executor, final MutexSemaphore<Task<?>> mutexSemaphore) {
    m_jobName = jobName;
    m_executor = executor;
    m_mutexSemaphore = mutexSemaphore;
    m_futureTask = new FutureTaskEx<R>(this);
  }

  @Override
  public final R call() throws Exception {
    return onCall();
  }

  /**
   * Schedules this task to be executed at the next reasonable opportunity. The invoker must ensure that this task is
   * the mutex-owner.
   *
   * @throws IllegalStateException
   *           if this task is not the mutex-owner.
   */
  public final void schedule() {
    if (m_mutexSemaphore.getMutexOwner() != this) {
      throw new IllegalStateException(String.format("Task rejected because not being the mutex-owner. [job=%s, mutexSemaphore=%s]", m_jobName, m_mutexSemaphore));
    }

    m_executor.execute(m_futureTask);
  }

  /**
   * @return the {@link Future} representing this task.
   */
  public final Future<R> getFuture() {
    return m_futureTask;
  }

  /**
   * Method invoked if this task was accepted by the executor immediately before this task is executed. This method is
   * also invoked for <code>cancelled</code> tasks. This method is invoked by the thread that will execute this task.
   * When being invoked, this task is the mutex-owner.
   *
   * @see #onRejected()
   */
  protected void onBefore() {
  }

  /**
   * Method invoked after executing this task. If the task was <code>cancelled</code> before running, this method is
   * called immediately after {@link #onBefore()}. This method is invoked by the thread that executed this task. When
   * being invoked, this task is still the mutex-owner.
   */
  protected void onAfter() {
  }

  /**
   * Method invoked if the executor rejected this task from being scheduled. This may occur when no more threads or
   * queue slots are available because their bounds would be exceeded, or upon shutdown of the executor. This method is
   * invoked from the thread that scheduled this task. When being invoked, this task is the mutex-owner.
   *
   * @see #onBefore()
   */
  protected void onRejected() {
  }

  /**
   * Method invoked to execute this task. This method is not invoked for <code>cancelled</code> tasks.
   *
   * @return computed result of this task.
   * @throws Exception
   *           if unable to compute a result.
   */
  protected abstract R onCall() throws Exception;

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_jobName);
    builder.ref("future", m_futureTask);
    return builder.toString();
  }
}
