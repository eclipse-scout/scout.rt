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
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.filter.NotFilter;
import org.eclipse.scout.rt.platform.filter.OrFilter;
import org.eclipse.scout.rt.platform.job.filter.event.JobEventFilterBuilder;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.IRegistrationHandle;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.IBiConsumer;
import org.eclipse.scout.rt.platform.util.concurrent.IBiFunction;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;

/**
 * Represents a {@link Future} to interact with the associated job, or to wait for the job to complete and to query it's
 * computation result.
 *
 * @see Future
 * @since 5.1
 */
public interface IFuture<RESULT> extends ICancellable {

  /**
   * The {@link IFuture} which is currently associated with the current thread.
   */
  ThreadLocal<IFuture<?>> CURRENT = new ThreadLocal<>();

  /**
   * Returns {@link JobInput} the job was instrumented with.
   */
  JobInput getJobInput();

  /**
   * Returns the {@link IExecutionSemaphore} which controls this job's execution, or <code>null</code> if there is no
   * concurrency restriction for this job.
   * <p>
   * With a semaphore in place, this job only commences execution, once a permit is free or gets available. If free, the
   * job commences execution immediately at the next reasonable opportunity, unless no worker thread is available.
   */
  IExecutionSemaphore getExecutionSemaphore();

  /**
   * Returns whether this job is a 'one-shot' execution, meaning just a single execution at a particular moment in time.
   *
   * @return <code>true</code> if single executing, or else <code>false</code> if it repeats one time at minimum.
   */
  boolean isSingleExecution();

  /**
   * Attempts to cancel the execution of the associated job. This attempt will be ignored if the job has already
   * completed or was cancelled. If not running yet, the job will never run. If the job has already started, then the
   * <code>interruptIfRunning</code> parameter determines whether the thread executing this job should be interrupted in
   * an attempt to stop the job.
   *
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this job should be interrupted; otherwise, in-progress jobs are
   *          allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally.
   */
  @Override
  boolean cancel(boolean interruptIfRunning);

  /**
   * Returns <code>true</code> if this job was cancelled before it completed normally.
   */
  @Override
  boolean isCancelled();

  /**
   * Returns <code>true</code> if this job completed either normally, by an exception, or was canceled.
   */
  boolean isDone();

  /**
   * Returns <code>true</code> if this job completed either normally, or by an exception, or if it will never commence
   * execution due to a premature cancellation. However, if the job is cancelled while running, this method will only
   * return <code>true</code> upon completion.
   */
  boolean isFinished();

  /**
   * Returns this future's current state; is never <code>null</code>.
   */
  JobState getState();

  /**
   * Waits if necessary for the job to complete, or until cancelled. This method does not throw an exception if
   * cancelled or the computation failed.
   *
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   */
  void awaitDone();

  /**
   * Waits if necessary for at most the given time for the job to complete, or until cancelled, or the timeout elapses.
   * This method does not throw an exception if cancelled, or the computation failed.
   *
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   * @throws TimedOutError
   *           if the wait timed out.
   */
  void awaitDone(long timeout, TimeUnit unit);

  /**
   * Waits if necessary for at most the given time for the job to finish, meaning that the job either completes normally
   * or by an exception, or that it will never commence execution due to a premature cancellation.
   *
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   * @throws TimedOutError
   *           if the wait timed out.
   */
  void awaitFinished(long timeout, TimeUnit unit);

  /**
   * Waits if necessary for the job to complete, and then returns its result, if available, or throws its exception
   * according to {@link DefaultRuntimeExceptionTranslator}, or throws {@link FutureCancelledError} if cancelled, or
   * throws {@link ThreadInterruptedError} if the current thread was interrupted while waiting.
   *
   * @return the job's result.
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *           if the job was cancelled.
   * @throws RuntimeException
   *           if the job throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  RESULT awaitDoneAndGet();

  /**
   * Waits if necessary for the job to complete, and then returns its result, if available, or throws its exception
   * according to {@link IExceptionTranslator}, or throws {@link FutureCancelledError} if cancelled, or throws
   * {@link ThreadInterruptedError} if the current thread was interrupted while waiting.
   * <p>
   * Use a specific {@link IExceptionTranslator} to control exception translation.
   *
   * @param exceptionTranslator
   *          to translate the job's exception if the job threw an exception.
   * @return the job's result.
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *           if the job was cancelled.
   * @throws EXCEPTION
   *           if the job throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION;

  /**
   * Waits if necessary for at most the given time for the job to complete, and then returns its result, if available,
   * or throws its exception according to {@link DefaultRuntimeExceptionTranslator}, or throws
   * {@link FutureCancelledError} if cancelled, or throws {@link TimedOutError} if waiting timeout elapsed, or
   * throws {@link ThreadInterruptedError} if the current thread was interrupted while waiting.
   *
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @return the job's result.
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *           if the job was cancelled.
   * @throws TimedOutError
   *           if the wait timed out.
   * @throws RuntimeException
   *           if the job throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  RESULT awaitDoneAndGet(long timeout, TimeUnit unit);

  /**
   * Waits if necessary for at most the given time for the job to complete, and then returns its result, if available,
   * or throws its exception according to {@link IExceptionTranslator}, or throws {@link FutureCancelledError} if
   * cancelled, or throws {@link TimedOutError} if waiting timeout elapsed, or throws
   * {@link ThreadInterruptedError} if the current thread was interrupted while waiting.
   * <p>
   * Use a specific {@link IExceptionTranslator} to control exception translation.
   *
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @param exceptionTranslator
   *          to translate the job's exception if the job threw an exception.
   * @return the job's result.
   * @throws ThreadInterruptedError
   *           if the current thread was interrupted while waiting.
   * @throws FutureCancelledError
   *           if the job was cancelled.
   * @throws TimedOutError
   *           if the wait timed out.
   * @throws EXCEPTION
   *           if the job throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(long timeout, TimeUnit unit, Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION;

  /**
   * Registers the given <code>callback</code> to be invoked once the Future enters 'done' state. That is once the
   * associated job completes successfully or with an exception, or was cancelled. The callback is invoked immediately
   * and in the calling thread, if being in 'done' state at the time of invocation. Otherwise, this method returns
   * immediately, and the callback invoked upon transition into 'done' state.
   * <p>
   * The following code snippet illustrates its usage:
   *
   * <pre>
   * <code>
   * Jobs.schedule(new Callable&lt;String&gt;() {
   *
   *   &#064;Override
   *   public String call() throws Exception {
   *     // do some work
   *     return ...;
   *   }
   * })<strong>
   * .whenDone(new IDoneHandler&lt;String&gt;() {
   *
   *   &#064;Override
   *   public void onDone(DoneEvent&lt;String&gt; event) {
   *     // invoked once the job completes, or is cancelled.
   *   }
   * }, RunContexts.copyCurrent())</strong>;
   * </code>
   * </pre>
   *
   * @param callback
   *          callback invoked upon transition into 'done' state.
   * @param runContext
   *          optional {@link RunContext} to invoke the handler on behalf, or <code>null</code> to not invoke on a
   *          specific {@link RunContext}.
   * @return The future to support for method chaining.
   */
  IFuture<RESULT> whenDone(IDoneHandler<RESULT> callback, RunContext runContext);

