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
package org.eclipse.scout.rt.client.job;

import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureNotifier;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.ThreadLocalInitializer;
import org.eclipse.scout.commons.job.interceptor.ThreadNameDecorator;
import org.eclipse.scout.commons.job.internal.Future;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job to interact with the client model that operates on a {@link IClientSession} and provides your executing code with
 * the client-context.
 * Jobs of this type run on behalf of a session-dedicated {@link ModelJobManager}, meaning that each
 * {@link IClientSession} has its own {@link ModelJobManager}.
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in sequence among other {@link ModelJob}s (mutual exclusion);</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to ThreadLocals: {@link IJob#CURRENT}, {@link JobContext#CURRENT},
 * {@link ISession#CURRENT}, {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 * <p/>
 * Within the same {@link ModelJobManager}, jobs are executed in sequence so that no more than one job will be active at
 * any given time. If a {@link ModelJob} gets blocked by entering a {@link IBlockingCondition}, the model-mutex will be
 * released which allows another model-job to run. When being unblocked, the job must compete for the model-mutex anew
 * in order to continue its execution.
 *
 * @param <R>
 *          the result type of the job's computation; use {@link Void} in combination with {@link #onRunVoid()} if this
 *          job does not return a result.
 * @see IJob
 * @see ModelJob
 * @since 5.1
 */
public class ModelJob<R> implements IJob<R>, IClientSessionProvider {

  protected final ModelJobManager m_jobManager;
  protected final String m_name;
  protected final IClientSession m_clientSession;

  public ModelJob(final String name, final IClientSession clientSession) {
    m_name = Assertions.assertNotNullOrEmpty(name);
    m_clientSession = Assertions.assertNotNull(clientSession);
    m_jobManager = Assertions.assertNotNull(createJobManager(clientSession));
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
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  /**
   * Runs this job synchronously on behalf of the current model-thread. This call blocks the caller as long as this job
   * is running.
   * <p/>
   * <strong>The calling thread must be the model-thread himself.</strong>
   *
   * @param job
   *          the {@link ModelJob} to be run.
   * @param callable
   *          the {@link Callable} to be executed.
   * @return the computed result.
   * @throws ProcessingException
   *           if the job throws an exception during execution.
   * @throws JobExecutionException
   *           if the job is already running or not called on behalf of the model-thread.
   */
  @Override
  public final R runNow() throws ProcessingException, JobExecutionException {
    return m_jobManager.runNow(this, createCallable(null));
  }

  /**
   * Runs this job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of this
   * method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the job manager the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the job manager.
   * <p/>
   * <strong>Do not wait for this job to complete if being a model-job yourself as this would cause a deadlock.</strong>
   *
   * @return {@link IFuture} to wait for the job's completion or to cancel the job execution.
   * @throws JobExecutionException
   *           if the job is already running.
   * @see #schedule(IAsyncFuture)
   */
  @Override
  public final IFuture<R> schedule() throws JobExecutionException {
    final Callable<R> callable = createCallable(null);
    return interceptFuture(m_jobManager.schedule(this, callable));
  }

  /**
   * Runs this job asynchronously on behalf of the model-thread at the next reasonable opportunity. The caller of this
   * method continues to run in parallel.
   * <p/>
   * If the given job is rejected by the job manager the time being scheduled, the job is <code>cancelled</code>. This
   * occurs if no more threads or queue slots are available, or upon shutdown of the job manager.
   * <p/>
   * The given {@link IAsyncFuture} is called once the job completes successfully or terminates with an exception. The
   * {@link IFuture} returned allows to cancel the execution of this job or to also wait for the job to complete.
   *
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure; is notified from from within
   *          the model-thread that executed the job; is not called if the job never started running.
   * @throws JobExecutionException
   *           if the job is already running.
   * @see #schedule()
   */
  @Override
  public final IFuture<R> schedule(final IAsyncFuture<R> asyncFuture) throws JobExecutionException {
    final Callable<R> callable = createCallable(asyncFuture);
    return interceptFuture(m_jobManager.schedule(this, callable));
  }

  /**
   * @return <code>true</code> if this job is blocked because waiting for a {@link IBlockingCondition} to fall.
   */
  public final boolean isBlocked() {
    return m_jobManager.isBlocked(this);
  }

  /**
   * This method is invoked by the {@link ModelJobManager} to run this model job.<br/>
   * Overwrite this method if your job returns a result to the caller.
   *
   * @param monitor
   *          {@link IProgressMonitor} to track the progress of this job.
   * @return the result of the job's computation.
   * @throws Exception
   *           if you encounter a problem that should be propagated to the caller; exceptions other than
   *           {@link ProcessingException} are wrapped into a {@link ProcessingException}.
   */
  protected R onRun(final IProgressMonitor monitor) throws Exception {
    onRunVoid(monitor);
    return null;
  }

  /**
   * This method is invoked by the {@link ModelJobManager} to run this model job.<br/>
   * Overwrite this method if you declared the job's return type as {@link Void} to not return a computation result to
   * the caller.
   *
   * @param monitor
   *          {@link IProgressMonitor} to track the progress of this job.
   * @throws Exception
   *           if you encounter a problem that should be propagated to the caller; exceptions other than
   *           {@link ProcessingException} are wrapped into a {@link ProcessingException}.
   */
  protected void onRunVoid(final IProgressMonitor monitor) throws Exception {
  }

  /**
   * Creates the {@link Callable} to be given to the {@link ModelJobManager} for execution.
   * <p/>
   * The default implementation invokes {@link #interceptCallable(Callable)} and installs the following functionality on
   * top of the contributions:
   * <ol>
   * <li>Notifies the {@link IAsyncFuture} about the computations result;</li>
   * <li>Translates computing exception into {@link ProcessingException};</li>
   * <li>Invokes the job's {@link #onRun(IProgressMonitor)}-method;</li>
   * </ol>
   *
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified once the job completes.
   * @return {@link Callable} to be given to the {@link JobManager} (head of the chain).
   */
  protected Callable<R> createCallable(final IAsyncFuture<R> asyncFuture) {
    final Callable<R> tail = createTargetInvoker();
    final Callable<R> p3 = new ExceptionTranslator<>(tail);
    final Callable<R> p2 = new AsyncFutureNotifier<R>(p3, asyncFuture);
    final Callable<R> p1 = interceptCallable(p2);

    return p1;
  }

  /**
   * Overwrite this method to contribute some behavior to the {@link Callable} given to the {@link ModelJobManager} for
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
   *   Callable p2 = new YourInterceptor2(<strong>next</strong>); // executed 3th<br/>
   *   Callable p1 = new YourInterceptor1(p2); // executed 2nd<br/>
   *   Callable head = <i>super.interceptCallable(p1)</i>; // executed 1st<br/>
   *   return head;
   * </code>
   * </p>
   * To be invoked <strong>before</strong> the super classes contributions, you can use constructions of the following
   * form:
   * <p/>
   * <code>
   *   Callable p2 = <i>super.interceptCallable(<strong>next</strong>)</i>; // executed 3th<br/>
   *   Callable p1 = new YourInterceptor2(p2); // executed 2nd<br/>
   *   Callable head = new YourInterceptor1(p1); // executed 1st<br/>
   *   return head;
   * </code>
   *
   * @param next
   *          subsequent chain-element; typically notifies the {@link IAsyncFuture}-callback and invokes the job's
   *          {@link #onRun(IProgressMonitor)}-method.
   * @return the head of the chain to be invoked first.
   */
  protected Callable<R> interceptCallable(final Callable<R> next) {
    final Callable<R> p6 = new ThreadLocalInitializer<>(next, ScoutTexts.CURRENT, m_clientSession.getTexts());
    final Callable<R> p5 = new ThreadLocalInitializer<>(p6, NlsLocale.CURRENT, m_clientSession.getLocale());
    final Callable<R> p4 = new ThreadLocalInitializer<>(p5, ISession.CURRENT, m_clientSession);
    final Callable<R> p3 = new ThreadLocalInitializer<>(p4, JobContext.CURRENT, JobContext.copy(JobContext.CURRENT.get()));
    final Callable<R> p2 = new ThreadLocalInitializer<>(p3, IJob.CURRENT, this);
    final Callable<R> p1 = new ThreadNameDecorator<R>(p2, m_name);

    return p1;
  }

  /**
   * This method can be used to intercept the concrete {@link IFuture} returned to the caller.
   * The default implementation simply returns the given future as {@link IFuture}.
   *
   * @param future
   *          {@link java.util.concurrent.Future} returned by the {@link ModelJobManager}.
   * @return {@link IFuture} that encapsulates the {@link ModelJobManager}'s future and translates exceptions into
   *         {@link ProcessingException}s.
   */
  protected Future<R> interceptFuture(final java.util.concurrent.Future<R> future) {
    return new Future<R>(future, getName());
  }

  /**
   * Method is invoked during initialization to bind jobs to a {@link ModelJobManager}.
   *
   * @return {@link ModelJobManager}; must not be <code>null</code>.
   */
  protected ModelJobManager createJobManager(final IClientSession clientSession) {
    return clientSession.getModelJobManager();
  }

  /**
   * Method is invoked during initialization to propagate control to {@link ModelJob#onRun(IProgressMonitor)} once a job
   * starts running.
   *
   * @return {@link Callable}; must not be <code>null</code>.
   */
  protected Callable<R> createTargetInvoker() {
    return new Callable<R>() {

      @Override
      public R call() throws Exception {
        final ModelJob<R> job = ModelJob.this;
        return job.onRun(m_jobManager.createProgressMonitor(job));
      }
    };
  }

  @Override
  public String toString() {
    final ToStringBuilder builder = new ToStringBuilder(this);
    builder.attr("jobName", getName());
    builder.ref("jobManager", m_jobManager);
    builder.ref("clientSession", m_clientSession);
    return builder.toString();
  }

  /**
   * @return The {@link ModelJob} which is currently executed by the current model-thread; is <code>null</code> if the
   *         current execution context is not run on behalf of a {@link ModelJob}.
   */
  public static ModelJob<?> get() {
    final IJob<?> currentJob = CURRENT.get();
    return (ModelJob<?>) (currentJob instanceof ModelJob ? currentJob : null);
  }
}
