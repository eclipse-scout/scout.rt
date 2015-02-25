/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job;

import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ConfigIniUtility;
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.JobMap;
import org.eclipse.scout.commons.job.internal.JobMap.IPutCallback;
import org.eclipse.scout.commons.job.internal.RunNowFuture;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Default implementation of {@link IJobManager}.
 *
 * @since 5.1
 */
public class JobManager implements IJobManager {

  public static final JobManager INSTANCE = new JobManager(); // TODO [dwi]: Job API v5: Obtain JobManager by OBJ.GET

  private static final IScoutLogger LOG = ScoutLogManager.getLogger(JobManager.class);

  @Internal
  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.corePoolSize";
  @Internal
  protected static final int DEFAULT_CORE_POOL_SIZE = 5; // The number of threads to keep in the pool, even if they are idle.

  @Internal
  protected final ScheduledExecutorService m_executor;
  @Internal
  protected final JobMap m_jobMap;

  public JobManager() {
    m_jobMap = new JobMap();
    m_executor = Assertions.assertNotNull(createExecutor());
  }

  @Override
  public final <RESULT> RESULT runNow(final IJob<RESULT> job, final Callable<RESULT> callable) throws ProcessingException, JobExecutionException {
    Assertions.assertNotNull(callable);

    // Create a 'RunNow'-Future if not running yet.
    final RunNowFuture<RESULT> future = m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, RunNowFuture<RESULT>>() {

      @Override
      public RunNowFuture<RESULT> onAbsent() {
        return new RunNowFuture<RESULT>(Thread.currentThread());
      }
    });

    // Run the future on behalf of the current thread.
    try {
      return interceptCallable(callable).call();
    }
    catch (final Exception e) {
      throw ExceptionTranslator.translate(e);
    }
    finally {
      m_jobMap.remove(future);
    }
  }

  @Override
  public final <RESULT> Future<RESULT> schedule(final IJob<RESULT> job, final Callable<RESULT> callable) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, Future<RESULT>>() {

      @Override
      public Future<RESULT> onAbsent() {
        return m_executor.submit(interceptCallable(callable));
      }
    });
  }

  @Override
  public final <RESULT> ScheduledFuture<RESULT> schedule(final IJob<RESULT> job, final Callable<RESULT> callable, final long delay, final TimeUnit delayUnit) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, ScheduledFuture<RESULT>>() {

      @Override
      public ScheduledFuture<RESULT> onAbsent() {
        return m_executor.schedule(interceptCallable(callable), delay, delayUnit);
      }
    });
  }

  @Override
  public final <RESULT> ScheduledFuture<RESULT> scheduleAtFixedRate(final IJob<RESULT> job, final Callable<RESULT> callable, final long initialDelay, final long period, final TimeUnit unit) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, ScheduledFuture<RESULT>>() {

      @Override
      public ScheduledFuture<RESULT> onAbsent() {
        final Runnable runnable = adaptRunnable(interceptCallable(callable));

        @SuppressWarnings("unchecked")
        final ScheduledFuture<RESULT> future = (ScheduledFuture<RESULT>) m_executor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
        return future;
      }
    });
  }

  @Override
  public final <RESULT> ScheduledFuture<RESULT> scheduleWithFixedDelay(final IJob<RESULT> job, final Callable<RESULT> callable, final long initialDelay, final long delay, final TimeUnit unit) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<RESULT, ScheduledFuture<RESULT>>() {

      @Override
      public ScheduledFuture<RESULT> onAbsent() {
        final Runnable runnable = adaptRunnable(interceptCallable(callable));

        @SuppressWarnings("unchecked")
        final ScheduledFuture<RESULT> future = (ScheduledFuture<RESULT>) m_executor.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
        return future;
      }
    });
  }

  @Override
  public final void shutdown() {
    m_executor.shutdownNow();

    final Set<Future<?>> futures = m_jobMap.clear();
    for (final Future<?> future : futures) {
      future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
    }
  }

  @Override
  public final boolean cancel(final IJob<?> job, final boolean interruptIfRunning) {
    return m_jobMap.cancel(job, interruptIfRunning);
  }

  @Override
  public final boolean isCanceled(final IJob<?> job) {
    return m_jobMap.isCancelled(job);
  }

  @Override
  public final Future<?> getFuture(final IJob<?> job) {
    return m_jobMap.getFuture(job);
  }

  @Override
  public final void visit(final IJobVisitor visitor) {
    m_jobMap.visit(visitor);
  }

  @Override
  public <RESULT> IProgressMonitor createProgressMonitor(final IJob<RESULT> job) {
    return new IProgressMonitor() {

      @Override
      public boolean isCancelled() {
        return isCanceled(job);
      }
    };
  }

  /**
   * This method can be used to intercept the concrete {@link Callable} given to the executor for execution. This method
   * is called just before executing the job. The default implementation simply returns the given {@link Callable}.
   *
   * @param callable
   *          {@link Callable} to decorate.
   * @return decorated {@link Callable} to be passed to an {@link ExecutorService}; must not be <code>null</code>.
   */
  protected <RESULT> Callable<RESULT> interceptCallable(final Callable<RESULT> callable) {
    return callable;
  }

  /**
   * Is invoked for periodic actions to adapt the {@link Callable} into a {@link Runnable} to be passed to the
   * {@link ScheduledExecutorService}.
   */
  protected <RESULT> Runnable adaptRunnable(final Callable<RESULT> callable) {
    return new Runnable() {

      @Override
      public void run() {
        try {
          callable.call();
        }
        catch (final Exception e) {
          LOG.error("Unhandled exception thrown during job execution.", e);
        }
      }
    };
  }

  /**
   * Creates the {@link ScheduledExecutorService} to run jobs in parallel.
   */
  @Internal
  protected ScheduledExecutorService createExecutor() {
    final int corePoolSize = ConfigIniUtility.getPropertyInt(PROP_CORE_POOL_SIZE, DEFAULT_CORE_POOL_SIZE);

    final RejectedExecutionHandler rejectionHandler = new RejectedExecutionHandler() {

      @Override
      public void rejectedExecution(final Runnable runnable, final ThreadPoolExecutor executor) {
        handleJobRejected((Future<?>) runnable);
      }
    };

    return new ScheduledThreadPoolExecutor(corePoolSize, new NamedThreadFactory("scout"), rejectionHandler) {

      @Override
      protected void afterExecute(final Runnable runnable, final Throwable t) {
        final Future<?> future = (RunnableFuture<?>) runnable;
        if (future instanceof ScheduledFuture && !future.isDone()) {
          // NOOP: periodic action which is not finished yet but scheduled for a next execution.
        }
        else {
          handleJobCompleted(future);
        }
      }
    };
  }

  /**
   * Method invoked if a job was rejected from being scheduled.
   *
   * @param future
   *          rejected {@link Future}.
   */
  @Internal
  protected void handleJobRejected(final Future<?> future) {
    future.cancel(true); // to indicate the submitter that the job was not executed.

    if (m_executor.isShutdown()) {
      throw new RejectedExecutionException("Job rejected because the job manager is shutdown.");
    }
    else {
      throw new RejectedExecutionException("Job rejected because no more threads or queue slots available.");
    }
  }

  /**
   * Method invoked if a job completed execution.
   *
   * @param future
   *          associated {@link Future}.
   */
  @Internal
  protected void handleJobCompleted(final Future<?> future) {
    m_jobMap.remove(future); // Remove the job from the map to allow the job to run again.
  }
}
