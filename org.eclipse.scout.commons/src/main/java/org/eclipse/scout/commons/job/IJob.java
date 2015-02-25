/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.job;

import org.eclipse.scout.commons.exception.ProcessingException;

/**
 * Jobs are units of runnable work that can be scheduled to be run by a job manager like {@link JobManager} and
 * are typically executed on behalf of some application-specific context provided by the {@link IJob}. The purpose of
 * the job manager is to provide resources to run given jobs according to the manager's scheduling rule like the maximal
 * number of concurrent running jobs, support for mutual exclusion within the jobs and so on.<br/>
 * Jobs are reusable meaning that once a job has completed, it can be scheduled to run again.
 *
 * @param <RESULT>
 *          the result type of the job's computation; use {@link Void} if this job does not return a result.
 * @see JobManager
 * @since 5.1
 */
public interface IJob<RESULT> {

  /**
   * The {@link IJob} which is currently associated with the current thread.
   */
  ThreadLocal<IJob<?>> CURRENT = new ThreadLocal<>();

  /**
   * @return name to describe the job; is never <code>null</code>.
   */
  String getName();

  /**
   * Attempts to cancel the execution of this {@link IJob}. This attempt will be ignored if the {@link IJob} has already
   * completed or was cancelled. If not running yet, the {@link IJob} will never run. If the job has already started,
   * then the <code>interruptIfRunning</code> parameter determines whether the thread executing this {@link IJob} should
   * be interrupted in an attempt to stop the job.
   *
   * @param interruptIfRunning
   *          <code>true</code> to interrupt the executing thread if the {@link IJob} is already running.
   * @return <code>false</code> if the {@link IJob} could not be cancelled, typically because it has already completed
   *         normally.
   */
  boolean cancel(boolean interruptIfRunning);

  /**
   * Runs this job synchronously on behalf of the current thread. This call blocks the calling thread as long as this
   * job is running.
   *
   * @return the computed result.
   * @throws ProcessingException
   *           if the job throws an exception during execution.
   * @throws JobExecutionException
   *           if the job is already running.
   */
  RESULT runNow() throws ProcessingException, JobExecutionException;

  /**
   * Runs this job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of this
   * method continues to run in parallel.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of this job. To
   * immediately block waiting for a job to complete, you can use constructions of the form
   * <code>result = job.schedule().get();</code>.
   *
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   * @see #schedule(IAsyncFuture)
   */
  IFuture<RESULT> schedule() throws JobExecutionException;

  /**
   * Runs this job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of this
   * method continues to run in parallel.
   * <p/>
   * The given {@link IAsyncFuture} is notified once the job completes successfully or terminates with an exception. The
   * {@link IFuture} returned allows to cancel the execution of this job or to also wait for the job to complete.
   *
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure; is notified from within the
   *          worker-thread that executed the job; is not called if the job never started running.
   * @throws JobExecutionException
   *           if the job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   * @see #schedule()
   */
  IFuture<RESULT> schedule(IAsyncFuture<RESULT> asyncFuture) throws JobExecutionException;
}
