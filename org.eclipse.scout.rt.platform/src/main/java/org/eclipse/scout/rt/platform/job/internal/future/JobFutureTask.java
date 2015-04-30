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

import java.util.concurrent.CancellationException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.ICancellable;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IDoneCallback;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobException;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.internal.MutexSemaphores;

/**
 * Future to be given to the executor for execution. This class combines both, the Executable and its Future.
 *
 * @see FutureTask
 * @since 5.1
 */
@Internal
public class JobFutureTask<RESULT> extends FutureTask<RESULT> implements IFutureTask<RESULT>, IFuture<RESULT>, ICancellable {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobFutureTask.class);

  protected final JobInput m_input;
  protected final Long m_expirationDate;
  private volatile boolean m_blocked;
  private final boolean m_periodic;

  private final MutexSemaphores m_mutexSemaphores;
  private final FutureDoneListener<RESULT> m_futureListener;

  public JobFutureTask(final JobInput input, final boolean periodic, final MutexSemaphores mutexSemaphores, final ICallable<RESULT> callable) {
    super(callable);
    m_futureListener = new FutureDoneListener<>(this);
    m_input = input;
    m_periodic = periodic;
    m_mutexSemaphores = mutexSemaphores;
    m_expirationDate = (input.expirationTimeMillis() != JobInput.INFINITE_EXPIRATION ? System.currentTimeMillis() + input.expirationTimeMillis() : null);

    postConstruct();
  }

  @Override
  @Internal
  public void run() {
    beforeExecuteSafe();

    // delegate control to the 'FutureTask' which in turn calls 'call' on the given Callable.
    try {
      if (m_periodic) {
        super.runAndReset(); // reset the FutureTask's state after execution so it can be executed anew once the period expires.
      }
      else {
        super.run();
      }
    }
    finally {
      afterExecuteSafe();
    }
  }

  @Override
  public boolean isBlocked() {
    return m_blocked;
  }

  @Override
  public void setBlocked(final boolean blocked) {
    m_blocked = blocked;
  }

  @Override
  public boolean isPeriodic() {
    return m_periodic;
  }

  @Override
  public Object getMutexObject() {
    return m_input.mutex();
  }

  @Override
  public boolean isMutexTask() {
    return getMutexObject() != null;
  }

  @Override
  public boolean isMutexOwner() {
    return m_mutexSemaphores.isMutexOwner(this);
  }

  /**
   * @return the {@link JobInput} associated with this Future.
   */
  @Override
  public JobInput getJobInput() {
    return m_input;
  }

  /**
   * @return <code>true</code> if the expiration time of this Future has elapsed and should be discarded by the job
   *         manager without commence execution, <code>false</code> otherwise.
   */
  protected boolean isExpired() {
    return (m_expirationDate == null ? false : System.currentTimeMillis() > m_expirationDate);
  }

  /**
   * Invoke this method if the Future was rejected for execution.
   */
  @Override
  public final void reject() {
    rejected();
  }

  /**
   * Method invoked after construction.
   */
  protected void postConstruct() {
  }

  /**
   * Method invoked if the executor rejected this task from being scheduled. This may occur when no more threads or
   * queue slots are available because their bounds would be exceeded, or upon shutdown of the executor. This method is
   * invoked from the thread that scheduled this task. When being invoked and this task is a mutex task, this task is
   * the mutex owner.
   */
  protected void rejected() {
  }

  /**
   * Method invoked if this task was accepted by the executor immediately before this task is executed. This method is
   * also invoked for <code>cancelled</code> tasks. This method is invoked by the thread that will execute this task.
   * When being invoked and this task is a mutex task, this task is the mutex owner.
   */
  protected void beforeExecute() {
  }

  /**
   * Method invoked after executing this task. If the task was <code>cancelled</code> before running, this method is
   * called immediately after {@link #beforeExecute()}. This method is invoked by the thread that executed this task.
   * When being invoked and this task is a mutex task, this task is still the mutex owner.
   */
  protected void afterExecute() {
  }

  @Override
  public RESULT awaitDoneAndGet() throws ProcessingException {
    try {
      return get();
    }
    catch (final CancellationException e) {
      return null; // Cancellation does not result in an exception.
    }
    catch (final InterruptedException e) {
      throw new JobException(String.format("Interrupted while waiting for the job to complete. [job=%s]", m_input.name()), e);
    }
    catch (final Throwable t) {
      throw BEANS.get(ExceptionTranslator.class).translate(t);
    }
  }

  @Override
  public RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit) throws ProcessingException {
    try {
      return get(timeout, unit);
    }
    catch (final CancellationException e) {
      return null; // Cancellation does not result in an exception.
    }
    catch (final InterruptedException e) {
      throw new JobException(String.format("Interrupted while waiting for the job to complete. [job=%s]", m_input.name()), e);
    }
    catch (final TimeoutException e) {
      throw new JobException(String.format("Failed to wait for the job to complete because it took longer than %sms [job=%s]", unit.toMillis(timeout), m_input.name()), e);
    }
    catch (final Throwable t) {
      throw BEANS.get(ExceptionTranslator.class).translate(t);
    }
  }

  @Override
  public void whenDone(final IDoneCallback<RESULT> callback) {
    m_futureListener.whenDone(callback);
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input);
    builder.attr("periodic", m_periodic);
    builder.attr("blocked", m_blocked);
    builder.attr("expirationDate", m_expirationDate);
    builder.ref("mutexSemaphores", m_mutexSemaphores);
    return builder.toString();
  }

  private void beforeExecuteSafe() {
    try {
      beforeExecute();
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected error in 'beforeExecute'", e);
    }
  }

  private void afterExecuteSafe() {
    try {
      afterExecute();
    }
    catch (final RuntimeException e) {
      LOG.error("Unexpected error in 'afterExecute'", e);
    }
  }
}
