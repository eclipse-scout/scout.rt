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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.OBJ;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Factory and utility methods for {@link IJobManager} to schedule jobs that run on behalf of a
 * <code>ServerRunContext</code>. Such jobs are called server jobs. This class is for convenience purpose to facilitate
 * the creation and scheduling of server jobs.
 * <p/>
 * <strong>By definition, a <code>ServerJob</code> requires a <code>ServerSession</code> and is run within a new
 * transaction. Other transaction scopes than {@link TransactionScope#REQUIRES_NEW} are not supported.</strong>
 * <p/>
 * The following code snippet illustrates what happens behind the scene:
 *
 * <pre>
 *   <i>
 *   final IServerSession session = ...;
 *   final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent().session(session).locale(Locale.US);
 *   </i>
 *   BEANS.get(IJobManager.class).schedule(new IRunnable() {
 * 
 *       &#064;Override
 *       public void run() throws Exception {
 *         serverRunContext.run(new IRunnable() {
 * 
 *           &#064;Override
 *           public void run() throws Exception {
 *             // do some work
 *           }
 *         });
 *       }
 *     });
 * </pre>
 *
 * @since 5.1
 * @see IJobManager
 * @see ServerRunContext
 */
public final class ServerJobs {

  private ServerJobs() {
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ServerRunContext}.
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
    return ServerJobs.schedule(executable, ServerJobs.newInput(ServerRunContexts.copyCurrent()));
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
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and tells the job manager in what {@link ServerRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final JobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, ServerJobs.validateInput(input));
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ServerRunContext}.
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
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit) {
    return ServerJobs.schedule(executable, delay, delayUnit, ServerJobs.newInput(ServerRunContexts.copyCurrent()));
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
   *          describes the job to be executed and contains execution instructions like 'serial execution' or
   *          'expiration', and tells the job manager in what {@link ServerRunContext} to run the job.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   * @see IJobManager#schedule(IExecutable, long, TimeUnit, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final IExecutable<RESULT> executable, final long delay, final TimeUnit delayUnit, final JobInput input) {
    return OBJ.get(IJobManager.class).schedule(executable, delay, delayUnit, ServerJobs.validateInput(input));
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
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ServerRunContext} to run the job.
   * @return Future to cancel the periodic action.
   * @see IJobManager#scheduleAtFixedRate(IRunnable, long, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> scheduleAtFixedRate(final IRunnable runnable, final long initialDelay, final long period, final TimeUnit unit, final JobInput input) {
    return OBJ.get(IJobManager.class).scheduleAtFixedRate(runnable, initialDelay, period, unit, ServerJobs.validateInput(input));
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
   *          describes the job to be executed and contains execution instructions like 'expiration', and tells the job
   *          manager in what {@link ServerRunContext} to run the job.
   * @return Future to cancel the periodic action.
   * @see IJobManager#scheduleWithFixedDelay(IRunnable, long, long, TimeUnit, JobInput)
   */
  public static IFuture<Void> scheduleWithFixedDelay(final IRunnable runnable, final long initialDelay, final long delay, final TimeUnit unit, final JobInput input) {
    return OBJ.get(IJobManager.class).scheduleWithFixedDelay(runnable, initialDelay, delay, unit, ServerJobs.validateInput(input));
  }

  /**
   * Returns <code>true</code> if the current Future belongs to a server job.
   */
  public static boolean isServerJob() {
    return ServerJobs.isServerJob(IFuture.CURRENT.get());
  }

  /**
   * Returns <code>true</code> if the given Future belongs to a server job.
   */
  public static boolean isServerJob(final IFuture<?> future) {
    if (future == null) {
      return false;
    }
    if (!(future.getJobInput().runContext() instanceof ServerRunContext)) {
      return false;
    }
    return true;
  }

  /**
   * Creates a {@link JobInput} initialized with the given {@link ServerRunContext}
   * <p/>
   * A <code>JobInput</code> contains information about the job like its name and execution instructions, whereas a
   * <code>RunContext</code> defines contextual values such as <code>Subject</code>, <code>Locale</code>,
   * <code>Session</code>, and more. The context given to the <code>JobInput</code> is applied during the job's
   * execution. A context is created as following:
   *
   * <pre>
   * <code>
   * // to create a "snapshot" of the current calling state
   * ServerRunContexts.copyCurrent();
   * 
   * // to create a "snapshot" of the current calling state, but with some values changed
   * ServerRunContexts.copyCurrent().session(...).subject(...).locale(Locale.US)
   * 
   * // to create an empty context with no values set
   * ServerRunContexts.empty();
   * </code>
   * </pre>
   */
  public static JobInput newInput(final ServerRunContext serverRunContext) {
    Assertions.assertNotNull(serverRunContext, "'RunContext' must not be null for server jobs");
    return OBJ.get(JobInput.class).threadName("scout-server-thread").runContext(serverRunContext);
  }

  /**
   * Creates a filter to accept Futures of all server jobs that comply with some specific characteristics. The filter
   * returned accepts all server job Futures. The filter is designed to support method chaining.
   */
  public static ServerJobFutureFilters.Filter newFutureFilter() {
    return new ServerJobFutureFilters.Filter();
  }

  /**
   * Creates a filter to accept events of all server jobs that comply with some specific characteristics. The filter
   * returned accepts all server job events. The filter is designed to support method chaining.
   */
  public static ServerJobEventFilters.Filter newEventFilter() {
    return new ServerJobEventFilters.Filter();
  }

  /**
   * Validates the given {@link JobInput} and {@link ServerRunContext}.
   */
  private static JobInput validateInput(final JobInput input) {
    OBJ.get(ServerJobInputValidator.class).validate(input);
    return input;
  }
}
