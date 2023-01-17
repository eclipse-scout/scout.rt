/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.platform.job.internal;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain;
import org.eclipse.scout.rt.platform.chain.callable.CallableChain.Chain;
import org.eclipse.scout.rt.platform.chain.callable.ICallableInterceptor;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IThrowableWithContextInfo;
import org.eclipse.scout.rt.platform.exception.PlatformException;
import org.eclipse.scout.rt.platform.job.DoneEvent;
import org.eclipse.scout.rt.platform.job.ExecutionTrigger;
import org.eclipse.scout.rt.platform.job.IDoneHandler;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.job.listener.JobEventData;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.ToStringBuilder;
import org.quartz.Calendar;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.spi.OperableTrigger;

/**
 * Represents a {@link RunnableFuture} to be given to {@link ExecutorService} for execution.
 *
 * @see FutureTask
 * @since 5.1
 */
public class JobFutureTask<RESULT> extends FutureTask<RESULT> implements IFuture<RESULT>, IRejectableRunnable {

  protected final JobManager m_jobManager;
  protected final RunMonitor m_runMonitor;
  protected final JobInput m_input;
  protected final Long m_expirationDate;
  protected final ExecutionSemaphore m_executionSemaphore;

  protected final CallableChain<RESULT> m_callableChain;
  protected final List<JobListenerWithFilter> m_listeners = new CopyOnWriteArrayList<>();

  protected volatile JobState m_state = JobState.NEW;

  protected final CompletionPromise<RESULT> m_completionPromise;
  protected final AtomicBoolean m_finished = new AtomicBoolean(false);

  protected final Set<String> m_executionHints = new HashSet<>();

  protected final Date m_firstFireTime;
  protected final boolean m_singleExecution;
  protected final boolean m_delayedExecution;

  protected final OperableTrigger m_trigger;
  protected final Calendar m_calendar;

  /**
   * The thread currently running the task
   * <p>
   * This thread is leased and released by the calling {@link JobManager}
   * <p>
   * The contract is that every access to the {@link Thread} is synchronized.
   *
   * @since 10.0
   */
  protected volatile Thread m_runner;
  protected final Object m_runnerLock = new Object();

  public JobFutureTask(final JobManager jobManager, final RunMonitor runMonitor, final JobInput input, final CallableChain<RESULT> callableChain, final Callable<RESULT> callable) {
    super(() -> callableChain.call(callable) /* run all processors as contained in the chain before invoking the callable */ );

    m_jobManager = jobManager;
    m_runMonitor = runMonitor;
    m_input = input;
    m_executionSemaphore = ExecutionSemaphore.get(input);

    m_callableChain = callableChain;

    // Initialize this Future
    m_completionPromise = new CompletionPromise<>(this, jobManager.getExecutor());
    m_expirationDate = (input.getExpirationTimeMillis() != JobInput.EXPIRE_NEVER ? System.currentTimeMillis() + input.getExpirationTimeMillis() : null);
    m_executionHints.addAll(input.getExecutionHints());

    // Contribute to the CallableChain
    m_jobManager.interceptCallableChain(m_callableChain, this, m_runMonitor, m_input);
    m_callableChain.addLast(new ICallableInterceptor<RESULT>() {

      @Override
      public RESULT intercept(Chain<RESULT> chain) {
        // do not run task if run monitor is cancelled, return null as FutureTask must have been cancelled as well
        // (JobFutureTask is registered as a child of the run monitor), IFuture.awaitDoneAndGet will throw a FutureCancelledError
        JobFutureTask.this.cancel(false);
        return null;
      }

      @Override
      public boolean isEnabled() {
        // intercept chain if run monitor is cancelled
        return m_runMonitor.isCancelled();
      }
    });
    m_callableChain.addLast(() -> {
      changeState(JobState.RUNNING);
      return null;
    });

    // Compute execution data.
    m_trigger = createQuartzTrigger(input);
    m_calendar = (input.getExecutionTrigger() == null ? null : input.getExecutionTrigger().getCalendar());
    m_firstFireTime = computeFirstFireTime(m_trigger);
    m_singleExecution = computeSingleExecuting(m_trigger, m_firstFireTime);
    m_delayedExecution = (input.getExecutionTrigger() != null && computeDelayedExecuting(m_firstFireTime, m_input.getExecutionTrigger().getNow()));

    // register this instance with the JobManager before it is registered with the RunMonitor, so that already cancelled RunMonitors are handled correctly.
    m_jobManager.registerFuture(this);

    // Register to also cancel this Future once the RunMonitor is cancelled (even if the job is not executed yet).
    m_runMonitor.registerCancellable(this);
  }

