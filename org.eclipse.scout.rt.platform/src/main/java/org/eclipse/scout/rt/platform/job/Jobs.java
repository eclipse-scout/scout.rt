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

import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.context.RunContexts;

/**
 * Helper class to schedule jobs that optionally run on behalf of a {@link RunContext}. This class is for convenience
 * purpose to facilitate the creation and scheduling of jobs.
 * <p/>
 * The following code snippet illustrates how the job is finally run:
 *
 * <pre>
 * <i>
 * final RunContext runContext = RunContexts.copyCurrent()
 *            .withSubject(subject)
 *            .withLocale(Locale.US);
 * </i>
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
   * Runs the given {@link IRunnable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext} to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Jobs.schedule(...).awaitDone();</code>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   * @see IJobManager#schedule(IRunnable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable) {
    return BEANS.get(IJobManager.class).schedule(runnable, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Callable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. Jobs in the form of a {@link Callable} typically return a computation
   * result to the submitter.
   * <p>
   * The job manager will use a {@link JobInput} with a copy of the current {@link RunContext} to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Object result = Jobs.schedule(...).awaitDoneAndGet();</code>
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @return Future to interact with the job like waiting for its completion, or to cancel its execution, or to get its
   *         computation result.
   * @see IJobManager#schedule(Callable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable) {
    return BEANS.get(IJobManager.class).schedule(callable, Jobs.newInput().withRunContext(RunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link IRunnable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p>
   * The job manager will use the {@link JobInput} as provided to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Jobs.schedule(...).awaitDone();</code>
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
   * Runs the given {@link Callable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. Jobs in the form of a {@link Callable} typically return a computation
   * result to the submitter.
   * <p>
   * The job manager will use the {@link JobInput} as provided to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Object result = Jobs.schedule(...).awaitDoneAndGet();</code>
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
   */
  public static JobInput newInput() {
    return BEANS.get(JobInput.class);
  }

  /**
   * Creates a filter to accept futures of all jobs that comply with some specific characteristics. The filter returned
   * accepts all futures. The filter is designed to support method chaining.
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
}
