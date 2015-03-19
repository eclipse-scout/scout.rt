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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IProgressMonitor;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.ProgressMonitorProvider;
import org.eclipse.scout.rt.platform.job.internal.MutexSemaphores;
import org.eclipse.scout.rt.platform.job.internal.callable.ExceptionTranslator;

/**
 * Future to be given to the executor for execution. This class combines both, the Executable and its Future. This
 * FutureTask works on behalf of the origin {@link ScheduledFutureTask} created by the
 * {@link ScheduledThreadPoolExecutor}, which is set during scheduling. That 'lazy initialization' is due to the fact
 * that unlike {@link FutureTask}, a {@link ScheduledFutureTask} cannot be instantiated because of visibility
 * restrictions.
 *
 * @see ScheduledThreadPoolExecutor#decorateTask
 * @since 5.1
 */
@Internal
public class JobFutureTask<RESULT> extends ScheduledFutureDelegate<RESULT> implements IFutureTask<RESULT>, IFuture<RESULT> {

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobFutureTask.class);

  protected final JobInput m_input;
  private final Job<RESULT> m_job;
  protected final Long m_expirationDate;
  private volatile boolean m_blocked;
  private final IProgressMonitor m_progressMonitor;
  private final boolean m_periodic;

  private final MutexSemaphores m_mutexSemaphores;

  public JobFutureTask(final JobInput input, final boolean periodic, final MutexSemaphores mutexSemaphores, final Callable<RESULT> callable) {
    m_input = input;
    m_periodic = periodic;
    m_mutexSemaphores = mutexSemaphores;
    m_job = new Job<>(this, callable);
    m_progressMonitor = OBJ.get(ProgressMonitorProvider.class).provide(this);

    final long expirationTime = input.getExpirationTimeMillis();
    m_expirationDate = (expirationTime != JobInput.INFINITE_EXPIRATION ? System.currentTimeMillis() + expirationTime : null);

    postConstruct();
  }

  @Override
  @Internal
  public void run() {
    beforeExecuteSafe();
    try {
      super.run(); // delegate control to the 'ScheduledFutureTask' which in turn calls 'call' on the job.
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
    return m_input.getMutex();
  }

  @Override
  public boolean isMutexTask() {
    return getMutexObject() != null;
  }

  @Override
  public boolean isMutexOwner() {
    return m_mutexSemaphores.isMutexOwner(this);
  }

  @Override
  public Job<RESULT> getJob() {
    return m_job;
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
  public IProgressMonitor getProgressMonitor() {
    return m_progressMonitor;
  }

  @Override
  public RESULT awaitDoneAndGet() throws ProcessingException {
    try {
      return get();
    }
    catch (final ExecutionException e) {
      throw ExceptionTranslator.translate(e.getCause());
    }
    catch (final CancellationException e) {
      return null; // Cancellation should not result in an exception.
    }
    catch (final InterruptedException e) {
      throw ExceptionTranslator.translateInterruptedException(e, m_input.getIdentifier());
    }
    catch (final RuntimeException e) {
      throw ExceptionTranslator.translate(e);
    }
  }

  @Override
  public RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit) throws ProcessingException {
    try {
      return get(timeout, unit);
    }
    catch (final ExecutionException e) {
      throw ExceptionTranslator.translate(e.getCause());
    }
    catch (final CancellationException e) {
      return null; // Cancellation should not result in an exception.
    }
    catch (final InterruptedException e) {
      throw ExceptionTranslator.translateInterruptedException(e, m_input.getIdentifier());
    }
    catch (final TimeoutException e) {
      throw ExceptionTranslator.translateTimeoutException(e, timeout, unit, m_input.getIdentifier());
    }
    catch (final RuntimeException e) {
      throw ExceptionTranslator.translate(e);
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input);
    builder.attr("periodic", m_periodic);
    builder.attr("blocked", m_blocked);
    builder.attr("expirationDate", m_expirationDate);
    builder.ref("progressMonitor", m_progressMonitor);
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
