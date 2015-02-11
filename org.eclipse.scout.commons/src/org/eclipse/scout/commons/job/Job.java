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

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureNotifier;
import org.eclipse.scout.commons.job.interceptor.ThreadLocalInitializer;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.eclipse.scout.commons.job.internal.Future;

/**
 * Default implementation of {@link IJob} to run in parallel among other jobs on behalf of the JVM-wide
 * {@link JobManager}.
 * <p/>
 * While running, jobs of this type have the following {@link ThreadLocal}s set:
 * <ul>
 * <li>{@link IJob#CURRENT}: to access this job</li>
 * <li>{@link JobContext#CURRENT}: to propagate properties to nested jobs</li>
 * </ul>
 *
 * @param <R>
 *          the result type of the job's computation; use {@link Void} in combination with {@link #onRunVoid()} if this
 *          job does not return a result.
 * @see JobManager
 * @since 5.0
 */
public class Job<R> implements IJob<R> {

  protected final JobManager m_jobManager;
  protected final String m_name;
  protected Callable<R> m_targetInvoker;

  /**
   * Creates a {@link Job} with the given name.
   *
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>; must not be
   *          unique.
   */
  public Job(final String name) {
    m_name = Assertions.assertNotNullOrEmpty(name);
    m_jobManager = Assertions.assertNotNull(createJobManager());
    m_targetInvoker = Assertions.assertNotNull(createTargetInvoker());
  }

  @Override
  public String getName() {
    return m_name;
  }

  @Override
  public boolean cancel(final boolean interruptIfRunning) {
    return m_jobManager.cancel(this, interruptIfRunning);
  }

  @Override
  public R runNow() throws ProcessingException {
    return m_jobManager.runNow(this, interceptCallable(m_targetInvoker, null));
  }

  @Override
  public IFuture<R> schedule() throws JobExecutionException {
    final Callable<R> callable = interceptCallable(m_targetInvoker, null);
    return interceptFuture(m_jobManager.schedule(this, callable));
  }

  @Override
  public IFuture<R> schedule(final IAsyncFuture<R> asyncFuture) throws JobExecutionException {
    final Callable<R> callable = interceptCallable(m_targetInvoker, asyncFuture);
    return interceptFuture(m_jobManager.schedule(this, callable));
  }

  /**
   * Runs this job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of this
   * method continues to run in parallel.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of this job. To
   * immediately block waiting for a task to complete, you can use constructions of the form
   * <code>result = job.schedule().get();</code>.
   *
   * @param delay
   *          the delay after which this job is to be run.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if this job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   */
  public IFuture<R> schedule(final long delay, final TimeUnit delayUnit) throws JobExecutionException {
    final Callable<R> callable = interceptCallable(m_targetInvoker, null);
    return interceptFuture(m_jobManager.schedule(this, callable, delay, delayUnit));
  }

  /**
   * Runs this job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of this
   * method continues to run in parallel.
   * <p/>
   * The given {@link IAsyncFuture} is called once the job completes successfully or terminates with an exception.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of this job. To
   * immediately block waiting for a task to complete, you can use constructions of the form
   * <code>result = job.schedule().get();</code>.
   *
   * @param delay
   *          the delay after which this job is to be run.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure; is notified from from within
   *          the worker-thread that executed the job; is not called if the job never started running.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if this job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   */
  public IFuture<R> schedule(final long delay, final TimeUnit delayUnit, final IAsyncFuture<R> asyncFuture) throws JobExecutionException {
    final Callable<R> callable = interceptCallable(m_targetInvoker, asyncFuture);
    return interceptFuture(m_jobManager.schedule(this, callable, delay, delayUnit));
  }

  /**
   * Periodically runs this job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, the second after <code>initialDelay+delay</code>,
   * the third after <code>initialDelay+delay+delay</code> and so on. If an execution takes longer than the
   * <code>period</code>, the subsequent execution is delayed and starts only once the current execution completed. So
   * you have kind of mutual exclusion meaning that at any given time, there is only one job running.<br/>
   * If any execution throws an exception, subsequent executions are suppressed. Otherwise, the task will only terminate
   * via cancellation or termination of the {@link JobManager}.
   *
   * @param initialDelay
   *          the time to delay first run.
   * @param period
   *          the period between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @return {@link IFuture} to cancel this periodic action.
   * @throws JobExecutionException
   *           if this job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   */
  public IFuture<R> scheduleAtFixedRate(final long initialDelay, final long period, final TimeUnit unit) throws JobExecutionException {
    final Callable<R> callable = interceptCallable(m_targetInvoker, null);
    return interceptFuture(m_jobManager.scheduleAtFixedRate(this, callable, initialDelay, period, unit));
  }