  /**
   * Method invoked after this task was accepted by {@link ExecutorService} and assigned to a worker thread to commence
   * execution, and is invoked for every scheduled task, even if being <code>cancelled</code>. However, 'super.run()' or
   * 'super.runAndReset()' will prevent cancelled tasks from running.
   * <p>
   * This method is invoked in the worker thread which will finally run the task.
   * <p>
   * When invoked and this task is assigned to an {@link IExecutionSemaphore}, this task owns a permit.
   */
  @Override
  public void run() {
    m_trigger.triggered(m_calendar);
    m_runner = Thread.currentThread();
    try {
      if (isExpired()) {
        cancel(true); // to enter done state and to interrupt a potential waiting submitter.
      }
      else if (isFinalRun()) {
        super.run(); // will enter done state upon completion.
      }
      else {
        super.runAndReset(); // will not enter done state upon completion.
      }
    }
    finally {
      synchronized (m_runnerLock) {
        m_runner = null;
      }
      finishInternal();
      releasePermit();
    }
  }

  /**
   * Method invoked once this task completed execution or is cancelled (regardless whether still executing), and is
   * invoked only once.
   */
  @Override
  protected void done() {
    changeState(JobState.DONE);
    m_listeners.clear();

    m_runMonitor.unregisterCancellable(this);
    m_completionPromise.done();
    finishInternal();

    // IMPORTANT: do not release permit here because also invoked upon cancellation.
  }

  /**
   * Method invoked once this task gets cancelled, and is invoked only once.
   */
  protected void cancelled(final boolean interruptIfRunning) {
    m_runMonitor.cancel(interruptIfRunning);

    // Interrupt a possible runner, but only if not running on behalf of a RunContext. Otherwise, interruption was already done by RunContext.
    if (interruptIfRunning && m_input.getRunContext() == null) {
      synchronized (m_runnerLock) {
        final Thread runner = m_runner;
        if (runner != null) {
          runner.interrupt();
        }
      }
    }
  }

  /**
   * Method invoked once this task finished execution, or upon a premature cancellation, meaning that the job did not
   * commence execution yet, and will never do so.
   */
  protected void finished() {
    m_jobManager.unregisterFuture(this);
    m_completionPromise.finish();

    // IMPORTANT: do not release permit here.
  }

  @Override
  public final void reject() {
    changeState(JobState.REJECTED);
    cancel(true); // to enter done state and to release a potential waiting submitter.
    releasePermit();
  }

  @Override
  public boolean isSingleExecution() {
    return m_singleExecution;
  }

  /**
   * Returns whether this task may be executed some time in the future, unless it gets cancelled.
   */
  protected boolean isDelayedExecution() {
    return m_delayedExecution;
  }

  /**
   * Returns whether this task may be executed some time in the future, unless it gets cancelled.
   */
  protected boolean hasNextExecution() {
    return m_trigger.mayFireAgain();
  }

  /**
   * Returns the time the job's trigger will fire for the first time.
   */
  public Date getFirstFireTime() {
    return m_firstFireTime;
  }

  /**
   * Returns the trigger which fires for job execution.
   */
  protected OperableTrigger getTrigger() {
    return m_trigger;
  }

