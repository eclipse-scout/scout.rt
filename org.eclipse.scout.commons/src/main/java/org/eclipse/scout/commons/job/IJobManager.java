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
package org.eclipse.scout.commons.job;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.internal.IProgressMonitorProvider;

/**
 * Job manager to execute jobs in parallel.
 *
 * @since 5.1
 */
public interface IJobManager<INPUT extends IJobInput> extends IProgressMonitorProvider {

  /**
   * Runs the given job immediately on behalf of the current thread. This call blocks the calling thread as long as
   * the job is running. The job manager will use a default {@link IJobInput} with values from the current calling
   * context.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @return computation result as returned by the callable; is <code>null</code> if operating on a runnable.
   * @throws ProcessingException
   *           if the executable throws an exception during execution.
   * @see #runNow(IExecutable, IJobInput)
   * @see JobInput#defaults()
   */
  <RESULT> RESULT runNow(IExecutable<RESULT> executable) throws ProcessingException;

  /**
   * Runs the given job immediately on behalf of the current thread. This call blocks the calling thread as long as
   * the job is running.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          gives the executable a semantic meaning and contains instructions about its execution.
   * @return computation result as returned by the callable; is <code>null</code> if operating on a runnable.
   * @throws ProcessingException
   *           if the executable throws an exception during execution.
   * @see #runNow(IExecutable)
   */
  <RESULT> RESULT runNow(IExecutable<RESULT> executable, INPUT input) throws ProcessingException;

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. The job manager will use a default {@link IJobInput} with values from the
   * current calling context.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).get();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is rejected by the job manager because no more threads or queue slots are available, or upon
   *           shutdown of the job manager.
   * @see #schedule(IExecutable, IJobInput)
   * @see JobInput#defaults()
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable) throws JobExecutionException;

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).get();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          gives the executable a semantic meaning and contains instructions about its execution.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is rejected by the job manager because no more threads or queue slots are available, or upon
   *           shutdown of the job manager.
   * @see #schedule(IExecutable)
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, INPUT input) throws JobExecutionException;

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. The job manager will use a default {@link IJobInput} with values from the
   * current calling context.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for a job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).get();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param delay
   *          the delay after which the job is to be run.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is rejected by the job manager because no more threads or queue slots are available, or upon
   *           shutdown of the job manager.
   * @see #schedule(IExecutable, long, TimeUnit, IJobInput)
   * @see JobInput#defaults()
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit) throws JobExecutionException;

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for a job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).get();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param delay
   *          the delay after which the job is to be run.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param input
   *          gives the executable a semantic meaning and contains instructions about its execution.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is rejected by the job manager because no more threads or queue slots are available, or upon
   *           shutdown of the job manager.
   * @see #schedule(IExecutable, long, TimeUnit)
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit, INPUT input) throws JobExecutionException;

  /**
   * To visit all Futures which did not complete yet.
   *
   * @param visitor
   *          {@link IFutureVisitor} called for each {@link Future}.
   */
  void visit(IFutureVisitor visitor);

  /**
   * Interrupts all running jobs and prevents scheduled jobs from running. After having shutdown, this job manager
   * cannot be used anymore.
   */
  void shutdown();
}
