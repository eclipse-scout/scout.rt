/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.annotations.Internal;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableDecorator;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobListenerRegistration;
import org.eclipse.scout.rt.platform.job.IMutex;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;

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

  protected final DonePromise<RESULT> m_donePromise;
  protected final CallableChain<RESULT> m_callableChain;
  protected final List<JobListenerWithFilter> m_listeners = new CopyOnWriteArrayList<>();

  protected volatile JobState m_state;
  protected volatile Set<String> m_executionHints = new HashSet<>();

  public JobFutureTask(final JobManager jobManager, final RunMonitor runMonitor, final JobInput input, final CallableChain<RESULT> callableChain, final Callable<RESULT> callable) {
    super(new Callable<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return callableChain.call(callable); // Run all processors as contained in the chain before invoking the Callable.
      }
    });

    m_jobManager = jobManager;
    m_runMonitor = runMonitor;
    m_input = input;
    m_callableChain = callableChain;

    // Initialize this Future
    m_donePromise = new DonePromise<>(this);
    m_expirationDate = (input.getExpirationTimeMillis() != JobInput.INFINITE_EXPIRATION ? System.currentTimeMillis() + input.getExpirationTimeMillis() : null);
    m_executionHints.addAll(input.getExecutionHints());

    // Contribute to the CallableChain
    m_jobManager.interceptCallableChain(m_callableChain, this, m_runMonitor, m_input);
    m_callableChain.addLast(new ICallableDecorator<RESULT>() {

      @Override
      public IUndecorator<RESULT> decorate() throws Exception {
        changeState(JobState.RUNNING);
        return null;
      }
    });

    // Register this Future
    m_jobManager.registerFuture(this);
    m_runMonitor.registerCancellable(this); // Register to also cancel this Future once the RunMonitor is cancelled (even if the job is not executed yet).
  }

  /**
   * Method invoked once this task completed execution or is cancelled.
   */
  @Override
  protected void done() {
    m_donePromise.fulfill();
    m_runMonitor.unregisterCancellable(this);
    m_jobManager.unregisterFuture(this);
    m_listeners.clear();

    // IMPORTANT: do not pass mutex here because invoked immediately upon cancellation.
  }

  @Override
  public final void reject() {
    changeState(JobState.REJECTED);
    cancel(true); // to enter 'DONE' state and to release a potential waiting submitter.
    releaseMutex();
  }

  /**
   * Method invoked if this task was accepted by the executor immediately before this task is executed. This method is
   * also invoked for <code>cancelled</code> tasks which are not subject for execution. This method is invoked by the
   * thread that will execute this task. When being invoked and this task is a mutex task, this task is the mutex owner.
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
  public JobState getState() {
    return m_state;
  }

  @Override
  public boolean addExecutionHint(final String hint) {
    try {
      return m_executionHints.add(hint);
    }
    finally {
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.JOB_EXECUTION_HINT_ADDED)
          .withFuture(this)
          .withData(hint));
    }
  }

  @Override
  public boolean removeExecutionHint(final String hint) {
    try {
      return m_executionHints.remove(hint);
    }
    finally {
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.JOB_EXECUTION_HINT_REMOVED)
          .withFuture(this)
          .withData(hint));
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
    assertNotSameMutex();

    try {
      m_donePromise.get();
    }
    catch (final ExecutionException | java.util.concurrent.CancellationException e) {
      // NOOP: Do not propagate ExecutionException and CancellationException (see JavaDoc contract)
    }
    catch (final java.lang.InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
  }

  @Override
  public void awaitDone(final long timeout, final TimeUnit unit) {
    assertNotSameMutex();

    try {
      m_donePromise.get(timeout, unit);
    }
    catch (final ExecutionException | java.util.concurrent.CancellationException e) {
      // NOOP: Do not propagate ExecutionException and CancellationException (see JavaDoc contract)
    }
    catch (final java.lang.InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
    catch (final java.util.concurrent.TimeoutException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for a job to complete because the maximal wait time elapsed", timeout, unit));
    }
  }

  @Override
  public RESULT awaitDoneAndGet() {
    return awaitDoneAndGet(DefaultRuntimeExceptionTranslator.class);
  }

  @Override
  public <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    assertNotSameMutex();

    try {
      return m_donePromise.get();
    }
    catch (final ExecutionException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateExecutionException(e, exceptionTranslator));
    }
    catch (final java.util.concurrent.CancellationException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateCancellationException(e, "Failed to wait for a job to complete because the job was cancelled"));
    }
    catch (final java.lang.InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
  }

  @Override
  public RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit) {
    return awaitDoneAndGet(timeout, unit, DefaultRuntimeExceptionTranslator.class);
  }

  @Override
  public <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(final long timeout, final TimeUnit unit, final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    assertNotSameMutex();

    try {
      return m_donePromise.get(timeout, unit);
    }
    catch (final ExecutionException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateExecutionException(e, exceptionTranslator));
    }
    catch (final java.util.concurrent.CancellationException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateCancellationException(e, "Failed to wait for a job to complete because the job was cancelled"));
    }
    catch (final java.lang.InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
    catch (final java.util.concurrent.TimeoutException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for a job to complete because the maximal wait time elapsed", timeout, unit));
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

  protected List<JobListenerWithFilter> getListeners() {
    return m_listeners;
  }

  protected DonePromise<RESULT> getDonePromise() {
    return m_donePromise;
  }

  /**
   * Asserts that the current job (if applicable) does not share the same mutex as the job to be awaited for. Otherwise,
   * that would end up in a deadlock.
   */
  protected void assertNotSameMutex() {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture == null) {
      return; // not running in a job.
    }

    final IMutex currentMutex = currentFuture.getJobInput().getMutex();
    if (currentMutex == null) {
      return; // current job is not running in mutual exclusive manner.
    }

    if (!currentMutex.isMutexOwner(currentFuture)) {
      return; // current job is not mutex owner.
    }

    if (isDone()) {
      return; // job already in 'done'-state.
    }

    Assertions.assertNotSame(currentMutex, getMutex(), "Deadlock detected: Cannot wait for a job that has the same mutex as the current job [mutex={}]", currentMutex);
  }

  /**
   * Releases the mutex if being a mutually exclusive task and currently the mutex owner.
   */
  protected void releaseMutex() {
    if (isMutexOwner()) {
      getMutex().release(this);
    }
  }

  /**
   * Restores the thread's interrupted status because cleared by catching {@link java.lang.InterruptedException}.
   */
  protected void restoreInterruptionStatus() {
    Thread.currentThread().interrupt();
  }

  /**
   * Method invoked to intercept an exception before given to the submitter.
   */
  protected <EXCEPTION extends Throwable> EXCEPTION interceptException(final EXCEPTION exception) {
    if (exception instanceof PlatformException) {
      ((PlatformException) exception).withContextInfo("job", getJobInput().getName());
    }
    return exception;
  }

  /**
   * Sets the new state, and fires {@link JobEvent}, unless being in {@link JobState#DONE} or {@link JobState#REJECTED}.
   */
  protected synchronized void changeState(final JobState state) {
    if (m_state == JobState.DONE || m_state == JobState.REJECTED) {
      return;
    }

    m_state = state;
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.JOB_STATE_CHANGED)
        .withData(state)
        .withFuture(this));
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input);
    builder.attr("state", m_state);
    builder.attr("expirationDate", m_expirationDate);
    return builder.toString();
  }
}
