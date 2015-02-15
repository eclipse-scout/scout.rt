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
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.internal.JobMap;
import org.eclipse.scout.commons.job.internal.JobMap.IPutCallback;
import org.eclipse.scout.commons.job.internal.RunNowFuture;
import org.eclipse.scout.commons.logger.IScoutLogger;
import org.eclipse.scout.commons.logger.ScoutLogManager;

/**
 * Job manager to run jobs in parallel on so called worker threads. By default, there is a single {@link JobManager}
 * installed on the JVM. This job manager is based on an unbounded {@link ScheduledThreadPoolExecutor} with a
 * <code>core-pool-size</code> set to reuse worker-threads among different jobs.
 *
 * @see Job
 * @since 5.1
 */
public class JobManager {

  public static final JobManager INSTANCE = new JobManager(); // TODO [dwi]: Job API v5: Obtain JobManager by OBJ.GET

  protected static final IScoutLogger LOG = ScoutLogManager.getLogger(JobManager.class);

  protected static final String PROP_CORE_POOL_SIZE = "org.eclipse.scout.job.corePoolSize";
  protected static final int DEFAULT_CORE_POOL_SIZE = 5; // The number of threads to keep in the pool, even if they are idle.

  protected final ScheduledExecutorService m_executor;
  protected final JobMap m_jobMap;

  public JobManager() {
    m_jobMap = new JobMap();
    m_executor = Assertions.assertNotNull(createExecutor());
  }

  /**
   * Runs the given job synchronously on behalf of the current thread. This call blocks the calling thread as long as
   * this job is running.
   * <p/>
   * Do not use this method directly. Use {@link Job#runNow()} instead.
   *
   * @param job
   *          the {@link IJob} to be run.
   * @param callable
   *          the {@link Callable} to be executed.
   * @throws ProcessingException
   *           if the job throws an exception during execution.
   * @throws JobExecutionException
   *           if the job is already running.
   */
  public <R> R runNow(final IJob<R> job, final Callable<R> callable) throws ProcessingException, JobExecutionException {
    Assertions.assertNotNull(callable);

    // Create a 'RunNow'-Future if not running yet.
    final RunNowFuture<R> future = m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, RunNowFuture<R>>() {

      @Override
      public RunNowFuture<R> onAbsent() {
        return new RunNowFuture<R>(Thread.currentThread());
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

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p/>
   * Do not use this method directly. Use {@link Job#schedule()} instead.
   *
   * @param job
   *          the {@link IJob} to be scheduled.
   * @param callable
   *          the {@link Callable} to be executed.
   * @return {@link Future} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   * @see ExecutorService#submit(Callable)
   */
  public <R> Future<R> schedule(final IJob<R> job, final Callable<R> callable) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, Future<R>>() {

      @Override
      public Future<R> onAbsent() {
        return m_executor.submit(interceptCallable(callable));
      }
    });
  }

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel.
   * <p/>
   * Do not use this method directly. Use {@link Job#schedule(long, TimeUnit)} instead.
   *
   * @param job
   *          the {@link IJob} to be scheduled.
   * @param callable
   *          the {@link Callable} to be executed.
   * @param delay
   *          the delay after which this job is to be run.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @return {@link ScheduledFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   * @see ScheduledExecutorService#schedule(Callable, long, TimeUnit)
   */
  public <R> ScheduledFuture<R> schedule(final IJob<R> job, final Callable<R> callable, final long delay, final TimeUnit delayUnit) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, ScheduledFuture<R>>() {

      @Override
      public ScheduledFuture<R> onAbsent() {
        return m_executor.schedule(interceptCallable(callable), delay, delayUnit);
      }
    });
  }

  /**
   * Periodically runs the given job on behalf of a worker thread at a fixed rate. The caller of this method continues
   * to run in parallel.
   * <p/>
   * Do not use this method directly. Use {@link Job#schedule(long, TimeUnit)} instead.
   *
   * @param job
   *          the {@link IJob} to be scheduled.
   * @param callable
   *          the {@link Callable} to be periodically executed.
   * @param initialDelay
   *          the time to delay first run.
   * @param period
   *          the period between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @return {@link ScheduledFuture} to cancel this periodic action.
   * @throws JobExecutionException
   *           if the job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   * @see ScheduledExecutorService#scheduleAtFixedRate(Runnable, long, long, TimeUnit)
   */
  public <R> ScheduledFuture<R> scheduleAtFixedRate(final IJob<R> job, final Callable<R> callable, final long initialDelay, final long period, final TimeUnit unit) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, ScheduledFuture<R>>() {

      @Override
      public ScheduledFuture<R> onAbsent() {
        final Runnable runnable = adaptRunnable(interceptCallable(callable));

        @SuppressWarnings("unchecked")
        final ScheduledFuture<R> future = (ScheduledFuture<R>) m_executor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
        return future;
      }
    });
  }

