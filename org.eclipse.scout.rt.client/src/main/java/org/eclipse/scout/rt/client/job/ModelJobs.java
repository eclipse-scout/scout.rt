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
package org.eclipse.scout.rt.client.job;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.Callables;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.session.ClientSessionProvider;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.internal.JobFutureTask;

/**
 * Factory and utility methods for {@link IJobManager} to schedule jobs that run on behalf of a {@link ClientRunContext}
 * and are executed in sequence among the same session. Such jobs are called model jobs with the characteristics that
 * only one model job is active at any given time for the same session. Model jobs are used to interact with the client
 * model to read and write model values. This class is for convenience purpose to facilitate the creation and scheduling
 * of model jobs.
 * <p/>
 * <strong>By definition, a <code>ModelJob</code> requires a <code>ClientSession</code>.</strong>
 * <p/>
 * The following code snippet illustrates what happens behind the scene:
 *
 * <pre>
 *   <i>
 *   final IClientSession session = ...;
 *   final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent().session(session).locale(Locale.US);
 *   </i>
 *   BEANS.get(IJobManager.class).schedule(new IRunnable() {
 * 
 *       &#064;Override
 *       public void run() throws Exception {
 *         clientRunContext.run(new IRunnable() {
 * 
 *           &#064;Override
 *           public void run() throws Exception {
 *             // do some work
 *           }
 *         });
 *       }
 *     }, BEANS.get(JobInput.class).<strong>mutex(session)</strong>);
 *
 * </pre>
 *
 * @since 5.1
 * @see IJobManager
 * @see ClientRunContext
 */
public final class ModelJobs {

  private ModelJobs() {
  }

  /**
   * Runs the given {@link IRunnable} asynchronously on behalf of a model thread at the next reasonable opportunity. The
   * caller of
   * this method continues to run in parallel. The job will only commence execution once acquired the model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ClientRunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable) {
    return ModelJobs.schedule(runnable, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a model thread at the next reasonable opportunity. The
   * caller of this method continues to run in parallel. The job will only commence execution once acquired the model
   * mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ClientRunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable) {
    return ModelJobs.schedule(callable, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link IRunnable} asynchronously on behalf of a model thread at the next reasonable opportunity. The
   * caller of this method continues to run in parallel. The job will only commence execution once acquired the model
   * mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ClientRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(Callables.callable(runnable), ModelJobs.validateInput(input));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a model thread at the next reasonable opportunity. The
   * caller of this method continues to run in parallel. The job will only commence execution once acquired the model
   * mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ClientRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(callable, ModelJobs.validateInput(input));
  }

  /**
   * Runs the given {@link IRunnable} asynchronously on behalf of a model thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. The job will only commence execution once acquired the
   * model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ClientRunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
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
    return ModelJobs.schedule(runnable, delay, delayUnit, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a model thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. The job will only commence execution once acquired the
   * model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ClientRunContext}.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
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
    return ModelJobs.schedule(callable, delay, delayUnit, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * Runs the given {@link IRunnable} asynchronously on behalf of a model thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. The job will only commence execution once acquired the
   * model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ClientRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(Callables.callable(runnable), delay, delayUnit, ModelJobs.validateInput(input));
  }

  /**
   * Runs the given {@link Callable} asynchronously on behalf of a model thread after the specified delay has elapsed.
   * The caller of this method continues to run in parallel. The job will only commence execution once acquired the
   * model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ClientRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(callable, delay, delayUnit, ModelJobs.validateInput(input));
  }

  /**
   * Returns <code>true</code> if the current thread represents the model thread for the current client session. At any
   * given time, there is only one model thread per client session.
   */
  public static boolean isModelThread() {
    return ModelJobs.isModelThread(ClientSessionProvider.currentSession());
  }

  /**
   * Returns <code>true</code> if the current thread represents the model thread for the given client session. At any
   * given time, there is only one model thread per client session.
   */
  public static boolean isModelThread(IClientSession clientSession) {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    return ModelJobs.isModelJob(currentFuture) && ((JobFutureTask) currentFuture).isMutexOwner() && (IClientSession) currentFuture.getJobInput().mutex() == clientSession;
  }

  /**
   * Returns <code>true</code> if the given Future belongs to a model job.
   */
  public static boolean isModelJob(final IFuture<?> future) {
    if (future == null) {
      return false;
    }
    if (!(future.getJobInput().runContext() instanceof ClientRunContext)) {
      return false;
    }
    if (!(future.getJobInput().mutex() instanceof IClientSession)) {
      return false; // this is a client job
    }
    return true;
  }

  /**
   * Creates a {@link JobInput} initialized with the given {@link ClientRunContext}
   * <p/>
   * A <code>JobInput</code> contains information about the job like its name and execution instructions, whereas a
   * <code>RunContext</code> defines contextual values such as <code>Subject</code>, <code>Locale</code>,
   * <code>Session</code>, and more. The context given to the <code>JobInput</code> is applied during the job's
   * execution. A context is created as following:
   *
   * <pre>
   * <code>
   * // to create a "snapshot" of the current calling state
   * ClientRunContexts.copyCurrent();
   * 
   * // to create a "snapshot" of the current calling state, but with some values changed
   * ClientRunContexts.copyCurrent().session(...).subject(...).locale(Locale.US)
   * 
   * // to create an empty context with no values set
   * ClientRunContexts.empty();
   * </code>
   * </pre>
   */
  public static JobInput newInput(final ClientRunContext clientRunContext) {
    Assertions.assertNotNull(clientRunContext, "'RunContext' must not be null for model jobs");
    Assertions.assertNotNull(clientRunContext.session(), "'ClientSession' must not be null for model jobs");
    return BEANS.get(JobInput.class).threadName("scout-model-thread").runContext(clientRunContext).mutex(clientRunContext.session());
  }

  /**
   * Creates a filter to accept Futures of all model jobs (and not client jobs) that comply with some specific
   * characteristics. The filter returned accepts all model job Futures. The filter is designed to support method
   * chaining.
   * <p/>
   * To accept both, model and client jobs, use a construct like the following:
   *
   * <pre>
   * new OrFilter&lt;&gt;(ModelJobs.newFutureFilter(), ClientJobs.newFutureFilter());
   * </pre>
   */
  public static ClientJobFutureFilters.Filter newFutureFilter() {
    return new ClientJobFutureFilters.Filter().andMatch(ClientJobFutureFilters.ModelJobFilter.INSTANCE);
  }

  /**
   * Creates a filter to accept events of all model jobs (and not client jobs) that comply with some specific
   * characteristics. The filter returned accepts all model job events. The filter is designed to support method
   * chaining.
   * <p/>
   * To accept both, model and client jobs, use a construct like the following:
   *
   * <pre>
   * new OrFilter&lt;&gt;(ModelJobs.newEventFilter(), ClientJobs.newEventFilter());
   * </pre>
   */
  public static ClientJobEventFilters.Filter newEventFilter() {
    return new ClientJobEventFilters.Filter().andMatch(ClientJobEventFilters.MODEL_JOB_EVENT_FILTER);
  }

  /**
   * Validates the given {@link JobInput} and {@link ClientRunContext}.
   */
  private static JobInput validateInput(final JobInput input) {
    BEANS.get(ModelJobInputValidator.class).validate(input);
    return input;
  }
}
