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
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IExecutable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.IVisitor;
import org.eclipse.scout.commons.filter.AlwaysFilter;
import org.eclipse.scout.commons.filter.AndFilter;
import org.eclipse.scout.commons.filter.IFilter;
import org.eclipse.scout.commons.filter.NotFilter;
import org.eclipse.scout.commons.filter.OrFilter;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;

/**
 * Job manager to execute jobs in parallel.
 * <p/>
 * This job manager supports sequential execution of jobs that belong to the same mutex object (mutual exclusion),
 * meaning that no more than one job will be active at any given time for that mutex object. Also, jobs can be put into
 * waiting state to wait for a {@link IBlockingCondition} to fall. Thereby, the condition can be used across multiple
 * jobs or any thread. See {@link IBlockingCondition} for more information.
 *
 * @since 5.1
 */
public interface IJobManager {

  /**
   * Runs the given job asynchronously on behalf of a worker thread at the next reasonable opportunity. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param input
   *          describes the job to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, JobInput input);

  /**
   * Runs the given job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of
   * this method continues to run in parallel. If the job is subject for mutual exclusion, the job only commence
   * execution once acquired the mutex.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of the job. To
   * immediately block waiting for the job to complete, you can use constructions of the form
   * <code>result = jobManager.schedule(...).awaitDone();</code>.
   *
   * @param executable
   *          executable to be executed; must be either a {@link IRunnable} or {@link ICallable} for a value-returning
   *          job.
   * @param delay
   *          the delay after which the job should commence execution.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param input
   *          describes the job to be executed.
   * @return Future to wait for the job's completion or to cancel the job's execution.
   */
  <RESULT> IFuture<RESULT> schedule(IExecutable<RESULT> executable, long delay, TimeUnit delayUnit, JobInput input);

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, the second after <code>initialDelay+period</code>
   * , the third after <code>initialDelay+period+period</code> and so on. If an execution takes longer than the
   * <code>period</code>, the subsequent execution is delayed and starts only once the current execution completed. The
   * job only terminates via cancellation or termination of the job manager.
   *
   * @param runnable
   *          the runnable to be executed periodically.
   * @param initialDelay
   *          the time to delay first run.
   * @param period
   *          the period between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @param input
   *          describes the job to be executed.
   * @return Future to cancel the periodic action.
   */
  IFuture<Void> scheduleAtFixedRate(IRunnable runnable, long initialDelay, long period, TimeUnit unit, JobInput input);

  /**
   * Periodically runs the given job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, and subsequently with the given
   * <code>delay</code> between the termination of one execution and the commencement of the next. The
   * job only terminates via cancellation or termination of the job manager.
   *
   * @param runnable
   *          the runnable to be executed periodically.
   * @param initialDelay
   *          the time to delay first run.
   * @param delay
   *          the fixed delay between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @param input
   *          describes the job to be executed.
   * @return Future to cancel the periodic action.
   */
  IFuture<Void> scheduleWithFixedDelay(IRunnable runnable, long initialDelay, long delay, TimeUnit unit, JobInput input);

  /**
   * Checks whether all Futures accepted by the given Filter are in 'done-state'.
   * <p/>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobFutureFilters} for simplified usage:<br/>
   * <code>JobFutureFilters.allFilter().notCurrentFuture().session(...);</code>
   *
   * @param filter
   *          filter to limit the Futures to be checked for their 'done-state'. If <code>null</code>, all Futures are
   *          checked, which is the same as using {@link AlwaysFilter}.
   * @return <code>true</code> if all Futures matching the given Filter are in 'done-state'.
   * @see JobFutureFilters
   * @see ServerJobFutureFilters
   * @see ClientJobFutureFilters
   */
  boolean isDone(IFilter<IFuture<?>> filter);

  /**
   * Blocks the calling thread until all Futures accepted by the given Filter are in 'done-state', or the given timeout
   * elapses.
   * <p/>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobFutureFilters} for simplified usage:<br/>
   * <code>JobFutureFilters.allFilter().notCurrentFuture().session(...);</code>
   *
   * @param filter
   *          filter to limit the Futures to await to become 'done'. If <code>null</code>, all Futures are awaited,
   *          which is the same as using {@link AlwaysFilter}.
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @return <code>false</code> if the deadline has elapsed upon return, else <code>true</code>.
   * @throws InterruptedException
   *           if the current thread is interrupted while waiting.
   * @see JobFutureFilters
   * @see ServerJobFutureFilters
   * @see ClientJobFutureFilters
   */
  boolean awaitDone(IFilter<IFuture<?>> filter, long timeout, TimeUnit unit) throws InterruptedException;

  /**
   * Visits all Futures that are accepted by the given Filter and are not in 'done-state'.
   * <p/>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobFutureFilters} for simplified usage:<br/>
   * <code>JobFutureFilters.allFilter().notCurrentFuture().session(...);</code>
   *
   * @param filter
   *          to limit the Futures to be visited. If <code>null</code>, all Futures are visited, which is the same as
   *          using {@link AlwaysFilter}.
   * @param visitor
   *          called for each Futures that passed the filter.
   * @see JobFutureFilters
   * @see ServerJobFutureFilters
   * @see ClientJobFutureFilters
   */
  void visit(IFilter<IFuture<?>> filter, IVisitor<IFuture<?>> visitor);

  /**
   * Cancels all Futures which are accepted by the given Filter.
   * <p/>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobFutureFilters} for simplified usage:<br/>
   * <code>JobFutureFilters.allFilter().notCurrentFuture().session(...);</code>
   *
   * @param filter
   *          to limit the Futures to be cancelled. If <code>null</code>, all Futures are cancelled, which is the same
   *          as using {@link AlwaysFilter}.
   * @param interruptIfRunning
   *          <code>true</code> to interrupt in-progress jobs.
   * @return <code>true</code> if all Futures matching the Filter are cancelled successfully, or <code>false</code>, if
   *         a Future could not be cancelled, typically because already completed normally.
   * @see JobFutureFilters
   * @see ServerJobFutureFilters
   * @see ClientJobFutureFilters
   */
  boolean cancel(IFilter<IFuture<?>> filter, boolean interruptIfRunning);

  /**
   * Creates a blocking condition to put a job into waiting mode until the condition falls. See
   * {@link IBlockingCondition} for more information.
   *
   * @param name
   *          the name of the blocking condition; primarily used for logging purpose.
   * @param blocking
   *          initial blocking-state of the blocking condition.
   * @return {@link IBlockingCondition}
   */
  IBlockingCondition createBlockingCondition(String name, boolean blocking);

  /**
   * Interrupts all running jobs and prevents scheduled jobs from running. After having shutdown, this job manager
   * cannot be used anymore.
   */
  void shutdown();

  /**
   * Registers the given listener to be notified about job lifecycle events. If the listener is already registered, that
   * previous registration is replaced.
   * <p/>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobFutureFilters} for simplified usage:<br/>
   * <code>JobEventFilters.allFilter().notCurrentFuture().session(...);</code>
   *
   * @param listener
   *          listener to be registered.
   * @param filter
   *          filter to only get notified about events of interest - that is for events accepted by the filter.
   * @return the given listener.
   * @see JobEventFilters
   * @see ServerJobEventFilters
   * @see ClientJobEventFilters
   */
  IJobListener addListener(IJobListener listener, IFilter<JobEvent> filter);

  /**
   * Removes the given listener from the list.
   *
   * @param listener
   *          listener to be removed.
   */
  void removeListener(IJobListener listener);
}
