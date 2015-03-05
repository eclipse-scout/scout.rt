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

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.filter.IFilter;
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
   * @return <code>true</code> if there is no running nor scheduled job matching the given Filter at the time of
   *         invocation.
   */
  boolean isDone(IFilter<IFuture<?>> filter);

  /**
   * Blocks the calling thread until all jobs which match the given Filter completed their execution, or the given
   * timeout elapses.
   *
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting for job completion.
   */
  boolean waitUntilDone(IFilter<IFuture<?>> filter, long timeout, TimeUnit unit) throws InterruptedException;

  /**
   * To visit Futures which did not complete yet and match the filter.
   *
   * @param filter
   *          to limit the Futures to be visited.
   * @param visitor
   *          called for each Futures that passed the filter.
   */
  void visit(IFilter<IFuture<?>> filter, IVisitor<IFuture<?>> visitor);

  /**
   * Cancels all Futures which are accepted by the given Filter. Also, any nested 'runNow'-style jobs, which where run
   * on behalf of accepted jobs and did not complete yet, are cancelled as well.
   *
   * @param filter
   *          Filter to control the Futures to be cancelled.
   * @return <code>true</code> if cancel was successful, <code>false</code> otherwise.
   */
  boolean cancel(IFilter<IFuture<?>> filter);

  /**
   * Interrupts all running jobs and prevents scheduled jobs from running. After having shutdown, this job manager
   * cannot be used anymore.
   */
  void shutdown();
}
