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
package org.eclipse.scout.rt.server.job;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.internal.Executables;
import org.eclipse.scout.rt.server.IServerSession;
import org.eclipse.scout.rt.server.context.ServerContext;

/**
 * Factory and utility methods for {@link IJobManager} to schedule and interact with server jobs.
 * <p/>
 * Use this class to schedule jobs that do require a server context for execution. By default, server jobs require a
 * {@link IServerSession} to be set.
 *
 * @since 5.1
 * @see IJobManager
 * @see ServerJobInput
 * @see ServerContext
 */
public final class ServerJobs {

  private ServerJobs() {
  }

  /**
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable) throws ProcessingException {
    return ServerContext.fillCurrent().invoke(Executables.callable(executable));
  }

  /**
   * 'Run-now'-style execution will be removed in 5.1.
   */
  public static <RESULT> RESULT runNow(final IExecutable<RESULT> executable, final ServerJobInput input) throws ProcessingException {
    return input.getContext().invoke(Executables.callable(executable));
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The job manager will use a default {@link ServerJobInput} with values from the current calling context.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ServerJobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable) {
    return OBJ.get(IJobManager.class).schedule(executable, ServerJobInput.fillCurrent());
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ServerJobs.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          describes the job to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final ServerJobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, input);
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The job manager will use a default {@link ServerJobInput} with values from the current calling context.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ServerJobs.schedule(...).awaitDone();</code>.
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
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, ServerJobInput.fillCurrent());
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = ServerJobs.schedule(...).awaitDone();</code>.
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
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final ServerJobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, input);
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
   *          describes the job to be executed.
   * @return Future to cancel the periodic action.
   * @see IJobManager#scheduleAtFixedRate(IRunnable, long, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> scheduleAtFixedRate(final IRunnable runnable, final long initialDelay, final long period, final TimeUnit unit, final ServerJobInput input) {
    return OBJ.get(IJobManager.class).scheduleAtFixedRate(runnable, initialDelay, period, unit, input);
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
   *          describes the job to be executed.
   * @return Future to cancel the periodic action.
   * @see IJobManager#scheduleWithFixedDelay(IRunnable, long, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> scheduleWithFixedDelay(final IRunnable runnable, final long initialDelay, final long delay, final TimeUnit unit, final ServerJobInput input) {
    return OBJ.get(IJobManager.class).scheduleWithFixedDelay(runnable, initialDelay, delay, unit, input);
  }

  /**
   * Returns <code>true</code> if the current Future belongs to a server job.
   *
   * @see ServerJobInput
   * @see ServerContext
   */
  public static boolean isServerJob() {
    return ServerJobs.isServerJob(IFuture.CURRENT.get());
  }

  /**
   * Returns <code>true</code> if the given Future belongs to a server job.
   *
   * @see ServerJobInput
   * @see ServerContext
   */
  public static boolean isServerJob(final IFuture<?> future) {
    return future != null && ServerJobInput.class.equals(future.getJobInput().getClass());
  }
}
