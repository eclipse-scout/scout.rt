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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.OBJ;
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
 *   final RunContext runContext = RunContexts.copyCurrent().subject(...).locale(Locale.US);
 *   </i>
 *   Beans.get(IJobManager.class).schedule(new IRunnable() {
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
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return Jobs.runNow(executable, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final JobInput input) throws ProcessingException {
    Assertions.assertTrue(executable instanceof IRunnable || executable instanceof ICallable, "Illegal executable provided: must be a '%s' or '%s'", IRunnable.class.getSimpleName(), ICallable.class.getSimpleName());
    validateInput(input);

    final RunContext runContext = input.getRunContext();

    if (executable instanceof IRunnable) {
      runContext.run((IRunnable) executable);
      return null;
    }
    else {
      return runContext.call((ICallable<RESULT>) executable);
    }
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable) {
    return Jobs.schedule(executable, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and optionally tells the job manager in what {@link RunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final JobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, Jobs.validateInput(input));
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit) {
    return Jobs.schedule(executable, delay, delayUnit, Jobs.newInput(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = Jobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
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
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, Jobs.validateInput(input));
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
    return OBJ.get(IJobManager.class).scheduleAtFixedRate(runnable, initialDelay, period, unit, Jobs.validateInput(input));
  }

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, and subsequently with the given
   * <code>delay</code> between the termination of one execution and the commencement of the next. The
   * job only terminates via cancellation or termination of the job manager.
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
    return OBJ.get(IJobManager.class).scheduleWithFixedDelay(runnable, initialDelay, delay, unit, Jobs.validateInput(input));
  }

  /**
   * Returns the job manager.
   */
  public static IJobManager getJobManager() {
    return OBJ.get(IJobManager.class);
  }

  /**
   * Creates a {@link JobInput} with the optional {@link RunContext} to be given to the job manager. Use
   * <code>null</code> for <code>runContext</code> to not run the job on behalf of a {@link RunContext}.
   */
  public static JobInput newInput(final RunContext runContext) {
    return OBJ.get(JobInput.class).runContext(runContext);
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
   * Returns <code>true</code> if the given {@link ProcessingException} describes a job timeout.
   */
  public static boolean isTimeout(final ProcessingException e) {
    if (e instanceof JobExecutionException) {
      return ((JobExecutionException) e).isTimeout();
    }
    else {
      return false;
    }
  }

  /**
   * Returns <code>true</code> if the given {@link ProcessingException} describes a job cancellation.
   */
  public static boolean isCancellation(final ProcessingException e) {
    if (e instanceof JobExecutionException) {
      return ((JobExecutionException) e).isCancellation();
    }
    else {
      return false;
    }
  }

  /**
   * Validates the given {@link JobInput} and {@link RunContext}.
   */
  private static JobInput validateInput(final JobInput input) {
    OBJ.get(JobInputValidator.class).validate(input);
    return input;
  }
}