  /**
   * Periodically runs this job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, and subsequently with the given
   * <code>delay</code> between the termination of one execution and the commencement of the next. So you have kind of
   * mutual exclusion meaning that at any given time, there is only one job running.<br/>
   * If any execution throws an exception, subsequent executions are suppressed. Otherwise, the task will only terminate
   * via cancellation or termination of the {@link JobManager}.
   *
   * @param initialDelay
   *          the time to delay first run.
   * @param delay
   *          the fixed delay between successive runs.
   * @param unit
   *          the time unit of the <code>initialDelay</code> and <code>period</code> arguments.
   * @return {@link IFuture} to cancel this periodic action.
   * @throws JobExecutionException
   *           if this job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   */
  public IFuture<R> scheduleWithFixedDelay(final long initialDelay, final long delay, final TimeUnit unit) throws JobExecutionException {
    final Callable<R> callable = interceptCallable(m_targetInvoker, null);
    return interceptFuture(m_jobManager.scheduleWithFixedDelay(this, callable, initialDelay, delay, unit));
  }

  /**
   * This method is invoked by the {@link JobManager} to run this job.<br/>
   * Overwrite this method if your job returns a result to the caller.
   *
   * @return the result of the job's computation.
   * @param monitor
   *          {@link IProgressMonitor} to track the progress of this job.
   * @throws ProcessingException
   *           throw a {@link ProcessingException} if you encounter a problem that should be propagated to the caller.
   * @throws RuntimeException
   *           {@link RuntimeException}s are wrapped into a {@link ProcessingException} and propagated to the caller.
   */
  protected R onRun(final IProgressMonitor monitor) throws ProcessingException {
    onRunVoid(monitor);
    return null;
  }

  /**
   * This method is invoked by the {@link JobManager} to run this job.<br/>
   * Overwrite this method if you declared the job's return type as {@link Void} to not return a computation result to
   * the caller.
   *
   * @param monitor
   *          {@link IProgressMonitor} to track the progress of this job.
   * @throws ProcessingException
   *           throw a {@link ProcessingException} if you encounter a problem that should be propagated to the caller.
   * @throws RuntimeException
   *           {@link RuntimeException}s are wrapped into a {@link ProcessingException} and propagated to the caller.
   */
  protected void onRunVoid(final IProgressMonitor monitor) throws ProcessingException {
  }

  /**
   * This method can be used to intercept the concrete {@link Callable} given to the {@link JobManager} for execution.<br/>
   * The default implementation adds {@link IAsyncFuture}-support and sets the following {@link ThreadLocal}s:
   * <ul>
   * <li>{@link IJob#CURRENT}: to access this job</li>
   * <li>{@link JobContext#CURRENT}: to propagate properties to nested jobs</li>
   * </ul>
   *
   * @param targetInvoker
   *          {@link Callable} which calls the job's {@link #onRun(IProgressMonitor)}-method.
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure.
   * @return {@link Callable} to be given to the {@link JobManager}.
   */
  protected Callable<R> interceptCallable(final Callable<R> targetInvoker, final IAsyncFuture<R> asyncFuture) {
    // Plugged according to design pattern: 'chain-of-responsibility'.

    final Callable<R> p5 = targetInvoker;
    final Callable<R> p4 = new AsyncFutureNotifier<R>(p5, asyncFuture);
    final Callable<R> p3 = new ThreadLocalInitializer<>(p4, JobContext.CURRENT, (JobContext.CURRENT.get() == null ? new JobContext() : JobContext.copy(JobContext.CURRENT.get())));
    final Callable<R> p2 = new ThreadLocalInitializer<>(p3, IJob.CURRENT, this);
    final Callable<R> head = new ThreadNameDecorator<R>(p2, m_name);

    return head;
  }

  /**
   * This method can be used to intercept the concrete {@link IFuture} returned to the caller.
   * The default implementation simply returns the given future as {@link IFuture}.
   *
   * @param future
   *          {@link java.util.concurrent.Future} returned by the {@link JobManager}.
   * @return {@link IFuture} that encapsulates the {@link JobManager}'s future and translates exceptions into
   *         {@link JobExecutionException}s.
   */
  protected IFuture<R> interceptFuture(final java.util.concurrent.Future<R> future) {
    return new Future<R>(future, m_name);
  }

  /**
   * Method is invoked during initialization to bind jobs to a {@link JobManager}.
   *
   * @return {@link JobManager}; must not be <code>null</code>.
   */
  protected JobManager createJobManager() {
    return JobManager.INSTANCE;
  }

  /**
   * Method is invoked during initialization to propagate control to {@link Job#onRun(IProgressMonitor)} once a job
   * starts running.
   *
   * @return {@link Callable}; must not be <code>null</code>.
   */
  protected Callable<R> createTargetInvoker() {
    return new Callable<R>() {

      @Override
      public R call() throws Exception {
        final Job<R> job = Job.this;
        return job.onRun(m_jobManager.createProgressMonitor(job));
      }
    };
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("jobName", getName());
    builder.ref("jobManager", m_jobManager);
    return builder.toString();
  }

  /**
   * @return The {@link Job} which is currently executed by the current thread; is <code>null</code> if the
   *         current execution context is not run on behalf of a {@link Job}.
   */
  public static Job<?> get() {
    final IJob<?> currentJob = CURRENT.get();
    return (Job<?>) (currentJob instanceof Job ? currentJob : null);
  }
}
