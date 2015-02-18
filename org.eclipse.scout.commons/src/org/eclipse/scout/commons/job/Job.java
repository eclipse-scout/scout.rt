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
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureCallable;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.InitThreadLocalCallable;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.eclipse.scout.commons.job.internal.Future;

/**
 * Default implementation of {@link IJob} to run on behalf of the JVM-wide {@link IJobManager}.
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in parallel among other {@link Job}s;</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to ThreadLocals: {@link IJob#CURRENT}, {@link JobContext#CURRENT};</li>
 * </ul>
 *
 * @param <RESULT>
 *          the result type of the job's computation; use {@link Void} if this job does not return a result.
 * @see IJob
 * @see IJobManager
 * @since 5.1
 */
public abstract class Job<RESULT> implements IJob<RESULT> {

  @Internal
  protected final IJobManager m_jobManager;
  @Internal
  protected final String m_name;
  @Internal
  protected final JobContext m_jobContext;

  /**
   * Creates a {@link Job} with the given name.
   *
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   */
  public Job(final String name) {
    m_name = Assertions.assertNotNullOrEmpty(name);
    m_jobManager = Assertions.assertNotNull(createJobManager());
    m_jobContext = JobContext.copy(JobContext.CURRENT.get());
  }

  @Override
  public final String getName() {
    return m_name;
  }

  @Override
  public final boolean cancel(final boolean interruptIfRunning) {
    return m_jobManager.cancel(this, interruptIfRunning);
  }

  @Override
  public final RESULT runNow() throws ProcessingException {
    return m_jobManager.runNow(this, createCallable(null));
  }

  @Override
  public final IFuture<RESULT> schedule() throws JobExecutionException {
    final Callable<RESULT> callable = createCallable(null);
    return interceptFuture(m_jobManager.schedule(this, callable));
  }

  @Override
  public final IFuture<RESULT> schedule(final IAsyncFuture<RESULT> asyncFuture) throws JobExecutionException {
    final Callable<RESULT> callable = createCallable(asyncFuture);
    return interceptFuture(m_jobManager.schedule(this, callable));
  }

  /**
   * Runs this job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of this
   * method continues to run in parallel.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of this job. To
   * immediately block waiting for a job to complete, you can use constructions of the form
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
  public final IFuture<RESULT> schedule(final long delay, final TimeUnit delayUnit) throws JobExecutionException {
    final Callable<RESULT> callable = createCallable(null);
    return interceptFuture(m_jobManager.schedule(this, callable, delay, delayUnit));
  }

  /**
   * Runs this job asynchronously on behalf of a worker thread after the specified delay has elapsed. The caller of this
   * method continues to run in parallel.
   * <p/>
   * The given {@link IAsyncFuture} is called once the job completes successfully or terminates with an exception.
   * <p/>
   * The {@link IFuture} returned allows to wait for the job to complete or to cancel the execution of this job. To
   * immediately block waiting for a job to complete, you can use constructions of the form
   * <code>result = job.schedule().get();</code>.
   *
   * @param delay
   *          the delay after which this job is to be run.
   * @param delayUnit
   *          the time unit of the <code>delay</code> argument.
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure; is notified from within the
   *          worker-thread that executed the job; is not called if the job never started running.
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if this job is already running or was rejected by the job manager because no more threads or queue slots
   *           are available, or upon shutdown of the job manager.
   */
  public final IFuture<RESULT> schedule(final long delay, final TimeUnit delayUnit, final IAsyncFuture<RESULT> asyncFuture) throws JobExecutionException {
    final Callable<RESULT> callable = createCallable(asyncFuture);
    return interceptFuture(m_jobManager.schedule(this, callable, delay, delayUnit));
  }

