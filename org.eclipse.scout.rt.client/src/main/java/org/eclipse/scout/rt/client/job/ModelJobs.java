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
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.internal.future.IFutureTask;

/**
 * Factory and utility methods for {@link IJobManager} to schedule and interact with model jobs.
 * <p/>
 * Use this class to schedule jobs that interact with the model and are executed in sequence among the same session, so
 * that no more than one model job will be active at any given time for that session. Thereto, the mutex object is the
 * session itself. Jobs of this type require a {@link IClientSession} to be set.
 *
 * @since 5.1
 * @see IJobManager
 * @see ClientRunContext
 * @see ModelJobInput
 */
public final class ModelJobs {

  private ModelJobs() {
  }

  /**
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return ModelJobs.runNow(executable, ModelJobInput.fillCurrent());
  }

  /**
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final ModelJobInput input) throws ProcessingException {
    Assertions.assertTrue(isModelThread(), "The current thread must be the model thread");
    Assertions.assertTrue(executable instanceof IRunnable || executable instanceof ICallable, "Illegal executable provided: must be a '%s' or '%s'", IRunnable.class.getSimpleName(), ICallable.class.getSimpleName());

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
   * Runs the given job asynchronously on behalf of a model thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. The job will only commence execution once acquired the model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The job manager will use a default {@link ModelJobInput} with values from the current calling context.
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
    return OBJ.get(IJobManager.class).schedule(executable, ModelJobInput.fillCurrent());
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
   *          describes the job to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final ModelJobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, input);
  }

  /**
   * Runs the given job asynchronously on behalf of a model thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. The job will only commence execution once acquired the model mutex.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * The job manager will use a default {@link ModelJobInput} with values from the current calling context.
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
   *          describes the job to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit) {
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, ModelJobInput.fillCurrent());
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
   *          describes the job to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final ModelJobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, input);
  }

  /**
   * Returns <code>true</code> if the current thread represents the model thread.
   */
  public static boolean isModelThread() {
    final IFuture<?> currentFuture = IFuture.CURRENT.get();
    return ModelJobs.isModelJob(currentFuture) && ((IFutureTask) currentFuture).isMutexOwner();
  }

  /**
   * Returns <code>true</code> if the current Future belongs to a model job.
   *
   * @see ModelJobInput
   * @see ClientRunContext
   */
  public static boolean isModelJob() {
    return ModelJobs.isModelJob(IFuture.CURRENT.get());
  }

  /**
   * Returns <code>true</code> if the given Future belongs to a model job.
   *
   * @see ModelJobInput
   * @see ClientRunContext
   */
  public static boolean isModelJob(final IFuture<?> future) {
    return future != null && ModelJobInput.class.equals(future.getJobInput().getClass());
  }
}
