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
import org.eclipse.scout.rt.platform.exception.DefaultRuntimeExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.IExceptionTranslator;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.filter.NotFilter;
import org.eclipse.scout.rt.platform.filter.OrFilter;
import org.eclipse.scout.rt.platform.job.filter.event.JobEventFilterBuilder;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.ICancellable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;

/**
 * Represents a {@link Future} to interact with the associated job, or to wait for the job to complete and to query it's
 * computation result. Exceptions thrown during the job's execution are propagated in the form of a
 * {@link ProcessingException}, or translated according to the given {@link IExceptionTranslator}.
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
   * Returns the mutex object, or <code>null</code> if not being a mutually exclusive task.
   */
  IMutex getMutex();

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
   * Returns <code>true</code> if the associated job was cancelled before it completed normally.
   */
  @Override
  boolean isCancelled();

  /**
   * Returns <code>true</code> if the associated job completed either normally, by an exception or was canceled.
   */
  boolean isDone();

  /**
   * Returns the scheduling rule how this job is executed, and is one of {@link #SCHEDULING_RULE_SINGLE_EXECUTION}, or
   * {@link #SCHEDULING_RULE_PERIODIC_EXECUTION_AT_FIXED_RATE}, or
   * {@link #SCHEDULING_RULE_PERIODIC_EXECUTION_WITH_FIXED_DELAY}.
   */
  int getSchedulingRule();

  /**
   * Returns this future's current state; is never <code>null</code>.
   */
  JobState getState();

  /**
   * Waits if necessary for the job to complete, or until cancelled. This method does not throw an exception if
   * cancelled or the computation failed.
   *
   * @throws InterruptedException
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
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws TimeoutException
   *           if the wait timed out.
   */
  void awaitDone(long timeout, TimeUnit unit);

  /**
   * Waits if necessary for the job to complete, and then retrieves its result or throws its exception according to
   * {@link DefaultRuntimeExceptionTranslator}, or throws {@link CancellationException} if cancelled, or throws
   * {@link InterruptedException} if the current thread was interrupted while waiting.
   *
   * @return the job's result.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws CancellationException
   *           if the job was cancelled.
   * @throws RuntimeException
   *           if the job throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  RESULT awaitDoneAndGet();

  /**
   * Waits if necessary for the job to complete, and then retrieves its result or throws its exception according to
   * {@link IExceptionTranslator}, or throws {@link CancellationException} if cancelled, or throws
   * {@link InterruptedException} if the current thread was interrupted while waiting.
   * <p>
   * Use a specific {@link IExceptionTranslator} to control exception translation.
   *
   * @param exceptionTranslator
   *          to translate the job's exception if the job threw an exception.
   * @return the job's result.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws CancellationException
   *           if the job was cancelled.
   * @throws EXCEPTION
   *           if the job throws an exception, and is translated by the given {@link IExceptionTranslator}.
   */
  <EXCEPTION extends Throwable> RESULT awaitDoneAndGet(Class<? extends IExceptionTranslator<EXCEPTION>> exceptionTranslator) throws EXCEPTION;

  /**
   * Waits if necessary for at most the given time for the job to complete, and then retrieves its result or throws its
   * exception according to {@link DefaultRuntimeExceptionTranslator}, or throws {@link CancellationException} if
   * cancelled, or throws {@link TimeoutException} if waiting timeout elapsed, or throws {@link InterruptedException} if
   * the current thread was interrupted while waiting.
   *
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @return the job's result.
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws CancellationException
   *           if the job was cancelled.
   * @throws TimeoutException
   *           if the wait timed out.
   * @throws RuntimeException
   *           if the job throws an exception, and is translated by {@link DefaultRuntimeExceptionTranslator}.
   */
  RESULT awaitDoneAndGet(long timeout, TimeUnit unit);

  /**
   * Waits if necessary for at most the given time for the job to complete, and then retrieves its result or throws its
   * exception according to {@link IExceptionTranslator}, or throws {@link CancellationException} if cancelled, or
   * throws {@link TimeoutException} if waiting timeout elapsed, or throws {@link InterruptedException} if the current
   * thread was interrupted while waiting.
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
   * @throws InterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws CancellationException
   *           if the job was cancelled.
   * @throws TimeoutException
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
   * Registers the given listener to be notified about all job lifecycle events related to this Future. If the listener
   * is already registered, that previous registration is replaced.
   *
   * @param listener
   *          listener to be registered.
   * @return A token representing the registration of the given {@link IJobListener}. This token can later be used to
   *         unregister the listener.
   */
  IJobListenerRegistration addListener(IJobListener listener);

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
  IJobListenerRegistration addListener(IFilter<JobEvent> filter, IJobListener listener);

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