  /**
   * Periodically runs this job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, the second after <code>initialDelay+period</code>
   * , the third after <code>initialDelay+period+period</code> and so on. If an execution takes longer than the
   * <code>period</code>, the subsequent execution is delayed and starts only once the current execution completed. So
   * you have kind of mutual exclusion meaning that at any given time, there is only one job running.<br/>
   * If any execution throws an exception, subsequent executions are suppressed. Otherwise, the job only terminates via
   * cancellation or termination of the {@link IJobManager}.
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
  public final IFuture<RESULT> scheduleAtFixedRate(final long initialDelay, final long period, final TimeUnit unit) throws JobExecutionException {
    final Callable<RESULT> callable = createCallable(null);
    return interceptFuture(m_jobManager.scheduleAtFixedRate(this, callable, initialDelay, period, unit));
  }

  /**
   * Periodically runs this job on behalf of a worker thread.<br/>
   * The first execution is after the given <code>initialDelay</code>, and subsequently with the given
   * <code>delay</code> between the termination of one execution and the commencement of the next. So you have kind of
   * mutual exclusion meaning that at any given time, there is only one job running.<br/>
   * If any execution throws an exception, subsequent executions are suppressed. Otherwise, the job will only terminate
   * via cancellation or termination of the {@link IJobManager}.
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
  public final IFuture<RESULT> scheduleWithFixedDelay(final long initialDelay, final long delay, final TimeUnit unit) throws JobExecutionException {
    final Callable<RESULT> callable = createCallable(null);
    return interceptFuture(m_jobManager.scheduleWithFixedDelay(this, callable, initialDelay, delay, unit));
  }

  /**
   * Creates the {@link Callable} to be given to the {@link IJobManager} for execution.
   * <p/>
   * The default implementation installs the following functionality:
   * <ol>
   * <li>Invokes the job's {@link #call()}-method;</li>
   * <li>Translates computing exception into {@link ProcessingException};</li>
   * <li>Notifies the {@link IAsyncFuture} about the computations result;</li>
   * <li><i>Invokes contributions by {@link #interceptCallable(Callable)};</i></li>
   * <li>Sets the {@link ThreadLocal} for {@link IProgressMonitor};</li>
   * <li>Sets the {@link ThreadLocal} for {@link IJob};</li>
   * </ol>
   *
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified once the job completes.
   * @return {@link Callable} to be given to the {@link IJobManager}
   */
  @Internal
  protected Callable<RESULT> createCallable(final IAsyncFuture<RESULT> asyncFuture) {
    final Callable<RESULT> cTail = createCallInvoker();
    final Callable<RESULT> c5 = new ExceptionTranslator<>(cTail);
    final Callable<RESULT> c4 = new AsyncFutureCallable<RESULT>(c5, asyncFuture);

    final Callable<RESULT> c3 = interceptCallable(c4);

    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, IProgressMonitor.CURRENT, m_jobManager.createProgressMonitor(this));
    final Callable<RESULT> cHead = new InitThreadLocalCallable<>(c2, IJob.CURRENT, this);

    return cHead;
  }

  /**
   * Overwrite this method to contribute some behavior to the {@link Callable} given to the {@link IJobManager} for
   * execution.
   * <p/>
   * Contributions are plugged according to the design pattern: 'chain-of-responsibility' - it is easiest to read the
   * chain from 'bottom-to-top'.
   * <p/>
   * To contribute on top of the chain (meaning that you are invoked <strong>after</strong> the contributions of super
   * classes and therefore can base on their contributed functionality), you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Callable c1 = new YourInterceptor1(c2); // executed 2nd<br/>
   *   Callable head = <i>super.interceptCallable(c1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super class contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable c2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Callable c1 = new YourInterceptor2(c2); // executed 2nd<br/>
   *   Callable head = new YourInterceptor1(c1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent chain-element; typically notifies the {@link IAsyncFuture}-callback and invokes the job's
   *          {@link #call()}-method.
   * @return the head of the chain to be invoked first.
   */
  protected Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(next, JobContext.CURRENT, m_jobContext);
    final Callable<RESULT> c1 = new ThreadNameDecorator<RESULT>(c2, m_name);

    return c1;
  }

  /**
   * This method can be used to intercept the concrete {@link IFuture} returned to the caller.
   * The default implementation simply returns the given future as {@link IFuture}.
   *
   * @param future
   *          {@link java.util.concurrent.Future} returned by the {@link IJobManager}.
   * @return {@link IFuture} that encapsulates the {@link IJobManager}'s future and translates exceptions into
   *         {@link JobExecutionException}s.
   */
  protected IFuture<RESULT> interceptFuture(final java.util.concurrent.Future<RESULT> future) {
    return new Future<RESULT>(future, m_name);
  }

  /**
   * Method is invoked during initialization to bind jobs to a {@link IJobManager}.
   *
   * @return {@link IJobManager}; must not be <code>null</code>.
   */
  @Internal
  protected IJobManager createJobManager() {
    return JobManager.INSTANCE;
  }

  /**
   * Method is invoked to create a {@link Callable} to propagate control to {@link Job#call()} once a
   * job starts running.
   *
   * @return {@link Callable}; must not be <code>null</code>.
   */
  @Internal
  protected Callable<RESULT> createCallInvoker() {
    return new Callable<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return Job.this.call();
      }
    };
  }

  /**
   * This method is invoked by the {@link IJobManager} to run this job.
   *
   * @return the result of the job's computation.
   * @throws Exception
   *           if you encounter a problem that should be propagated to the caller; exceptions other than
   *           {@link ProcessingException} are wrapped into a {@link ProcessingException}.
   */
  protected abstract RESULT call() throws Exception;

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("jobName", getName());
    builder.ref("jobManager", m_jobManager);
    return builder.toString();
  }
}
