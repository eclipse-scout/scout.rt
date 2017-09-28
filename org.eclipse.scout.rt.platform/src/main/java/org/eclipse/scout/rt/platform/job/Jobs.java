/*******************************************************************************
 * Copyright (c) 2010-2016 BSI Business Systems Integration AG.
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

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.filter.event.JobEventFilterBuilder;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilterBuilder;
import org.eclipse.scout.rt.platform.job.internal.ExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.Assertions.AssertionException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;

/**
 * Helper class to schedule jobs that optionally run on behalf of a {@link RunContext}. This class is for convenience
 * purpose to facilitate the creation and scheduling of jobs.
 * <p>
 * Example:
 *
 * <pre>
 * Jobs.schedule(new IRunnable() {
 *
 *   &#64;Override
 *   public void run() throws Exception {
 *     // do something
 *   }
 * }, Jobs.newInput()
 *      .withName("example job")
 *      .withRunContext(RunContexts.copyCurrent()
 *          .withSubject(...)
 *          .withLocale(Locale.US))
 *      .withExecutionTrigger(Jobs.newExecutionTrigger()
 *          .withStartIn(5, TimeUnit.SECONDS)
 *          .withSchedule(FixedDelayScheduleBuilder.repeatForever(10, TimeUnit.SECONDS)));
 * </pre>
 *
 * The following code snippet illustrates how the job is finally run:
 *
 * <pre>
 * BEANS.get(IJobManager.class).schedule(new IRunnable() {
 *
 *   &#64;Override
 *   public void run() throws Exception {
 *     runContext.run(new IRunnable() {
 *
 *       &#064;Override
 *       public void run() throws Exception {
 *         // do some work
 *       }
 *     });
 *   }
 * });
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
   * Returns the job manager.
   */
  public static IJobManager getJobManager() {
    return BEANS.get(IJobManager.class);
  }

  /**
   * Runs the given {@link IRunnable} asynchronously in another thread once the associated execution trigger fires,
   * which depends on both, the trigger's start time and schedule. However, if not set, the job will commence execution
   * immediately at the next reasonable opportunity. In either case, the submitter of the job continues to run in
   * parallel.
   * <p>
   * If the maximal concurrency level for a semaphore aware job is reached, the job is queued until a permit becomes
   * available. As a general rule, jobs compete for an execution permit once being fired by the associated trigger, and
   * in the order as being scheduled. For example, if scheduling two jobs in a row, they very likely will have the same
   * execution time (granularity in milliseconds). However, job manager guarantees the first job to compete for an
   * execution permit before the second job does.
   * <p>
   * The job manager will use the {@link JobInput} as given to control job execution.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * Usage:
   *
   * <pre>
   * Jobs.schedule(new IRunnable() {
   *
   *   &#64;Override
   *   public void run() throws Exception {
   *     // do something
   *   }
   * }, Jobs.newInput()
   *     .withRunContext(ClientRunContexts.copyCurrent())
   *     .withName("job name"));
   * </pre>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param input
   *          information about the job with execution instructions for the job manager to run the job.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   * @see IJobManager#schedule(IRunnable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(runnable, input);
  }

  /**
   * Runs the given {@link Callable} asynchronously in another thread once the associated execution trigger fires, which
   * depends on both, the trigger's start time and schedule. However, if not set, the job will commence execution
   * immediately at the next reasonable opportunity. In either case, the submitter of the job continues to run in
   * parallel.
   * <p>
   * Jobs in the form of a {@link Callable} typically return a computation result to the submitter.
   * <p>
   * If the maximal concurrency level for a semaphore aware job is reached, the job is queued until a permit becomes
   * available. As a general rule, jobs compete for an execution permit once being fired by the associated trigger, and
   * in the order as being scheduled. For example, if scheduling two jobs in a row, they very likely will have the same
   * execution time (granularity in milliseconds). However, job manager guarantees the first job to compete for an
   * execution permit before the second job does.
   * <p>
   * The job manager will use the {@link JobInput} as given to control job execution.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * Usage:
   *
   * <pre>
   * String result = Jobs.schedule(new Callable<String>() {
   *
   *   &#64;Override
   *   public String call() throws Exception {
   *     return "result";
   *   }
   * }, Jobs.newInput()
   *     .withRunContext(ClientRunContexts.copyCurrent())
   *     .withName("job name"))
   *     .awaitDoneAndGet();
   * </pre>
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param input
   *          information about the job with execution instructions for the job manager to run the job.
   * @return Future to interact with the job like waiting for its completion, or to cancel its execution, or to get its
   *         computation result.
   * @see IJobManager#schedule(Callable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(callable, input);
  }

  /**
   * Creates a new {@link JobInput}.
   * <p>
   * The job input returned can be associated with meta information about the job, with execution instructions to tell
   * the job manager how to run the job, and with a {@link RunContext} to be applied during the job's execution. The
   * input is to be given to the job manager alongside with the {@link IRunnable} or {@link Callable} to be executed.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newInput()
   *     .withName("example job")
   *     .withRunContext(ClientRunContexts.copyCurrent())
   *     .withExecutionTrigger(Jobs.newExecutionTrigger()
   *         .withStartIn(5, TimeUnit.SECONDS));
   * </pre>
   */
  public static JobInput newInput() {
    return BEANS.get(JobInput.class);
  }

  /**
   * Returns a builder to create a filter for {@link IFuture} objects. This builder facilitates the creation of a
   * {@link IFuture} filter and to match multiple criteria joined by logical 'AND' operation
   * <p>
   * Example usage:
   *
   * <pre>
   * Jobs.newFutureFilterBuilder()
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   */
  public static FutureFilterBuilder newFutureFilterBuilder() {
    return BEANS.get(FutureFilterBuilder.class);
  }

  /**
   * Returns a builder to create a filter for {@link JobEvent} objects. This builder facilitates the creation of a
   * JobEvent filter and to match multiple criteria joined by logical 'AND' operation.
   * <p>
   * Example usage:
   *
   * <pre>
   * Jobs.newEventFilterBuilder()
   *     .andMatchEventType(JobEventType.SCHEDULED, JobEventType.DONE)
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   */
  public static JobEventFilterBuilder newEventFilterBuilder() {
    return BEANS.get(JobEventFilterBuilder.class);
  }

  /**
   * Creates an execution semaphore to control the maximal number of jobs running concurrently among the same semaphore.
   * <p>
   * With a semaphore in place, a job only commences execution, once a permit is free or gets available. If free, the
   * job commences execution immediately at the next reasonable opportunity, unless no worker thread is available.
   * <p>
   * A semaphore initialized to <code>one</code> allows to run jobs in a mutually exclusive manner, and a semaphore
   * initialized to <code>zero</code> to run no job at all. The number of total permits available can be changed at any
   * time, which allows to adapt the maximal concurrency level to some dynamic criteria like time of day or system load.
   * However, once calling {@link #seal()}, the number of permits cannot be changed anymore, and any attempts will
   * result in an {@link AssertionException}.
   *
   * @param permits
   *          the number of permits.
   */
  public static IExecutionSemaphore newExecutionSemaphore(final int permits) {
    return BEANS.get(ExecutionSemaphore.class).withPermits(permits);
  }

  /**
   * Creates a blocking condition to put a job into waiting mode until the condition falls.
   * <p>
   * If the job belongs to an {@link IExecutionSemaphore}, the job's permit is released and passed to the next competing
   * job of that same semaphore while being blocked.
   * <p>
   * See {@link IBlockingCondition} for more information.
   *
   * @param blocking
   *          initial blocking-state of the blocking condition.
   */
  public static IBlockingCondition newBlockingCondition(final boolean blocking) {
    return BEANS.get(IJobManager.class).newBlockingCondition(blocking);
  }

  /**
   * Creates a trigger to define the schedule upon which the job will commence execution.
   * <p>
   * The trigger mechanism is provided by Quartz Scheduler, meaning that you can profit from the powerful Quartz
   * schedule capabilities.
   * <p>
   * For more information, see <a href="http://www.quartz-scheduler.org">http://www.quartz-scheduler.org</a>.
   */
  public static ExecutionTrigger newExecutionTrigger() {
    return BEANS.get(ExecutionTrigger.class);
  }
}
