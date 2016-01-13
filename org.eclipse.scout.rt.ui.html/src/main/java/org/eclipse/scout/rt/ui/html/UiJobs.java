/*******************************************************************************
 * Copyright (c) 2014-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.job.filter.future.ModelJobFutureFilter;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.CancellationException;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.InterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimeoutException;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;

/**
 * Helper methods to work with the Job API from UI.
 */
@ApplicationScoped
public class UiJobs {

  /**
   * The maximal timeout to wait for model jobs to complete.
   */
  public static final long AWAIT_TIMEOUT = TimeUnit.HOURS.toMillis(1);

  /**
   * Execution hint to mark futures which represent a poll request.
   */
  public static final String EXECUTION_HINT_POLL_REQUEST = UiJobs.class.getName() + ".pollRequest";

  /**
   * Execution hint to mark futures which represent a 'response-to-json' job.
   */
  public static final String EXECUTION_HINT_RESPONSE_TO_JSON = UiJobs.class.getName() + ".responseToJson";

  /**
   * Waits until all model jobs of the given session are in 'done' state, or require 'user interaction'. If the jobs
   * cannot be awaited because the current thread is interrupted, or because the jobs did not complete within the
   * maximal timeout, the respective exception is handled by the given {@link ExceptionHandler}.
   * <p>
   * The hint {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED} indicates, whether a job requires 'user
   * interaction'.
   *
   * @param clientSession
   *          session to calculate the jobs to wait for; must not be <code>null</code>.
   * @param exceptionHandler
   *          to handle {@link TimeoutException} or {@link InterruptedException}.
   */
  public void awaitModelJobs(final IClientSession clientSession, final Class<? extends ExceptionHandler> exceptionHandler) {
    Assertions.assertNotNull(clientSession, "'ClientSession' must not be null");

    try {
      await(new AndFilter<>(
          ModelJobFutureFilter.INSTANCE,
          new SessionFutureFilter(clientSession)));
    }
    catch (TimeoutException | InterruptedException e) {
      // Handle exception in proper ClientRunContext.
      ClientRunContexts.copyCurrent().withSession(clientSession, true).run(new IRunnable() {

        @Override
        public void run() throws Exception {
          BEANS.get(exceptionHandler).handle(e);
        }
      });
    }
  }

  /**
   * Waits until the given {@link IFuture} is in 'done' state to return its result, or requires 'user interaction'. If
   * the job cannot be awaited because the current thread is interrupted, or because the job is cancelled, or because
   * the job did not complete within the maximal timeout, the respective exception is propagated to the caller. Upon job
   * completion, the method returns with the job's result or its exception.
   * <p>
   * The hint {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED} indicates, whether a job requires 'user
   * interaction'.
   *
   * @param future
   *          the {@link IFuture} to wait until 'done' or requiring 'user interaction'.
   * @return the job's result, or <code>null</code> if requires 'user interaction'.
   * @throws CancellationException
   *           if the job is cancelled.
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting for the job to complete.
   * @throws RuntimeException
   *           if the job completed with an exception.
   */
  public <RESULT> RESULT awaitAndGet(final IFuture<RESULT> future) {
    await(new FutureFilter(future));

    if (future.containsExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)) {
      // The job did not complete yet, but requires 'user interaction'.
      return null;
    }
    else {
      // Return the job's result because it completed.
      return future.awaitDoneAndGet();
    }
  }

  /**
   * Awaits until all jobs matching the given filter are in 'done' state, or require 'user interaction'.
   * <p>
   * The hint {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED} indicates, whether a job requires 'user
   * interaction'.
   *
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting for the job to complete.
   * @throws TimeoutException
   *           if the job did not complete within the maximal timeout.
   */
  public void await(final IFilter<IFuture<?>> filter) {
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchNotExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .andAreSingleExecuting() // only wait for 'one-shot' jobs
        .andMatchNotState(JobState.PENDING) // ignore 'one-shot' jobs which are scheduled with an initial delay because consumed by the poller
        .andMatch(filter)
        .toFilter(), AWAIT_TIMEOUT, TimeUnit.MILLISECONDS);
  }
}
