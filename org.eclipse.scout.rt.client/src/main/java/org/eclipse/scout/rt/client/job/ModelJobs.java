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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.internal.future.IFutureTask;

/**
 * Factory and utility methods for {@link IJobManager} to schedule jobs that run on behalf of a {@link ClientRunContext}
 * and are executed in sequence among the same session. Such jobs are called model jobs with the characteristics that
 * only one model job is active at any given time for the same session. Model jobs are used to interact with the client
 * model to read and write model values. This class is for convenience purpose to facilitate the creation and scheduling
 * of model jobs.
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
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return ModelJobs.runNow(executable, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final JobInput input) throws ProcessingException {
    Assertions.assertTrue(isModelThread(), "The current thread must be the model thread");
    Assertions.assertTrue(executable instanceof IRunnable || executable instanceof ICallable, "Illegal executable provided: must be a '%s' or '%s'", IRunnable.class.getSimpleName(), ICallable.class.getSimpleName());
    validateInput(input);

    final RunContext runContext = input.runContext();

    if (executable instanceof IRunnable) {
      runContext.run((IRunnable) executable);
      return null;
    }
    else {
      return runContext.call((ICallable<RESULT>) executable);
    }
  }

  /**
   * Runs the given job asynchronously on behalf of a model thread at the next reasonable opportunity. The caller of
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
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable) {
    return ModelJobs.schedule(executable, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * Runs the given job asynchronously on behalf of a model thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. The job will only commence execution once acquired the model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ClientRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final JobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, ModelJobs.validateInput(input));
  }

  /**
   * Runs the given job asynchronously on behalf of a model thread after the specified delay has elapsed. The caller of
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
    return ModelJobs.schedule(executable, delay, delayUnit, ModelJobs.newInput(ClientRunContexts.copyCurrent()));
  }

  /**
   * Runs the given job asynchronously on behalf of a model thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. The job will only commence execution once acquired the model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ModelJobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
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
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, ModelJobs.validateInput(input));
  }

  /**
   * Returns <code>true</code> if the current thread represents the model thread. At any given time, there is only one
   * model thread.
   */
  public static boolean isModelThread() {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    return ModelJobs.isModelJob(currentFuture) && ((IFutureTask) currentFuture).isMutexOwner();
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
   * Creates a {@link JobInput} initialized with the given {@link ClientRunContext}.
   */
  public static JobInput newInput(final ClientRunContext clientRunContext) {
    Assertions.assertNotNull(clientRunContext, "'RunContext' must not be null for model jobs");
    Assertions.assertNotNull(clientRunContext.session(), "'ClientSession' must not be null for model jobs");
    return OBJ.get(JobInput.class).threadName("scout-model-thread").runContext(clientRunContext).mutex(clientRunContext.session());
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
    return new ClientJobFutureFilters.Filter().andFilter(ClientJobFutureFilters.ModelJobFilter.INSTANCE);
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
    return new ClientJobEventFilters.Filter().andFilter(ClientJobEventFilters.MODEL_JOB_EVENT_FILTER);
  }

  /**
   * Validates the given {@link JobInput} and {@link ClientRunContext}.
   */
  private static JobInput validateInput(final JobInput input) {
    OBJ.get(ModelJobInputValidator.class).validate(input);
    return input;
  }
}
