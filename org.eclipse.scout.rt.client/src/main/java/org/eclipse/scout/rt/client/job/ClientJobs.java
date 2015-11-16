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

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.IJobManager;
import org.eclipse.scout.rt.platform.job.JobInput;

/**
 * Helper class to schedule jobs that run on behalf of a {@link ClientRunContext}. Such jobs are called client jobs.
 * This class is for convenience purpose to facilitate the creation and scheduling of client jobs.
 * <p>
 * <strong>By definition, a <code>ClientJob</code> requires a {@link ClientRunContext}.</strong>
 * <p>
 * The following code snippet illustrates how the job is finally run:
 *
 * <pre>
 * <i>
 * final IClientSession session = ...;
 * final ClientRunContext clientRunContext = ClientRunContexts.copyCurrent()
 *          .withSession(session)
 *          .withLocale(Locale.US);
 * </i>
 * BEANS.get(IJobManager.class).schedule(new IRunnable() {
 *
 *     &#064;Override
 *     public void run() throws Exception {
 *       clientRunContext.run(new IRunnable() {
 *
 *         &#064;Override
 *         public void run() throws Exception {
 *           // do some work
 *         }
 *       });
 *     }
 *   });
 * </pre>
 *
 * @since 5.1
 * @see IJobManager
 * @see ClientRunContext
 */
public final class ClientJobs {

  private ClientJobs() {
  }

  /**
   * Runs the given {@link IRunnable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ClientRunContext} to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>ClientJobs.schedule(...).awaitDone();</code>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   * @see IJobManager#schedule(IRunnable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable) {
    return BEANS.get(IJobManager.class).schedule(runnable, ClientJobs.validateInput(ClientJobs.newInput(ClientRunContexts.copyCurrent())));
  }

  /**
   * Runs the given {@link Callable} asynchronously in another thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. Jobs in the form of a {@link Callable} typically return a computation
   * result to the submitter.
   * <p>
   * The job manager will use a {@link JobInput} with a copy of the current {@link ClientRunContext} to run the job.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Object result = ClientJobs.schedule(...).awaitDoneAndGet();</code>
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @return Future to interact with the job like waiting for its completion, or to cancel its execution, or to get its
   *         computation result.
   * @see IJobManager#schedule(Callable, JobInput)
   */
  public static <RESULT> IFuture<RESULT> schedule(final Callable<RESULT> callable) {
    return BEANS.get(IJobManager.class).schedule(callable, ClientJobs.validateInput(ClientJobs.newInput(ClientRunContexts.copyCurrent())));
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
   * <code>ClientJobs.schedule(...).awaitDone();</code>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param input
   *          information about the job with execution instructions for the job manager to run the job.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   * @see IJobManager#schedule(IRunnable, JobInput)
   */
  public static IFuture<Void> schedule(final IRunnable runnable, final JobInput input) {
    return BEANS.get(IJobManager.class).schedule(runnable, ClientJobs.validateInput(input));
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
   * <code>Object result = ClientJobs.schedule(...).awaitDoneAndGet();</code>
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
    return BEANS.get(IJobManager.class).schedule(callable, ClientJobs.validateInput(input));
  }

  /**
   * Creates a {@link JobInput} initialized with the given {@link ClientRunContext}.
   * <p>
   * The job input returned can be associated with meta information about the job and with execution instructions to
   * tell the job manager how to run the job. The input is to be given to the job manager alongside with the
   * {@link IRunnable} or {@link Callable} to be executed.
   *
   * @param clientRunContext
   *          The {@link ClientRunContext} to be associated with the {@link JobInput} returned; must not be
   *          <code>null</code>.
   */
  public static JobInput newInput(final ClientRunContext clientRunContext) {
    Assertions.assertNotNull(clientRunContext, "ClientRunContext required for client jobs");
    return BEANS.get(JobInput.class).withThreadName("scout-client-thread").withRunContext(clientRunContext);
  }

  /**
   * Creates a filter to accept futures of all client jobs (and not model jobs) that comply with some specific
   * characteristics. The filter returned accepts all client job futures. The filter is designed to support method
   * chaining.
   * <p>
   * To accept both, client and model jobs, use a construct like the following:
   * <p>
   *
   * <pre>
   * new OrFilter&lt;&gt;(ClientJobs.newFutureFilter(), ModelJobs.newFutureFilter());
   * </pre>
   */
  public static ClientJobFutureFilters.Filter newFutureFilter() {
    return new ClientJobFutureFilters.Filter().andMatch(ClientJobFutureFilters.ClientJobFilter.INSTANCE);
  }

  /**
   * Creates a filter to accept events of all client jobs (and not model jobs) that comply with some specific
   * characteristics. The filter returned accepts all client job events. The filter is designed to support method
   * chaining.
   * <p>
   * To accept both, client and model jobs, use a construct like the following:
   * <p>
   *
   * <pre>
   * new OrFilter&lt;&gt;(ClientJobs.newEventFilter(), ModelJobs.newEventFilter());
   * </pre>
   */
  public static ClientJobEventFilters.Filter newEventFilter() {
    return new ClientJobEventFilters.Filter().andMatch(ClientJobEventFilters.CLIENT_JOB_EVENT_FILTER);
  }

  /**
   * Returns <code>true</code> if the given Future belongs to a client job.
   */
  public static boolean isClientJob(final IFuture<?> future) {
    if (future == null) {
      return false;
    }
    if (!(future.getJobInput().getRunContext() instanceof ClientRunContext)) {
      return false;
    }
    if (future.getJobInput().getMutex() instanceof IClientSession) {
      return false; // this is a model job
    }
    return true;
  }

  /**
   * Validates the given {@link JobInput} to conform a client job.
   */
  private static JobInput validateInput(final JobInput input) {
    BEANS.get(ClientJobInputValidator.class).validate(input);
    return input;
  }
}
