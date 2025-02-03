/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.session;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.ClientConfigProperties.JobCompletionDelayOnSessionShutdown;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.job.filter.future.ModelJobFutureFilter;
import org.eclipse.scout.rt.client.ui.desktop.IDesktop;
import org.eclipse.scout.rt.client.ui.desktop.IDesktopUIFacade;
import org.eclipse.scout.rt.platform.Bean;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.job.FixedDelayScheduleBuilder;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.Assertions;
import org.eclipse.scout.rt.platform.util.NumberUtility;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedError;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 6.1
 */
@Bean
public class ClientSessionStopHelper {
  private static final Logger LOG = LoggerFactory.getLogger(ClientSessionStopHelper.class);
  public static final String STOP_JOB_HINT = "ClientSessionStopHelper.session.stop";

  public IFuture<?> scheduleStop(final IClientSession clientSession, final boolean force, final String stopReason) {
    // cancel all other - possibly blocking - jobs
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(clientSession))
        .andMatch(ModelJobFutureFilter.INSTANCE)
        .andMatchNotExecutionHint(STOP_JOB_HINT)
        .andMatchNotState(JobState.DONE, JobState.REJECTED)
        .toFilter(), true);

    // add stop job
    LOG.debug("Stop client session {} due to {}", clientSession.getId(), stopReason);
    return ModelJobs.schedule(() -> callStop(clientSession, force),
        ModelJobs.newInput(ClientRunContexts.empty()
                .withSession(clientSession, true))
            .withExecutionHint(STOP_JOB_HINT)
            .withName("Stop client session {} due to {}", clientSession.getId(), stopReason));
  }

  /**
   * This code runs inside the model thread
   * <p>
   * Stops the given session if it is active. To stop it, {@link IDesktopUIFacade#closeFromUI(boolean)} is called, which
   * forces the desktop to close without opening any more forms (which could be the case when using
   * {@link IClientSession#stop()}).
   * <p>
   * If the client session is still active after that, a warning is printed to the log.
   */
  protected void callStop(IClientSession session, boolean force) {
    Assertions.assertNotNull(session);
    if (!session.isActive()) {
      LOG.debug("Client session with ID {} is already inactive.", session.getId());
    }
    else if (session.isStopping()) {
      LOG.debug("Client session with ID {} is already stopping.", session.getId());
    }
    else {
      LOG.debug("Forcing session with ID {} to shut down...", session.getId());
      IDesktop desktop = session.getDesktop();
      if (force && desktop != null) {
        desktop.getUIFacade().closeFromUI(force); // true = force
      }
      else {
        session.stop();
      }
      LOG.info("Client session with ID {} terminated.", session.getId());
    }
  }

  /**
   * Schedule a repeated timer that watches until {@link #cancelRunningJobsExceptCurrentJob(IClientSession)} and then
   * checks if there are still running jobs and cancels them.
   *
   * @return the watcher job
   * @see {@link JobCompletionDelayOnSessionShutdown}
   */
  public IFuture<?> scheduleJobTerminationLoop(final IClientSession session) {
    return Jobs.schedule(() -> {
      if (!IFuture.CURRENT.get().isCancelled()) {
        runJobTermination(session);
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withStartIn(1, TimeUnit.SECONDS)
            .withSchedule(FixedDelayScheduleBuilder.repeatForever(1, TimeUnit.SECONDS))));
  }

  /**
   * @see #scheduleJobTerminationWatcher(IClientSession), {@link JobCompletionDelayOnSessionShutdown}
   */
  public void runJobTermination(IClientSession session) {
    IFuture<?> myself = IFuture.CURRENT.get();
    if (awaitJobs(session, myself)) {
      return;
    }
    if (myself.isCancelled()) {
      return;
    }
    cancelJobs(session, myself);
  }

  /**
   * Wait for completion of all jobs associated with this session except the calling job. The wait time is configured in
   * {@link JobCompletionDelayOnSessionShutdown}.
   * <p>
   * The cal lingjob (calling this method) may be the client session stopping job or a scheduled watcher thread.
   *
   * @return true if all jobs have completed.
   */
  protected boolean awaitJobs(IClientSession session, IFuture<?> caller) {
    Predicate<IFuture<?>> runningJobsFilter = createJobFilter(session, caller);

    // Wait for running jobs to complete before we cancel them
    long seconds = NumberUtility.nvl(CONFIG.getPropertyValue(JobCompletionDelayOnSessionShutdown.class), 0L);
    if (seconds > 0L) {
      try {
        Jobs.getJobManager().awaitDone(runningJobsFilter, seconds, TimeUnit.SECONDS);
      }
      catch (TimedOutError e) { // NOSONAR
        // NOP (not all jobs have been finished within the delay)
      }
      catch (ThreadInterruptedError e) {//NOSONAR
        // NOP
      }
    }

    if (Jobs.getJobManager().getFutures(runningJobsFilter).isEmpty()) {
      return true;
    }
    LOG.warn("Client session {} did not stop within {} seconds. Stopping will continue.", session, seconds);
    return false;
  }

  /**
   * Stop all jobs associated with this session except the calling job.
   * <p>
   * The calling job (calling this method) may be the client session stopping job or a scheduled watcher thread. If it
   * is the client session stopping job then it is not cancelled itself. If it is a watcher thread then also the client
   * session stopping thread is cancelled. That may be that case if the client session stopping job is blocked while
   * calling session store, event listeners or other uncontrollable code.
   */
  protected void cancelJobs(IClientSession session, IFuture<?> caller) {
    Predicate<IFuture<?>> jobFilter = createJobFilter(session, caller);

    // Cancel remaining jobs and write a warning to the logger
    Set<IFuture<?>> runningFutures = Jobs.getJobManager().getFutures(jobFilter);
    if (!runningFutures.isEmpty()) {
      LOG.info("Cancel running model jobs because the client session was shut down. [session={}, user={}, jobs={}]", session, session.getUserId(), runningFutures);
      Jobs.getJobManager().cancel(jobFilter, true);
    }
  }

  /**
   * Filter matches all running jobs that have the same client session associated, except the current thread and model
   * jobs. If the current thread is the stopping model job, we cannot wait for other model threads. They are always
   * cancelled.
   */
  protected Predicate<IFuture<?>> createJobFilter(IClientSession session, IFuture<?> caller) {
    return Jobs.newFutureFilterBuilder()
        .andMatch(new SessionFutureFilter(session))
        .andMatchNotFuture(caller)
        .andMatchNotState(JobState.DONE, JobState.REJECTED)
        .toFilter();
  }
}
