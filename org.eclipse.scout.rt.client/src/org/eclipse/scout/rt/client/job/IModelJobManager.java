/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse  License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.job;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IJobVisitor;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.rt.client.IClientSession;

/**
 * Job manager to run jobs interacting with the client model on behalf of the model-thread. There is one
 * {@link IModelJobManager} per {@link IClientSession}.
 * <p/>
 * Within this manager, jobs are executed in sequence so that no more than one job will be active at any given time. If
 * a {@link IModelJob} gets blocked by entering a {@link IBlockingCondition}, the model-mutex will be released which
 * allows another model-job to run. When being unblocked, the job must compete for the model-mutex anew in order to
 * continue execution.
 *
 * @see {@link IClientSession#getModelJobManager()}
 * @see IModelJob
 * @see IBlockingCondition
 * @since 5.1
 */
public interface IModelJobManager {

  /**
   * Runs the given job synchronously on behalf of the current model-thread. This call blocks the caller as long as the
   * given job is running.
   * <p/>
   * <strong>The calling thread must be the model-thread himself.</strong>
   * <p/>
   * Do not use this method directly. Use {@link IModelJob#runNow()} instead.
   *
   * @param job
   *          the {@link IModelJob} to be run.
   * @param callable
   *          the {@link Callable} to be executed.
   * @return the computed result.
   * @throws ProcessingException
   *           if the job throws an exception during execution.
   * @throws JobExecutionException
   *           if the job is already running or not called on behalf of the model-thread.
   */
  <RESULT> RESULT runNow(IModelJob<RESULT> job, Callable<RESULT> callable) throws ProcessingException, JobExecutionException;

  /**
   * Runs the given job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the executor the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the executor.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   * <p/>
   * Do not use this method directly. Use {@link IModelJob#schedule()} instead.
   *
   * @param job
   *          the {@link IModelJob} to be scheduled.
   * @param callable
   *          the {@link Callable} to be executed.
   * @return {@link Future} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running.
   */
  <RESULT> Future<RESULT> schedule(IModelJob<RESULT> job, Callable<RESULT> callable) throws JobExecutionException;

  /**
   * @return <code>true</code> if the given job is currently blocked because waiting for a {@link IBlockingCondition} to
   *         fall.
   */
  boolean isBlocked(IModelJob<?> modelJob);

  /**
   * @return <code>true</code> if the calling thread is the model-thread.
   */
  boolean isModelThread();

  /**
   * @return <code>true</code> if the model-mutex is currently not acquired.
   */
  boolean isIdle();

  /**
   * Blocks the calling thread until the model-mutex gets available. Does not block if available at time of invocation.
   *
   * @param timeout
   *          the maximal time to wait for the model-mutex to become available.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   * @see {@link #isIdle()}
   */
  boolean waitForIdle(long timeout, TimeUnit unit) throws InterruptedException;

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
   * @see IFuture#cancel(boolean)
   */
  boolean cancel(IJob<?> job, boolean interruptIfRunning);

  /**
   * @return <code>true</code> if the given job was cancelled before it completed normally.
   */
  boolean isCanceled(IJob<?> job);

  /**
   * @return {@link Future} associated with the given job; is <code>null</code> if not scheduled or already completed.
   */
  Future<?> getFuture(IModelJob<?> job);

  /**
   * Interrupts a possible running job, rejects pending jobs and interrupts jobs waiting for a blocking condition to
   * fall. After having shutdown, this {@link IModelJobManager} cannot be used anymore.
   */
  void shutdown();

  /**
   * To visit the running and all pending model jobs.
   *
   * @param visitor
   *          {@link IJobVisitor} called for each {@link IModelJob}.
   */
  void visit(IJobVisitor visitor);

  /**
   * Creates a blocking condition to put a {@link IModelJob} into waiting mode and let another job acquire the
   * model-mutex. This condition can be used across multiple model-threads to wait for the same condition; this
   * condition is reusable upon signaling.
   *
   * @param name
   *          the name of the blocking condition; primarily used for debugging purpose.
   * @return {@link IBlockingCondition}.
   */
  IBlockingCondition createBlockingCondition(String name);

  /**
   * Creates a {@link IProgressMonitor} for the given {@link IModelJob}.
   *
   * @param job
   *          the job to create a {@link IProgressMonitor} for.
   * @return {@link IProgressMonitor}; is never <code>null</code>.
   */
  <RESULT> IProgressMonitor createProgressMonitor(IModelJob<RESULT> job);
}
