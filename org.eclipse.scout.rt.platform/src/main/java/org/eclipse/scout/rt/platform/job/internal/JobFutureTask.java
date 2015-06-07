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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.context.internal.InitThreadLocalCallable;
import org.eclipse.scout.rt.platform.exception.ExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IDoneCallback;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobException;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;

/**
 * {@link RunnableFuture} to be given to {@link ExecutorService} for execution. Basically, this class adds the following
 * functionality to Java {@link FutureTask}:
 * <ul>
 * <li>registers/unregisters this task in {@link JobManager};</li>
 * <li>fires a job lifecycle event when transitioning to another state;</li>
 * <li>passes the mutex to the next queued task after job completion;</li>
 * <li>ensures a {@link RunMonitor} to be set on <code>ThreadLocal</code>;</li>
 * <li>combines cancellation of {@link RunMonitor} and {@link Future};</li>
 * <li>ensures this {@link IFuture} to be set on <code>ThreadLocal</code>;</li>
 * <li>adds functionality to await for the Future's completion (either synchronously or asynchronously);</li>
 * </ul>
 *
 * @see FutureTask
 * @since 5.1
 */
@Internal
public class JobFutureTask<RESULT> extends FutureTask<RESULT> implements IFuture<RESULT>, IMutexTask<RESULT>, IRejectable {

  protected final JobManager m_jobManager;

  protected final JobInput m_input;
  protected final Long m_expirationDate;
  private volatile boolean m_blocked;
  private final boolean m_periodic;

  private final boolean m_runWithRunContext;
  private final RunMonitor m_runMonitor;

  private final DonePromise<RESULT> m_donePremise;

  /**
   * Factory method to create a {@link JobFutureTask} for the given {@link Callable}.
   */
  public static <RESULT> JobFutureTask<RESULT> create(final JobManager jobManager, final JobInput input, final boolean periodic, final Callable<RESULT> callable) {
    final AtomicReference<JobFutureTask<RESULT>> holder = new AtomicReference<>();

    // Provide a wrapped Callable to the FutureTask to control its execution.
    final JobFutureTask<RESULT> task = new JobFutureTask<>(jobManager, input, periodic, new Callable<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return holder.get().invoke(callable);
      }
    });
    holder.set(task);
    return task;
  }

  private JobFutureTask(final JobManager jobManager, final JobInput input, final boolean periodic, final Callable<RESULT> callable) {
    super(callable);
    m_jobManager = jobManager;
    m_donePremise = new DonePromise<>(this);
    m_input = input;
    m_periodic = periodic;
    m_expirationDate = (input.expirationTimeMillis() != JobInput.INFINITE_EXPIRATION ? System.currentTimeMillis() + input.expirationTimeMillis() : null);

    m_runWithRunContext = m_input.runContext() != null;
    m_runMonitor = (m_runWithRunContext ? m_input.runContext().runMonitor() : BEANS.get(RunMonitor.class));

    m_jobManager.registerFuture(this);
    m_runMonitor.registerCancellable(this); // Register to also cancel this Future once the RunMonitor is cancelled (even if the job is not executed yet).

    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.SCHEDULED, this));
  }

  /**
   * Method invoked once this task completed execution or is cancelled.
   */
  @Override
  protected void done() {
    m_runMonitor.unregisterCancellable(this);
    m_jobManager.unregisterFuture(this);
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.DONE, this));
    m_donePremise.onDone();

    // IMPORTANT: do not pass mutex here because invoked immediately upon cancellation.
  }

  @Override
  public final void reject() {
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.REJECTED, this));
    cancel(true); // to enter 'DONE' state and to interrupt a potential waiting submitter.
    m_jobManager.passMutexIfMutexOwner(this);
  }

  /**
   * Method invoked if this task was accepted by the executor immediately before this task is executed. This method is
   * also invoked for <code>cancelled</code> tasks which are not subject for execution. This method is invoked by the
   * thread that will execute this task. When being invoked and this task is a mutex task, this task is the mutex owner.
   *
   * @see #invoke(Callable)
   */
  @Override
  public void run() {
    try {
      if (isExpired()) {
        cancel(true); // to enter 'DONE' state and to interrupt a potential waiting submitter.
      }

      if (m_periodic) {
        super.runAndReset(); // periodic action
      }
      else {
        super.run(); // one shot action
      }
    }
    finally {
      m_jobManager.passMutexIfMutexOwner(this);
    }
  }

  /**
   * Method invoked to finally execute the {@link Callable}.
   *
   * @see #run
   */
  protected RESULT invoke(final Callable<RESULT> target) throws Exception {
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.ABOUT_TO_RUN, this));

    // Callable to set the current RunMonitor. That is only required, if no RunContext is provided, because done by RunContext otherwise.
    final Callable<RESULT> c2 = (m_runWithRunContext ? target : new InitThreadLocalCallable<>(target, RunMonitor.CURRENT, m_runMonitor));
    // Callable to set the current Future.
    final Callable<RESULT> c1 = new InitThreadLocalCallable<>(c2, IFuture.CURRENT, this);

    return c1.call();
  }

  @Override
  public boolean isBlocked() {
    return m_blocked;
  }

  /**
   * Marks this task as 'blocked' or 'unblocked'.
   */
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

  /**
   * @return <code>true</code> if this task is a mutex task and currently owns the mutex.
   */
  public boolean isMutexOwner() {
    return m_jobManager.isMutexOwner(this);
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

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    final Set<Boolean> status = CollectionUtility.hashSet(!isDone()); // Check for 'done' or 'cancel' to comply with Future.isCancelled semantic.

    // 1. Unregister this 'Cancellable' from RunMonitor (to prevent multiple cancel invocations).
    m_runMonitor.unregisterCancellable(this);

    // 2. Cancel RunMonitor if not done yet.
    //    Note: The Future should only be cancelled after the RunMonitor is cancelled, so that waiting threads (via Future.awaitDone) have a proper RunMonitor state.
    if (!m_runMonitor.isCancelled()) {
      status.add(m_runMonitor.cancel(interruptIfRunning));
    }

    // 3. Cancel FutureTask if not done yet.
    if (!JobFutureTask.this.isCancelled()) {
      status.add(JobFutureTask.super.cancel(interruptIfRunning));
    }

    return Collections.singleton(Boolean.TRUE).equals(status);
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
    m_donePremise.whenDone(callback);
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input);
    builder.attr("periodic", m_periodic);
    builder.attr("blocked", m_blocked);
    builder.attr("expirationDate", m_expirationDate);
    return builder.toString();
  }
}
