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

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.server.context.ServerRunContext;
import org.eclipse.scout.rt.server.context.ServerRunContexts;
import org.eclipse.scout.rt.server.transaction.TransactionScope;

/**
 * Helper class to schedule jobs that run on behalf of a {@link ServerRunContext}. Such jobs are called server jobs.
 * This class is for convenience purpose to facilitate the creation and scheduling of server jobs.
 * <p>
 * <strong>By definition, a <code>ServerJob</code> requires a {@link ServerRunContext} with transaction scope
 * {@link TransactionScope#REQUIRES_NEW}.</strong>
 * <p>
 * The following code snippet illustrates how the job is finally run:
 *
 * <pre>
 * <i>
 * final IServerSession session = ...;
 * final ServerRunContext serverRunContext = ServerRunContexts.copyCurrent()
 *            .withSession(session)
 *            .withLocale(Locale.US);
 * </i>
 * BEANS.get(IJobManager.class).schedule(new IRunnable() {
 *
 *   &#64;Override
 *   public void run() throws Exception {
 *     serverRunContext.run(new IRunnable() {
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
 * @see ServerRunContext
 */
public final class ServerJobs {

  private ServerJobs() {
  }

  /**
   * Runs the given {@link IRunnable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ServerRunContext} to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>ServerJobs.schedule(...).awaitDone();</code>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   * @see IJobManager#schedule(IRunnable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable) {
    return BEANS.get(IJobManager.class).schedule(runnable, ServerJobs.validateInput(ServerJobs.newInput(ServerRunContexts.copyCurrent())));
  }

  /**
   * Runs the given {@link Callable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. Jobs in the form of a {@link Callable} typically return a computation
   * result to the submitter.
   * <p>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ServerRunContext} to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Object result = ServerJobs.schedule(...).awaitDoneAndGet();</code>
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @return Future to interact with the job like waiting for its completion, or to cancel its execution, or to get its
   *         computation result.
   * @see IJobManager#schedule(Callable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable) {
    return BEANS.get(IJobManager.class).schedule(callable, ServerJobs.validateInput(ServerJobs.newInput(ServerRunContexts.copyCurrent())));
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
   * <code>ServerJobs.schedule(...).awaitDone();</code>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param input
   *          information about the job with execution instructions for the job manager to run the job.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   * @see IJobManager#schedule(IRunnable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(runnable, ServerJobs.validateInput(input));
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
   * <code>Object result = ServerJobs.schedule(...).awaitDoneAndGet();</code>
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
    return BEANS.get(IJobManager.class).schedule(callable, ServerJobs.validateInput(input));
  }

  /**
   * Creates a {@link JobInput} initialized with the given {@link ServerRunContext}.
   * <p>
   * The job input returned can be associated with meta information about the job and with execution instructions to
   * tell the job manager how to run the job. The input is to be given to the job manager alongside with the
   * {@link IRunnable} or {@link Callable} to be executed.
   *
   * @param serverRunContext
   *          The {@link ServerRunContext} to be associated with the {@link JobInput} returned; must not be
   *          <code>null</code>.
   */
  public static JobInput newInput(final ServerRunContext serverRunContext) {
    Assertions.assertNotNull(serverRunContext, "ServerRunContext required for server jobs");
    return BEANS.get(JobInput.class).withThreadName("scout-server-thread").withRunContext(serverRunContext);
  }

  /**
   * Creates a filter to accept futures of all server jobs that comply with some specific characteristics. The filter
   * returned accepts all server job futures. The filter is designed to support method chaining.
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
   * Returns <code>true</code> if the given Future belongs to a server job.
   */
  public static boolean isServerJob(final IFuture<?> future) {
    if (future == null) {
      return false;
    }
    if (!(future.getJobInput().getRunContext() instanceof ServerRunContext)) {
      return false;
    }
    return true;
  }

  /**
   * Validates the given {@link JobInput} to conform a server job.
   */
  private static JobInput validateInput(final JobInput input) {
    BEANS.get(ServerJobInputValidator.class).validate(input);
    return input;
  }
}
