/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.ui.html;

import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.job.filter.future.ModelJobFutureFilter;
import org.eclipse.scout.rt.client.ui.messagebox.MessageBoxes;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.exception.ExceptionHandler;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.filter.future.FutureFilter;
import org.eclipse.scout.rt.platform.status.IStatus;
import org.eclipse.scout.rt.platform.text.TEXTS;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.concurrent.FutureCancelledError;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.eclipse.scout.rt.ui.html.UiHtmlConfigProperties.UiModelJobsAwaitTimeoutProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods to work with the Job API from UI.
 */
@ApplicationScoped
public class UiJobs {
  private static final Logger LOG = LoggerFactory.getLogger(UiJobs.class);

  /**
   * Execution hint to mark futures which represent a poll request.
   */
  public static final String EXECUTION_HINT_POLL_REQUEST = UiJobs.class.getName() + ".pollRequest";

  /**
   * Execution hint to mark futures which represent a 'response-to-json' job.
   */
  public static final String EXECUTION_HINT_RESPONSE_TO_JSON = UiJobs.class.getName() + ".responseToJson";

  /**
   * The maximal timeout to wait for model jobs to complete.
   */
  private final long m_awaitTimeout = CONFIG.getPropertyValue(UiModelJobsAwaitTimeoutProperty.class);

  /**
   * Waits until all model jobs of the given session are in 'done' state, or require 'user interaction'. If the jobs
   * cannot be awaited because the current thread is interrupted, or because the jobs did not complete within the
   * maximal timeout, the respective exception is handled by the given {@link ExceptionHandler}.
   * <p>
   * The hint {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED} indicates, whether a job requires 'user
   * interaction'.
   *
   * @param clientSession
   *     session to calculate the jobs to wait for; must not be <code>null</code>.
   * @param exceptionHandler
   *     to handle {@link TimedOutError} or {@link ThreadInterruptedError}.
   */
  public void awaitModelJobs(final IClientSession clientSession, final Class<? extends ExceptionHandler> exceptionHandler) {
    Assertions.assertNotNull(clientSession, "'ClientSession' must not be null");

    try {
      await(new AndFilter<>(
          ModelJobFutureFilter.INSTANCE,
          new SessionFutureFilter(clientSession)));
    }
    catch (final ThreadInterruptedError e) {
      // Handle exception in proper ClientRunContext.
      ClientRunContexts.copyCurrent().withSession(clientSession, true).run(() -> BEANS.get(exceptionHandler).handle(e));
    }
    catch (TimedOutError e) {
      handleAwaitModelJobsTimedOutError(clientSession, exceptionHandler, e);
    }
  }

  protected void handleAwaitModelJobsTimedOutError(final IClientSession clientSession, final Class<? extends ExceptionHandler> exceptionHandler, TimedOutError e) {
    LOG.warn("Timeout while waiting for model jobs to finish, cancelling running and scheduled model jobs.");
    cancelModelJobs(clientSession);

    ModelJobs.schedule(() -> {
      MessageBoxes.createOk()
          .withHeader(TEXTS.get("ui.RequestTimeout"))
          .withBody(TEXTS.get("ui.RequestTimeoutMsg"))
          .withSeverity(IStatus.ERROR)
          .show();
    }, ModelJobs.newInput(ClientRunContexts.copyCurrent().withSession(clientSession, true))
        .withName("Handling await model jobs timeout"));
  }

  /**
   * Cancels all running model jobs for the requested session (interrupt if necessary).
   * <p>
   * Exceptions are the calling job and jobs with the hint {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED}
   */
  public void cancelModelJobs(IClientSession clientSession) {
    Jobs.getJobManager().cancel(ModelJobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(clientSession))
        .andMatchNotExecutionHint(EXECUTION_HINT_RESPONSE_TO_JSON)
        .andMatchNotExecutionHint(EXECUTION_HINT_POLL_REQUEST)
        .andMatchNotExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .andMatchNotExecutionHint(ModelJobs.EXECUTION_HINT_NOT_CANCELLABLE_BY_USER)
        .toFilter(), true);
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
   *     the {@link IFuture} to wait until 'done' or requiring 'user interaction'.
   * @return the job's result or {@code null} if {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED} is set.
   * @throws FutureCancelledError
   *     if the job is cancelled.
   * @throws ThreadInterruptedError
   *     if the current thread is interrupted while waiting for the job to complete.
   * @throws RuntimeException
   *     if the job completed with an exception.
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
   * Waits until all jobs matching the given filter are in 'done' state, or require 'user interaction'.
   * <p>
   * The hint {@link ModelJobs#EXECUTION_HINT_UI_INTERACTION_REQUIRED} indicates, whether a job requires 'user
   * interaction'.
   *
   * @throws ThreadInterruptedError
   *     if the current thread is interrupted while waiting for the job to complete.
   * @throws TimedOutError
   *     if the job did not complete within the maximal timeout.
   */
  public void await(final Predicate<IFuture<?>> filter) {
    Jobs.getJobManager().awaitDone(Jobs.newFutureFilterBuilder()
        .andMatchNotExecutionHint(ModelJobs.EXECUTION_HINT_UI_INTERACTION_REQUIRED)
        .andAreSingleExecuting() // only wait for 'one-shot' jobs
        .andMatchNotState(JobState.NEW) // ignore jobs which are not submitted yet (e.g. created via IFuture.whenDoneSchedule)
        .andMatchNotState(JobState.PENDING) // ignore 'one-shot' jobs which are scheduled with an initial delay because consumed by the poller
        .andMatch(filter)
        .toFilter(), getAwaitTimeout(), TimeUnit.SECONDS);
  }

  protected long getAwaitTimeout() {
    return m_awaitTimeout;
  }
}
