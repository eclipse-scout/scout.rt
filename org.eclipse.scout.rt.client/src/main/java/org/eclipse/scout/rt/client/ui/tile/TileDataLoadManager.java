/*
 * Copyright (c) 2010, 2024 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.scout.rt.client.ui.tile;

import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.client.context.ClientRunContexts;
import org.eclipse.scout.rt.client.job.ModelJobs;
import org.eclipse.scout.rt.client.job.filter.future.ModelJobFutureFilter;
import org.eclipse.scout.rt.platform.ApplicationScoped;
import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.config.AbstractPositiveIntegerConfigProperty;
import org.eclipse.scout.rt.platform.config.CONFIG;
import org.eclipse.scout.rt.platform.context.RunMonitor;
import org.eclipse.scout.rt.platform.job.IExecutionSemaphore;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.platform.job.JobState;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.job.listener.JobEventType;
import org.eclipse.scout.rt.platform.util.ObjectUtility;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.job.filter.future.SessionFutureFilter;

/**
 * @since 5.2
 */
@ApplicationScoped
public class TileDataLoadManager {

  private static final String MANUAL_CANCELLATION_MARKER = "cancelledByTileDataLoadManager";
  private final IExecutionSemaphore m_tileExecutionSemaphore = Jobs.newExecutionSemaphore(CONFIG.getPropertyValue(TileMaxConcurrentDataLoadThreadsProperty.class)).seal();

  public TileDataLoadManager() {
    // do not handle jobs we cancelled with TileDataLoadManager.cancel() -> (MANUAL_CANCELLATION_MARKER)
    // these tiles are not visible and will be reloaded automatically when activated, no need to display an error to the user
    // if the job is canceled elsewhere (i.e. no permit is available etc.) tiles might be visible and user needs to see an error message
    Jobs.getJobManager().addListener(Jobs.newEventFilterBuilder()
        .andMatchEventType(JobEventType.JOB_STATE_CHANGED)
        .andMatchState(JobState.DONE)
        .andMatchName(ITileGrid.ASYNC_LOAD_JOBNAME_PREFIX)
        .andMatchNotExecutionHint(MANUAL_CANCELLATION_MARKER)
        .toFilter(),
        event -> {
          if (event.getData().getFuture().isCancelled()) { // still needed, MANUAL_CANCELLATION_MARKER used to filter Jobs cancelled manually, Jobs cancelled e.g. when expired should be handled here
            final ClientRunContext runContext = (ClientRunContext) event.getData().getFuture().getJobInput().getRunContext();
            ModelJobs.schedule(() -> {
              ITileLoadCancellable tileLoadCancellable = runContext.getProperty(ITileGrid.RUN_CONTEXT_TILE_LOAD_CANCELLABLE);
              tileLoadCancellable.onLoadDataCancel();
            }, ModelJobs.newInput(runContext.copy().withRunMonitor(BEANS.get(RunMonitor.class))).withName("handling of cancelled tile data load jobs"));
          }
        });
  }

  public IFuture<Void> schedule(IRunnable runnable, JobInput jobInput) {
    return Jobs.schedule(runnable, jobInput
        .withExecutionSemaphore(m_tileExecutionSemaphore)
        .withExpirationTime(CONFIG.getPropertyValue(TileDataLoadQueueTimeoutSeconds.class), TimeUnit.SECONDS));
  }

  public void cancel(String excludeJobName, String windowIdentifier) {
    Set<IFuture<?>> futures = Jobs.getJobManager().getFutures(Jobs.newFutureFilterBuilder()
        .andMatchRunContext(ClientRunContext.class)
        .andMatchNot(ModelJobFutureFilter.INSTANCE)
        .andMatchNotState(JobState.DONE)
        .andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
        .andMatch(new JobExcludeCurrentByIdentifierFilter(excludeJobName, windowIdentifier))
        .toFilter());

    // mark futures we cancelled, so they can be filtered in the error listener above
    for (IFuture<?> future : futures) {
      future.addExecutionHint(MANUAL_CANCELLATION_MARKER);
    }

    // cancel running jobs that do not have the excludeJobName but share the same windowIdentifier
    Jobs.getJobManager().cancel(Jobs.newFutureFilterBuilder()
        .andMatchFuture(futures).toFilter(), true);
  }

  public void runInModelJob(IRunnable r) {
    ModelJobs.schedule(r,
        ModelJobs.newInput(ClientRunContexts.copyCurrent()
            .withRunMonitor(BEANS.get(RunMonitor.class))) // do not use same RunMonitor since it might have been canceled and job will not execute in that case
            .withName("setting tile data"));
  }

  public static class JobExcludeCurrentByIdentifierFilter implements Predicate<IFuture<?>> {

    private final String m_asyncLoadIdentifierName;
    private final String m_windowIdentifier;

    public JobExcludeCurrentByIdentifierFilter(String asyncLoadIdentifier, String windowIdentifier) {
      m_asyncLoadIdentifierName = asyncLoadIdentifier;
      m_windowIdentifier = windowIdentifier;
    }

    @Override
    public boolean test(final IFuture<?> future) {
      JobInput jobInput = future.getJobInput();
      return jobInput != null
          && ObjectUtility.equals(future.getJobInput().getName(), ITileGrid.ASYNC_LOAD_JOBNAME_PREFIX)
          && !jobInput.getExecutionHints().contains(ITileGrid.ASYNC_LOAD_IDENTIFIER_PREFIX + m_asyncLoadIdentifierName)
          && jobInput.getExecutionHints().contains(ITileGrid.WINDOW_IDENTIFIER_PREFIX + m_windowIdentifier);
    }
  }

  public static class TileMaxConcurrentDataLoadThreadsProperty extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.tiles.maxConcurrentDataLoadThreads";
    }

    @Override
    public String description() {
      return "Maximum number of threads per server that can be created to load tiles. The default value is 25.";
    }

    @Override
    public Integer getDefaultValue() {
      return 25;
    }
  }

  public static class TileDataLoadQueueTimeoutSeconds extends AbstractPositiveIntegerConfigProperty {

    @Override
    public String getKey() {
      return "scout.tiles.dataLoadQueueTimeoutSeconds";
    }

    @Override
    public String description() {
      return "Maximum number of seconds a tile load job can execute until it is automatically cancelled. The default value is 2 minutes.";
    }

    @Override
    public Integer getDefaultValue() {
      return 120;
    }
  }
}