  /**
   * Returns the calendar to be applied to this trigger's schedule.
   */
  protected Calendar getCalendar() {
    return m_calendar;
  }

  @Override
  public ExecutionSemaphore getExecutionSemaphore() {
    return m_executionSemaphore;
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
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.JOB_EXECUTION_HINT_ADDED, new JobEventData()
          .withFuture(this)
          .withExecutionHint(hint)));
    }
  }

  @Override
  public boolean removeExecutionHint(final String hint) {
    try {
      return m_executionHints.remove(hint);
    }
    finally {
      m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.JOB_EXECUTION_HINT_REMOVED, new JobEventData()
          .withFuture(this)
          .withExecutionHint(hint)));
    }
  }

  @Override
  public boolean containsExecutionHint(final String hint) {
    return m_executionHints.contains(hint);
  }

  /**
   * Returns <code>true</code> if expired and this job should not commence execution, or else <code>false</code>.
   */
  protected boolean isExpired() {
    return m_expirationDate != null && System.currentTimeMillis() > m_expirationDate;
  }

  @Override
  public boolean isFinished() {
    return m_completionPromise.isFinished();
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    if (super.cancel(false)) { // Interrupt running thread later (if applicable), so that it may query 'RunMonitor.CURRENT.get().isCancelled()' upon interruption.
      cancelled(interruptIfRunning);
      return true;
    }

    return false;
  }

  @Override
  public void awaitDone() {
    assertNotSameSemaphore();

    try {
      m_completionPromise.awaitDoneAndGet();
    }
    catch (final ExecutionException | CancellationException e) { // NOSONAR
      // NOOP: Do not propagate ExecutionException and CancellationException (see JavaDoc contract)
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
  }

  @Override
  public void awaitDone(final long timeout, final TimeUnit unit) {
    assertNotSameSemaphore();

    try {
      m_completionPromise.awaitDoneAndGet(timeout, unit);
    }
    catch (final ExecutionException | CancellationException e) { // NOSONAR
      // NOOP: Do not propagate ExecutionException and CancellationException (see JavaDoc contract)
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
    catch (final TimeoutException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for a job to complete because the maximal wait time elapsed", timeout, unit));
    }
  }

  @Override
  public void awaitFinished(final long timeout, final TimeUnit unit) {
    assertNotSameSemaphore();

    try {
      m_completionPromise.awaitFinished(timeout, unit);
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to finish"));
    }
    catch (final TimeoutException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for a job to finish because the maximal wait time elapsed", timeout, unit));
    }
  }

  @Override
  public RESULT awaitDoneAndGet() {
    return awaitDoneAndGet(DefaultRuntimeExceptionTranslator.class);
  }

  @Override
  public <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(final Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION {
    assertNotSameSemaphore();

    try {
      return m_completionPromise.awaitDoneAndGet();
    }
    catch (final ExecutionException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateExecutionException(e, exceptionTranslator));
    }
    catch (final CancellationException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateCancellationException(e, "Failed to wait for a job to complete because the job was cancelled"));
    }
    catch (final InterruptedException e) {
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
    assertNotSameSemaphore();

    try {
      return m_completionPromise.awaitDoneAndGet(timeout, unit);
    }
    catch (final ExecutionException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateExecutionException(e, exceptionTranslator));
    }
    catch (final CancellationException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateCancellationException(e, "Failed to wait for a job to complete because the job was cancelled"));
    }
    catch (final InterruptedException e) {
      restoreInterruptionStatus();
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateInterruptedException(e, "Interrupted while waiting for a job to complete"));
    }
    catch (final TimeoutException e) {
      throw interceptException(BEANS.get(JobExceptionTranslator.class).translateTimeoutException(e, "Failed to wait for a job to complete because the maximal wait time elapsed", timeout, unit));
    }
  }

  @Override
  public IFuture<RESULT> whenDone(final IDoneHandler<RESULT> callback, final RunContext runContext) {
    m_completionPromise.whenDone(callback, runContext);
    return this;
  }

  @Override
  public <FUNCTION_RESULT> IFuture<FUNCTION_RESULT> whenDoneSchedule(final BiFunction<RESULT, Throwable, FUNCTION_RESULT> function, final JobInput input) {
    Assertions.assertNotNull(input, "Input must not be null");
    Assertions.assertNotNull(function, "Function must not be null");

    final AtomicReference<DoneEvent<RESULT>> doneEvent = new AtomicReference<>();

    // Create the future to be executed after entering done state.
    final JobFutureTask<FUNCTION_RESULT> functionFuture = m_jobManager.createJobFutureTask(() -> function.apply(doneEvent.get().getResult(), doneEvent.get().getException()), input);

    // Submit the function's future upon entering done state.
    whenDone(event -> {
      doneEvent.set(event);
      // Propagate cancellation if applicable.
      // Nevertheless, even if not executed, the future must be submitted to enter done state.
      if (event.isCancelled()) {
        functionFuture.cancel(false);
      }
      m_jobManager.submit(functionFuture);
    }, null);

    return functionFuture;
  }

  @Override
  public IFuture<Void> whenDoneSchedule(final BiConsumer<RESULT, Throwable> function, final JobInput input) {
    return whenDoneSchedule((result, throwable) -> {
      function.accept(result, throwable);
      return null;
    }, input);
  }

  @Override
  public IRegistrationHandle addListener(final IJobListener listener) {
    return addListener(null, listener);
  }

  @Override
  public IRegistrationHandle addListener(final Predicate<JobEvent> filter, final IJobListener listener) {
    final JobListenerWithFilter localListener = new JobListenerWithFilter(listener, filter);
    m_listeners.add(localListener);

    return () -> m_listeners.remove(localListener);
  }

  protected List<JobListenerWithFilter> getListeners() {
    return m_listeners;
  }

  protected CompletionPromise<RESULT> getCompletionPromise() {
    return m_completionPromise;
  }

  /**
   * Asserts that the current job (if present) is not assigned to the same {@link IExecutionSemaphore} as the job to be
   * awaited for. Otherwise, that could end up in a deadlock.
   */
  protected void assertNotSameSemaphore() {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    if (currentFuture == null) {
      return; // not running in a job.
    }

    final IExecutionSemaphore currentSemaphore = currentFuture.getJobInput().getExecutionSemaphore();
    if (currentSemaphore == null) {
      return; // current job has no maximal concurrency restriction.
    }

    if (!currentSemaphore.isPermitOwner(currentFuture)) {
      return; // current job is not permit owner.
    }

    if (isDone()) {
      return; // job already in done state.
    }

    Assertions.assertNotSame(currentSemaphore, m_executionSemaphore, "Potential deadlock detected: Cannot wait for a job which is assigned to the same semaphore as the current job [semaphore={}]", currentSemaphore);
  }

  /**
   * Releases this job's permit if assigned to an {@link IExecutionSemaphore}, but only if this job currently owns a
   * permit.
   */
  protected void releasePermit() {
    if (m_executionSemaphore != null && m_executionSemaphore.isPermitOwner(this)) {
      m_executionSemaphore.release(this);
    }
  }

  /**
   * Restores the thread's interrupted status because cleared by catching {@link InterruptedException}.
   */
  protected void restoreInterruptionStatus() {
    Thread.currentThread().interrupt();
  }

  /**
   * Method invoked to intercept an exception before given to the submitter.
   */
  protected <EXCEPTION extends Throwable> EXCEPTION interceptException(final EXCEPTION exception) {
    if (exception instanceof IThrowableWithContextInfo) {
      ((IThrowableWithContextInfo) exception).withContextInfo("job", getJobInput().getName());
    }
    return exception;
  }

  /**
   * Sets the new state, and fires {@link JobEvent}, unless already being in state {@link JobState#DONE} or
   * {@link JobState#REJECTED}, or the specified state is already the current state.
   */
  protected void changeState(final JobState state) {
    changeState(new JobEventData()
        .withState(state)
        .withFuture(this));
  }

  /**
   * Sets the new state, and fires the given event, unless already being in state {@link JobState#DONE} or
   * {@link JobState#REJECTED}, or the specified state is already the current state.
   * <p>
   * The caller is responsible for setting {@link JobEventData#getState()} and {@link JobEventData#getFuture()}
   * accordingly.
   */
  protected synchronized void changeState(final JobEventData eventData) {
    Assertions.assertNotNull(eventData.getState(), "missing state");
    Assertions.assertSame(this, eventData.getFuture(), "wrong future [expected={}]", this);

    // Do nothing if equals to current state.
    if (m_state == eventData.getState()) {
      return;
    }

    // Do nothing if already in done or rejected state.
    if (m_state == JobState.DONE || m_state == JobState.REJECTED) {
      return;
    }

    m_state = eventData.getState();
    m_jobManager.fireEvent(new JobEvent(m_jobManager, JobEventType.JOB_STATE_CHANGED, eventData));
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("job", m_input.getName());
    builder.attr("state", m_state);
    return builder.toString();
  }

  /**
   * Returns if this is the very final run of this job.
   */
  protected boolean isFinalRun() {
    if (isSingleExecution()) {
      return true; // 'one-shot' job
    }
    else if (!hasNextExecution()) {
      return true; // job which will not commence execution once more
    }

    return false;
  }

  /**
   * Invokes {@link #finished()} if done and currently not executing.
   */
  protected void finishInternal() {
    // Ensure task not currently running.
    if (m_runner != null) {
      return;
    }

    // Ensure task to be in done state, because method also called for periodic tasks which did not complete yet.
    if (!isDone()) {
      return;
    }

    // Ensure to be called only once (thread safety).
    if (!m_finished.compareAndSet(false, true)) {
      return;
    }

    finished();
  }

  /**
   * Computes the time the specified trigger will fire for the first time, and is never <code>null</code>.
   */
  protected Date computeFirstFireTime(final OperableTrigger trigger) {
    try {
      trigger.validate();
    }
    catch (final SchedulerException e) {
      throw new PlatformException("Trigger not valid [trigger={}, job={}]", trigger, this, e);
    }

    // Compute 'first fire time' with respect to an optionally configured calendar.
    final Date firstFireDate = trigger.computeFirstFireTime(m_calendar);
    return Assertions.assertNotNull(firstFireDate, "Trigger not valid, because it will never fire. Check trigger's schedule. [schedule={}, future={}]", trigger.getScheduleBuilder(), this);
  }

  /**
   * Computes whether the specified trigger is single executing, meaning executed just once.
   */
  protected boolean computeSingleExecuting(final Trigger trigger, final Date firstFireTime) {
    if (trigger.getFinalFireTime() == null) {
      return false;
    }
    else {
      return trigger.getFinalFireTime().equals(firstFireTime);
    }
  }

  /**
   * Computes whether this is about a delayed execution.
   */
  protected boolean computeDelayedExecuting(final Date firstFireTime, final Date now) {
    return firstFireTime.after(now);
  }

  /**
   * Creates the Quartz Trigger to fire execution.
   */
  protected OperableTrigger createQuartzTrigger(final JobInput input) {
    final TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger()
        .forJob(JobFutureTask.class.getSimpleName());

    final ExecutionTrigger executionTrigger = input.getExecutionTrigger();
    if (executionTrigger != null) {
      builder
          .startAt(executionTrigger.getStartTime())
          .endAt(executionTrigger.getEndTime())
          .withSchedule(executionTrigger.getSchedule());
    }
    return (OperableTrigger) builder.build();
  }
}