  /**
   * Schedules a new job to execute the given function after <code>this</code> future completes. Thereby, the function
   * is provided with the result or failure of <code>this</code> future, and is invoked only if neither
   * <code>this</code> future, nor the function's future, nor the {@link RunMonitor} associated with the function's job
   * input is cancelled. If the evaluation of the function throws an exception, it is relayed to consumers of the
   * returned future.
   * <p>
   * Unlike {@link #whenDone(IDoneHandler, RunContext)}, the future returned does not represent <code>this</code>
   * future, but the function's future instead.
   * <p>
   * If the function's future or {@link RunContext} are cancelled, this does not imply that <code>this</code> future is
   * cancelled as well. Propagation of cancellation works only the other way round.
   *
   * @param function
   *          the function to be executed upon completion of <code>this</code> future, and is invoked only if neither
   *          <code>this</code> future, nor the function's future, nor the {@link RunMonitor} associated with the
   *          function's job input is cancelled.
   *          <p>
   *          The function's first argument is provided with <code>this</code> future's result and the second with
   *          <code>this</code> future's exception (if any).
   * @param input
   *          input to schedule the function.
   * @return the future representing the asynchronous execution of the function.
   */
  <FUNCTION_RESULT> IFuture<FUNCTION_RESULT> whenDoneSchedule(IBiFunction<RESULT, Throwable, FUNCTION_RESULT> function, JobInput input);

  /**
   * Provides the same functionality as {@link #whenDoneSchedule(IBiFunction, JobInput)} but is convenience for a job
   * which does not compute a result.
   *
   * @see #whenDoneSchedule(IBiFunction, JobInput)
   */
  IFuture<Void> whenDoneSchedule(IBiConsumer<RESULT, Throwable> function, final JobInput input);

  /**
   * Registers the given listener to be notified about all job lifecycle events related to this Future. If the listener
   * is already registered, that previous registration is replaced.
   *
   * @param listener
   *          listener to be registered.
   * @return A token representing the registration of the given {@link IJobListener}. This token can later be used to
   *         unregister the listener.
   */
  IRegistrationHandle addListener(IJobListener listener);

  /**
   * Registers the given listener to be notified about job lifecycle events related to this Future, and which comply
   * with the given filter. If the listener is already registered, that previous registration is replaced.
   * <p>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobEventFilterBuilder} to create a filter to match multiple criteria
   * joined by logical 'AND' operation.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newEventFilterBuilder()
   *     .andMatchEventType(JobEventType.SCHEDULED, JobEventType.DONE)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   *
   * @param filter
   *          filter to only get notified about events of interest - that is for events accepted by the filter.
   * @param listener
   *          listener to be registered.
   * @return A token representing the registration of the given {@link IJobListener}. This token can later be used to
   *         unregister the listener.
   */
  IRegistrationHandle addListener(IFilter<JobEvent> filter, IJobListener listener);

  /**
   * Associates this {@link IFuture} with an execution hint, which can be evaluated by filters like when listening to
   * job lifecycle events, or when waiting for job completion, or by the job manager.
   *
   * @param hint
   *          the hint to be associated with this {@link IFuture}.
   * @return <code>true</code> if this {@link IFuture} did not already contain the specified hint, or <code>false</code>
   *         otherwise.
   */
  boolean addExecutionHint(String hint);

  /**
   * Removes an execution hint from this {@link IFuture}. Has no effect if not associated yet.
   *
   * @param hint
   *          the hint to be removed from this {@link IFuture}.
   * @return <code>true</code> if this {@link IFuture} contained the specified hint, or <code>false</code> otherwise.
   */
  boolean removeExecutionHint(String hint);

  /**
   * Returns, whether the given 'execution hint' is associated with this {@link IFuture}.
   */
  boolean containsExecutionHint(String hint);
}
