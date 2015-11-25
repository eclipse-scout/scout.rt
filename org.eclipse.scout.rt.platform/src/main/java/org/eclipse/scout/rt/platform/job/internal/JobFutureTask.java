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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.CollectionUtility;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.chain.InvocationChain;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.IThrowableTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingExceptionTranslator;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
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
public class JobFutureTask<RESULT> extends FutureTask<RESULT> implements IFuture<RESULT>, IRejectableRunnable {

  protected final JobManager m_jobManager;
  protected final RunMonitor m_runMonitor;
  protected final JobInput m_input;
  protected final Long m_expirationDate;

  protected final DonePromise<RESULT> m_donePromise = new DonePromise<>(this);
  protected final List<JobListenerWithFilter> m_listeners = new CopyOnWriteArrayList<>();

  protected volatile boolean m_blocked;

  protected Set<String> m_executionHints = new HashSet<>();

  public JobFutureTask(final JobManager jobManager, final RunMonitor runMonitor, final JobInput input, final InvocationChain<RESULT> invocationChain, final Callable<RESULT> callable) {
    super(new Callable<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return invocationChain.invoke(callable); // Run all processors as contained in the chain before invoking the Callable.
      }
    });

    m_jobManager = jobManager;
    m_runMonitor = runMonitor;
    m_input = input;

    m_expirationDate = (input.getExpirationTimeMillis() != JobInput.INFINITE_EXPIRATION ? System.currentTimeMillis() + input.getExpirationTimeMillis() : null);

    m_jobManager.registerFuture(this);
    m_runMonitor.registerCancellable(this); // Register to also cancel this Future once the RunMonitor is cancelled (even if the job is not executed yet).

    m_executionHints.addAll(input.getExecutionHints());

    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.SCHEDULED).withFuture(this));
  }

  /**
   * Method invoked once this task completed execution or is cancelled.
   */
  @Override
  protected void done() {
    // IMPORTANT: event must be sent before any waiting thread is released.
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.DONE).withFuture(this));

    m_runMonitor.unregisterCancellable(this);
    m_jobManager.unregisterFuture(this);
    m_listeners.clear();
    m_donePromise.fulfill();

    // IMPORTANT: do not pass mutex here because invoked immediately upon cancellation.
  }

  @Override
  public final void reject() {
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.REJECTED).withFuture(this));
    cancel(true); // to enter 'DONE' state and to release a potential waiting submitter.
    releaseMutex();
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

      if (m_input.getSchedulingRule() == JobInput.SCHEDULING_RULE_SINGLE_EXECUTION) {
        super.run();
      }
      else {
        super.runAndReset();
      }
    }
    finally {
      releaseMutex();
    }
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
  public int getSchedulingRule() {
    return m_input.getSchedulingRule();
  }

  /**
   * @return the mutex object, or <code>null</code> if not being a mutual exclusive task.
   */
  public IMutex getMutex() {
    return m_input.getMutex();
  }

  /**
   * @return <code>true</code> if this task is a mutual exclusive task and currently owns the mutex.
   */
  public boolean isMutexOwner() {
    final IMutex mutex = getMutex();
    return mutex != null && mutex.isMutexOwner(this);
  }

  @Override
  public JobInput getJobInput() {
    return m_input;
  }

  @Override
  public boolean addExecutionHint(final String hint) {
    try {
      return m_executionHints.add(hint);
    }
    finally {
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.EXECUTION_HINT_CHANGED).withFuture(this));
    }
  }

  @Override
  public boolean removeExecutionHint(final String hint) {
    try {
      return m_executionHints.remove(hint);
    }
    finally {
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.EXECUTION_HINT_CHANGED).withFuture(this));
    }
  }

  @Override
  public boolean containsExecutionHint(final String hint) {
    return m_executionHints.contains(hint);
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
  public void awaitDone() {
    try {
      awaitDoneAndGet();
    }
    catch (final ProcessingException e) {
      if (e.isInterruption()) {
        throw e;
      }
      else {
        // NOOP: Do not propagate exception (see JavaDoc contract)
      }
    }
  }

  @Override
  public boolean awaitDone(final long timeout, final TimeUnit unit) {
    try {
      awaitDoneAndGet(timeout, unit);
    }
    catch (final ProcessingException e) {
      if (e.isInterruption()) {
        throw e;
      }
      else if (e.isTimeout()) {
        return false;
      }
      else {
        // NOOP: Do not propagate exception (see JavaDoc contract)
      }
    }
    return true;
  }

  @Override
  public RESULT awaitDoneAndGet() {
    return awaitDoneAndGet(BEANS.get(ProcessingExceptionTranslator.class));
  }

  @Override
  public <ERROR extends Throwable> RESULT awaitDoneAndGet(final IThrowableTranslator<ERROR> throwableTranslator) throws ERROR {
    assertNotSameMutex();

    try {
      return m_donePromise.get();
    }
    catch (final CancellationException e) {
      return throwElseReturnNull(new CancellationException(String.format("The job was cancelled. [job=%s]", m_input.getName())), throwableTranslator);
    }
    catch (final InterruptedException e) {
      return throwElseReturnNull(new InterruptedException(String.format("Interrupted while waiting for the job to complete. [job=%s]", m_input.getName())), throwableTranslator);
    }
    catch (final Throwable t) {
      return throwElseReturnNull(t, throwableTranslator);
    }
  }

  @Override
  public RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit) {
    return awaitDoneAndGet(timeout, unit, BEANS.get(ProcessingExceptionTranslator.class));
  }

  @Override
  public <ERROR extends Throwable> RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit, final IThrowableTranslator<ERROR> throwableTranslator) throws ERROR {
    assertNotSameMutex();

    try {
      return m_donePromise.get(timeout, unit);
    }
    catch (final CancellationException e) {
      return throwElseReturnNull(new CancellationException(String.format("The job was cancelled. [job=%s]", m_input.getName())), throwableTranslator);
    }
    catch (final InterruptedException e) {
      return throwElseReturnNull(new InterruptedException(String.format("Interrupted while waiting for the job to complete. [job=%s]", m_input.getName())), throwableTranslator);
    }
    catch (final TimeoutException e) {
      return throwElseReturnNull(new TimeoutException(String.format("Failed to wait for the job to complete because it took longer than %sms [job=%s]", unit.toMillis(timeout), m_input.getName())), throwableTranslator);
    }
    catch (final Throwable t) {
      return throwElseReturnNull(t, throwableTranslator);
    }
  }

  @Override
  public IFuture<RESULT> whenDone(final IDoneHandler<RESULT> callback, final RunContext runContext) {
    m_donePromise.whenDone(callback, runContext);
    return this;
  }

  @Override
  public IJobListenerRegistration addListener(final IJobListener listener) {
    return addListener(null, listener);
  }

  @Override
  public IJobListenerRegistration addListener(final IFilter<JobEvent> filter, final IJobListener listener) {
    final JobListenerWithFilter localListener = new JobListenerWithFilter(listener, filter);
    m_listeners.add(localListener);

    return new IJobListenerRegistration() {

      @Override
      public void dispose() {
        m_listeners.remove(localListener);
      }
    };
  }

  List<JobListenerWithFilter> getListeners() {
    return m_listeners;
  }

  private <ERROR extends Throwable> RESULT throwElseReturnNull(final Throwable t, final IThrowableTranslator<ERROR> throwableTranslator) throws ERROR {
    final ERROR error = throwableTranslator.translate(t);
    if (error != null) {
      throw error;
    }
    else {
      return null;
    }
  }

  /**
   * Asserts that the current job (if applicable) does not share the same mutex as the job to be awaited for. Otherwise,
   * that would end up in a deadlock.
   */
  private void assertNotSameMutex() {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture == null) {
      return; // not running in a job.
    }

    final IMutex currentMutex = currentFuture.getJobInput().getMutex();
    if (currentMutex == null) {
      return; // current job is not running in mutual exclusive manner.
    }

    if (isDone()) {
      return; // job already in 'done'-state.
    }

    Assertions.assertNotSame(currentMutex, getMutex(), "Deadlock detected: Cannot wait for a job that has the same mutex as the current job [mutex=%s]", currentMutex);
  }

  /**
   * Releases the mutex if being a mutual exclusive task and currently the mutex owner.
   */
  protected void releaseMutex() {
    if (isMutexOwner()) {
      getMutex().release(this);
    }
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input);
    builder.attr("blocked", m_blocked);
    builder.attr("expirationDate", m_expirationDate);
    return builder.toString();
  }
}
