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
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;

/**
 * Factory and utility methods for {@link IJobManager} to schedule jobs that optionally run on behalf of a
 * {@link RunContext}. This class is for convenience purpose to facilitate the creation and scheduling of jobs.
 * <p/>
 * The following code snippet illustrates what happens behind the scene:
 *
 * <pre>
 *   <i>
 *   final RunContext runContext = RunContexts.copyCurrent().withSubject(...).withLocale(Locale.US);
 *   </i>
 *   BEANS.get(IJobManager.class).schedule(new IRunnable() {
 * 
 *     &#064;Override
 *     public void run() throws Exception {
 *       if (runContext == null) {
 *         // do some work
 *       }
 *       else {
 *         runContext.run(new IRunnable() {
 * 
 *           &#064;Override
 *           public void run() throws Exception {
 *             // do some work
 *           }
 *         });
 *       }
 *     }
 *   });
 * </pre>
 *
 * @since 5.1
 * @see IJobManager
 * @see RunContext
 */
public final class Jobs {

  private Jobs() {
  }

  /**
   * Runs the given {@link IRunnable} asynchronously on behalf of a worker thread at the next reasonable opportunity.
   * The caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @return Future to wait for the job's completion, or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable) {
    return Jobs.schedule(runnable, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a worker thread at the next reasonable opportunity. The
   * caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @return Future to wait for the job's completion, or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable) {
    return Jobs.schedule(callable, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Runnable} asynchronously on behalf of a worker thread at the next reasonable opportunity. The
   * caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and optionally tells the job manager in what {@link RunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(Callables.callable(runnable), Jobs.validateInput(input));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a worker thread at the next reasonable opportunity. The
   * caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and optionally tells the job manager in what {@link RunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(callable, Jobs.validateInput(input));
  }

  /**
   * Runs the given {@link Runnable} asynchronously on behalf of a worker thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final long delay, final TimeUnit delayUnit) {
    return Jobs.schedule(runnable, delay, delayUnit, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a worker thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final long delay, final TimeUnit delayUnit) {
    return Jobs.schedule(callable, delay, delayUnit, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Runnable} on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and optionally tells the job manager in what {@link RunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(Callables.callable(runnable), delay, delayUnit, Jobs.validateInput(input));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a worker thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. If the job is subject for mutual exclusion, the job only
   * commence execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and optionally tells the job manager in what {@link RunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(callable, delay, delayUnit, Jobs.validateInput(input));
  }

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, the second after <code>initialDelay+period</code>
   * , the third after <code>initialDelay+period+period</code> and so on. If an execution takes longer than the
   * <code>period</code>, the subsequent execution is delayed and starts only once the current execution completed. The
   * job only terminates via cancellation or termination of the job manager.
   *
   * @param runnable
   *          the runnable to be executed periodically.
   * @param initialDelay
   *          the time to delay first run.
   * @param period
   *          the period between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and optionally
   *          tells the job manager in what {@link RunContext} to run the job.
   * @return Future to cancel the periodic action.
   * @see IJobManager#scheduleAtFixedRate(IRunnable, long, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> scheduleAtFixedRate(final IRunnable runnable, final long initialDelay, final long period, final TimeUnit unit, final JobInput input) {
    return BEANS.get(IJobManager.class).scheduleAtFixedRate(runnable, initialDelay, period, unit, Jobs.validateInput(input));
  }

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, and subsequently with the given
   * <code>delay</code> between the termination of one execution and the commencement of the next. The job only
   * terminates via cancellation or termination of the job manager.
   *
   * @param runnable
   *          the runnable to be executed periodically.
   * @param initialDelay
   *          the time to delay first run.
   * @param delay
   *          the fixed delay between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and optionally
   *          tells the job manager in what {@link RunContext} to run the job.
   * @return Future to cancel the periodic action.
   * @see IJobManager#scheduleWithFixedDelay(IRunnable, long, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> scheduleWithFixedDelay(final IRunnable runnable, final long initialDelay, final long delay, final TimeUnit unit, final JobInput input) {
    return BEANS.get(IJobManager.class).scheduleWithFixedDelay(runnable, initialDelay, delay, unit, Jobs.validateInput(input));
  }

  /**
   * Returns the job manager.
   */
  public static IJobManager getJobManager() {
    return BEANS.get(IJobManager.class);
  }

  /**
   * Creates a {@link JobInput} which is optionally initialized with the given {@link RunContext}.
   * <p/>
   * A <code>JobInput</code> contains information about the job like its name and execution instructions, whereas a
   * <code>RunContext</code> defines contextual values such as <code>Subject</code> and <code>Locale</code>. The context
   * given to the <code>JobInput</code> is applied during the job's execution. A context is created as following:
   *
   * <pre>
   * <code>
   * // to create a "snapshot" of the current calling state
   * RunContexts.copyCurrent();
   * 
   * // to create a "snapshot" of the current calling state, but with some values changed
   * RunContexts.copyCurrent().withSubject(...).withLocale(Locale.US)
   * 
   * // to create an empty context with no values set
   * RunContexts.empty();
   * </code>
   * </pre>
   */
  public static JobInput newInput(final RunContext runContext) {
    return BEANS.get(JobInput.class).withRunContext(runContext);
  }

  /**
   * Creates a filter to accept Futures of all jobs that comply with some specific characteristics. The filter returned
   * accepts all Futures. The filter is designed to support method chaining.
   */
  public static JobFutureFilters.Filter newFutureFilter() {
    return new JobFutureFilters.Filter();
  }

  /**
   * Creates a filter to accept events of all jobs that comply with some specific characteristics. The filter returned
   * accepts all events. The filter is designed to support method chaining.
   */
  public static JobEventFilters.Filter newEventFilter() {
    return new JobEventFilters.Filter();
  }

  /**
   * Validates the given {@link JobInput} and {@link RunContext}.
   */
  private static JobInput validateInput(final JobInput input) {
    BEANS.get(JobInputValidator.class).validate(input);
    return input;
  }
}