  /**
   * Periodically runs the given job on behalf of a worker thread with a fixed delay. The caller of this method
   * continues to run in parallel.
   * <p/>
   * Do not use this method directly. Use {@link Job#schedule(long, TimeUnit)} instead.
   *
   * @param job
   *          the {@link IJob} to be scheduled.
   * @param callable
   *          the {@link Callable} to be periodically executed.
   * @param initialDelay
   *          the time to delay first run.
   * @param delay
   *          the fixed delay between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @return {@link ScheduledFuture} to cancel this periodic action.
   * @throws JobExecutionException
   *           if the job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   * @see ScheduledExecutorService#scheduleWithFixedDelay(Runnable, long, long, TimeUnit)
   */
  public <R> ScheduledFuture<R> scheduleWithFixedDelay(final IJob<R> job, final Callable<R> callable, final long initialDelay, final long delay, final TimeUnit unit) throws JobExecutionException {
    Assertions.assertNotNull(job);
    Assertions.assertNotNull(callable);

    // Schedule the job if not running yet.
    return m_jobMap.putIfAbsentElseReject(job, new IPutCallback<R, ScheduledFuture<R>>() {

      @Override
      public ScheduledFuture<R> onAbsent() {
        final Runnable runnable = adaptRunnable(interceptCallable(callable));

        @SuppressWarnings("unchecked")
        final ScheduledFuture<R> future = (ScheduledFuture<R>) m_executor.scheduleWithFixedDelay(runnable, initialDelay, delay, unit);
        return future;
      }
    });
  }

  /**
   * Interrupts all running jobs and prevents scheduled jobs from running. After having shutdown, this
   * {@link JobManager} cannot be used anymore.
   */
  public void shutdown() {
    m_executor.shutdownNow();

    final Set<Future<?>> futures = m_jobMap.clear();
    for (final Future<?> future : futures) {
      future.cancel(true); // to interrupt the submitter if waiting for the job to complete.
    }
  }

  /**
   * Attempts to cancel execution of the given job.
   *
   * @param job
   *          the job to be canceled.
   * @param interruptIfRunning
   *          <code>true</code> if the thread executing this job should be interrupted; otherwise, in-progress jobs
   *          are allowed to complete.
   * @return <code>false</code> if the job could not be cancelled, typically because it has already completed normally;
   *         <code>true</code> otherwise.
   * @see Future#cancel(boolean)
   */
  public boolean cancel(final IJob<?> job, final boolean interruptIfRunning) {
    return m_jobMap.cancel(job, interruptIfRunning);
  }

  /**
   * @return <code>true</code> if the given job was cancelled before it completed normally.
   */
  public boolean isCanceled(final IJob<?> job) {
    return m_jobMap.isCancelled(job);
  }

  /**
   * @return {@link Future} associated with the given job; is <code>null</code> if not scheduled or already completed.
   */
  public Future<?> getFuture(final IJob<?> job) {
    return m_jobMap.getFuture(job);
  }

  /**
   * To visit all running jobs.
   *
   * @param visitor
   *          {@link IJobVisitor} called for each {@link IJob}.
   */
  public void visit(final IJobVisitor visitor) {
    m_jobMap.visit(visitor);
  }

  /**
   * This method can be used to intercept the concrete {@link Callable} given to the executor for execution. This method
   * is called just before executing the job. The default implementation simply returns the given {@link Callable}.
   *
   * @param callable
   *          {@link Callable} to decorate.
   * @return decorated {@link Callable} to be passed to an {@link ExecutorService}; must not be <code>null</code>.
   */
  protected <R> Callable<R> interceptCallable(final Callable<R> callable) {
    return callable;
  }

  /**
   * Is invoked for periodic actions to adapt the {@link Callable} into a {@link Runnable} to be passed to the
   * {@link ScheduledExecutorService}.
   */
  protected <R> Runnable adaptRunnable(final Callable<R> callable) {
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
   * Creates a {@link IProgressMonitor} for the given {@link IJob}.
   */
  public <R> IProgressMonitor createProgressMonitor(final IJob<R> job) {
    return new IProgressMonitor() {

      @Override
      public boolean isCancelled() {
        return isCanceled(job);
      }
    };
  }

  /**
   * Creates the {@link ScheduledExecutorService} to run jobs in parallel.
   */
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
  protected void handleJobCompleted(final Future<?> future) {
    m_jobMap.remove(future); // Remove the job from the map to allow the job to run again.
  }
}
