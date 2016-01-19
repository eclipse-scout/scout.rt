/*******************************************************************************
 * Copyright (c) 2010-2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.rt.platform.IPlatform.State;
import org.eclipse.scout.rt.platform.IPlatformListener;
import org.eclipse.scout.rt.platform.filter.AlwaysFilter;
import org.eclipse.scout.rt.platform.filter.AndFilter;
import org.eclipse.scout.rt.platform.filter.IFilter;
import org.eclipse.scout.rt.platform.filter.NotFilter;
import org.eclipse.scout.rt.platform.filter.OrFilter;
import org.eclipse.scout.rt.platform.job.filter.event.JobEventFilterBuilder;
import org.eclipse.scout.rt.platform.job.listener.IJobListener;
import org.eclipse.scout.rt.platform.job.listener.JobEvent;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.ThreadInterruptedException;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutException;
import org.eclipse.scout.rt.platform.visitor.CollectorVisitor;
import org.eclipse.scout.rt.platform.visitor.IVisitor;

/**
 * Job manager to run tasks in parallel.
 * <p>
 * A task is called a job, and consists of some work in the form of a {@link IRunnable} or {@link Callable}, and some
 * instruction information in the form of a {@link JobInput} for the job manager to run the job.
 * <p>
 * This job manager allows to control the maximal number of jobs running concurrently by assigning a job to a
 * {@link IExecutionSemaphore}. That way, jobs which are assigned to the same semaphore run concurrently until they
 * reach the concurrency level as defined for that semaphore. Subsequent tasks then wait in a queue until a permit
 * becomes available. For more information, see {@link JobInput#withExecutionSemaphore(IExecutionSemaphore)}.
 * <p>
 * As a general rule, jobs compete for an execution permit once being fired by the associated trigger, and in the order
 * as being scheduled. For example, if scheduling two jobs in a row, they very likely will have the same execution time
 * (granularity in milliseconds). However, job manager guarantees the first job to compete for an execution permit
 * before the second job does.
 * <p>
 * This job manager supports jobs to be executed some time in the future, or repeatedly based on a schedule. For more
 * information, see {@link JobInput#withExecutionTrigger(ExecutionTrigger)}.
 *
 * @since 5.1
 */
public interface IJobManager {

  /**
   * Indicates the order of the job manager's {@link IPlatformListener} to shutdown itself upon entering platform state
   * {@link State#PlatformStopping}. Any listener depending on job manager facility must be configured with an order
   * less than {@link #DESTROY_ORDER}.
   */
  long DESTROY_ORDER = 5_900;

  /**
   * Runs the given {@link IRunnable} asynchronously in another thread once the associated execution trigger fires,
   * which depends on both, the trigger's start time and schedule. However, if not set, the job will commence execution
   * immediately at the next reasonable opportunity. In either case, the submitter of the job continues to run in
   * parallel.
   * <p>
   * If the maximal concurrency level for a semaphore aware job is reached, the job is queued until a permit becomes
   * available. As a general rule, jobs compete for an execution permit once being fired by the associated trigger, and
   * in the order as being scheduled. For example, if scheduling two jobs in a row, they very likely will have the same
   * execution time (granularity in milliseconds). However, job manager guarantees the first job to compete for an
   * execution permit before the second job does.
   * <p>
   * The job manager will use the {@link JobInput} as given to control job execution.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>BEANS.get(IJobManager.class).schedule(...).awaitDone();</code>
   *
   * @param runnable
   *          <code>IRunnable</code> to be executed.
   * @param input
   *          information about the job with execution instructions for the job manager to run the job.
   * @return Future to interact with the job like waiting for its completion or to cancel its execution.
   */
  IFuture<Void> schedule(IRunnable runnable, JobInput input);

  /**
   * Runs the given {@link Callable} asynchronously in another thread once the associated execution trigger fires, which
   * depends on both, the trigger's start time and schedule. However, if not set, the job will commence execution
   * immediately at the next reasonable opportunity. In either case, the submitter of the job continues to run in
   * parallel.
   * <p>
   * Jobs in the form of a {@link Callable} typically return a computation result to the submitter.
   * <p>
   * If the maximal concurrency level for a semaphore aware job is reached, the job is queued until a permit becomes
   * available. As a general rule, jobs compete for an execution permit once being fired by the associated trigger, and
   * in the order as being scheduled. For example, if scheduling two jobs in a row, they very likely will have the same
   * execution time (granularity in milliseconds). However, job manager guarantees the first job to compete for an
   * execution permit before the second job does.
   * <p>
   * The job manager will use the {@link JobInput} as given to control job execution.
   * <p>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel its execution. To immediately
   * block waiting for the job to complete, you can use constructions of the following form.
   * <p>
   * <code>Object result = BEANS.get(IJobManager.class).schedule(...).awaitDoneAndGet();</code>
   *
   * @param callable
   *          <code>Callable</code> to be executed.
   * @param input
   *          information about the job with execution instructions for the job manager to run the job.
   * @return Future to interact with the job like waiting for its completion, or to cancel its execution, or to get its
   *         computation result.
   */
  <RESULT> IFuture<RESULT> schedule(Callable<RESULT> callable, JobInput input);

  /**
   * Checks whether all Futures accepted by the given Filter are in 'done-state' (completed or cancelled).
   * <p>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link newFutureFilterBuilder} to create a filter to match multiple
   * criteria joined by logical 'AND' operation.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newFutureFilterBuilder()
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   *
   * @param filter
   *          filter to limit the Futures to be checked for their 'done-state'. If <code>null</code>, all Futures are
   *          checked, which is the same as using {@link AlwaysFilter}.
   * @return <code>true</code> if all Futures matching the given Filter are in 'done-state'.
   */
  boolean isDone(IFilter<IFuture<?>> filter);

