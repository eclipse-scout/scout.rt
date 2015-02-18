/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Job manager to run jobs in parallel. By default, there is a single {@link IJobManager} installed on the JVM.
 *
 * @since 5.1
 */
public interface IJobManager {

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
  <RESULT> RESULT runNow(IJob<RESULT> job, Callable<RESULT> callable) throws ProcessingException, JobExecutionException;

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
  <RESULT> Future<RESULT> schedule(IJob<RESULT> job, Callable<RESULT> callable) throws JobExecutionException;

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
  <RESULT> ScheduledFuture<RESULT> schedule(IJob<RESULT> job, Callable<RESULT> callable, long delay, TimeUnit delayUnit) throws JobExecutionException;

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
  <RESULT> ScheduledFuture<RESULT> scheduleAtFixedRate(IJob<RESULT> job, Callable<RESULT> callable, long initialDelay, long period, TimeUnit unit) throws JobExecutionException;

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
  <RESULT> ScheduledFuture<RESULT> scheduleWithFixedDelay(IJob<RESULT> job, Callable<RESULT> callable, long initialDelay, long delay, TimeUnit unit) throws JobExecutionException;

  /**
   * Interrupts all running jobs and prevents scheduled jobs from running. After having shutdown, this
   * {@link JobManager} cannot be used anymore.
   */
  void shutdown();

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
  boolean cancel(IJob<?> job, boolean interruptIfRunning);

  /**
   * @return <code>true</code> if the given job was cancelled before it completed normally.
   */
  boolean isCanceled(IJob<?> job);

  /**
   * @return {@link Future} associated with the given job; is <code>null</code> if not scheduled or already completed.
   */
  Future<?> getFuture(IJob<?> job);

  /**
   * To visit all running jobs.
   *
   * @param visitor
   *          {@link IJobVisitor} called for each {@link IJob}.
   */
  void visit(IJobVisitor visitor);

  /**
   * Creates a {@link IProgressMonitor} for the given {@link IModelJob}.
   *
   * @param job
   *          the job to create a {@link IProgressMonitor} for.
   * @return {@link IProgressMonitor}; is never <code>null</code>.
   */
  <RESULT> IProgressMonitor createProgressMonitor(IJob<RESULT> job);
}