  /**
   * Waits if necessary for at most the given time for all futures matching the given filter to complete, or until
   * cancelled, or the timeout elapses.
   * <p>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link newFutureFilterBuilder} to create a filter to match multiple
   * criteria joined by logical 'AND' operation.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newFutureFilterBuilder()
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   *
   * @param filter
   *          filter to limit the Futures to await for. If <code>null</code>, all Futures are awaited, which is the same
   *          as using {@link AlwaysFilter}.
   * @param timeout
   *          the maximal time to wait.
   * @param unit
   *          unit of the given timeout.
   * @throws ThreadInterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws TimedOutException
   *           if the wait timed out.
   */
  void awaitDone(IFilter<IFuture<?>> filter, long timeout, TimeUnit unit);

  /**
   * Waits if necessary for at most the given time for all futures matching the given filter to finish, meaning that
   * those jobs either complete normally or by an exception, or that they will never commence execution due to a
   * premature cancellation.
   *
   * @param filter
   *          filter to limit the Futures to await for. If <code>null</code>, all Futures are awaited, which is the same
   *          as using {@link AlwaysFilter}.
   * @param timeout
   *          the maximal time to wait for the job to complete.
   * @param unit
   *          unit of the timeout.
   * @throws ThreadInterruptedException
   *           if the current thread was interrupted while waiting.
   * @throws TimedOutException
   *           if the wait timed out.
   */
  void awaitFinished(IFilter<IFuture<?>> filter, long timeout, TimeUnit unit);

  /**
   * Visits all Futures that are accepted by the given Filter and are not in 'done-state'.
   * <p>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. See {@link newFutureFilterBuilder} to create a filter to match multiple criteria
   * joined by logical 'AND' operation. Use a {@link CollectorVisitor} to collect all visited Futures.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newFutureFilterBuilder()
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   *
   * Example collector:
   *
   * <pre>
   * CollectorVisitor&lt;IFuture&lt;?>&gt; collector = new CollectorVisitor&lt;&gt;();
   * Jobs.getJobManager().visit(Jobs.newFutureFilterBuilder()
   *     .andMatchRunContext(ClientRunContext.class)
   *     .andMatch(new SessionFutureFilter(ISession.CURRENT.get()))
   *     .toFilter(), collector);
   *
   * List&lt;IFuture&lt;?&gt;&gt; futures = collector.getElements();
   * </pre>
   *
   * @param filter
   *          to limit the Futures to be visited. If <code>null</code>, all Futures are visited, which is the same as
   *          using {@link AlwaysFilter}.
   * @param visitor
   *          called for each Futures that passed the filter.
   * @see CollectorVisitor
   */
  void visit(IFilter<IFuture<?>> filter, IVisitor<IFuture<?>> visitor);

  /**
   * Cancels all Futures which are accepted by the given Filter.
   * <p>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link newFutureFilterBuilder} to create a filter to match multiple
   * criteria joined by logical 'AND' operation.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newFutureFilterBuilder()
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   *
   * @param filter
   *          to limit the Futures to be cancelled. If <code>null</code>, all Futures are cancelled, which is the same
   *          as using {@link AlwaysFilter}.
   * @param interruptIfRunning
   *          <code>true</code> to interrupt in-progress tasks.
   * @return <code>true</code> if all Futures matching the Filter are cancelled successfully, or <code>false</code>, if
   *         a Future could not be cancelled, typically because already completed normally.
   */
  boolean cancel(IFilter<IFuture<?>> filter, boolean interruptIfRunning);

  /**
   * Creates a blocking condition to put a job into waiting mode until the condition falls.
   * <p>
   * If the job is assigned to an {@link IExecutionSemaphore}, the job's permit is released and passed to the next
   * competing job of that same semaphore while being blocked.
   * <p>
   * See {@link IBlockingCondition} for more information.
   *
   * @param blocking
   *          initial blocking-state of the blocking condition.
   */
  IBlockingCondition newBlockingCondition(boolean blocking);

  /**
   * Registers the given listener to be notified about all job lifecycle events. If the listener is already registered,
   * that previous registration is replaced.
   *
   * @param listener
   *          listener to be registered.
   * @return A token representing the registration of the given {@link IJobListener}. This token can later be used to
   *         unregister the listener.
   */
  IJobListenerRegistration addListener(IJobListener listener);

  /**
   * Registers the given listener to be notified about job lifecycle events that comply with the given filter. If the
   * listener is already registered, that previous registration is replaced.
   * <p>
   * Filters can be plugged by using logical filters like {@link AndFilter} or {@link OrFilter}, or negated by enclosing
   * a filter in {@link NotFilter}. Also see {@link JobEventFilterBuilder} to create a filter to match multiple criteria
   * joined by logical 'AND' operation.
   * <p>
   * Example:
   *
   * <pre>
   * Jobs.newEventFilterBuilder()
   *     .andMatchEventType(JobEventType.SCHEDULED, JobEventType.DONE)
   *     .andMatchFuture(...)
   *     .andMatch(...)
   *     .toFilter();
   * </pre>
   *
   * @param filter
   *          filter to only get notified about events of interest - that is for events accepted by the filter.
   * @param listener
   *          listener to be registered.
   * @return A token representing the registration of the given {@link IJobListener}. This token can later be used to
   *         unregister the listener.
   */
  IJobListenerRegistration addListener(IFilter<JobEvent> filter, IJobListener listener);

  /**
   * Returns <code>true</code> if this job manager is shutdown, or else <code>false</code>.
   */
  boolean isShutdown();

  /**
   * Interrupts all running tasks and prevents scheduled tasks from running. After having shutdown, this job manager
   * cannot be used anymore.
   */
  void shutdown();
}
