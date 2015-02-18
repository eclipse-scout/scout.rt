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
import org.eclipse.scout.commons.annotations.Internal;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IFuture;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobExecutionException;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.AsyncFutureCallable;
import org.eclipse.scout.commons.job.interceptor.ExceptionTranslator;
import org.eclipse.scout.commons.job.interceptor.InitThreadLocalCallable;
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
 * Jobs of this type return a result to the caller and run on behalf of a dedicated {@link IModelJobManager}, meaning
 * that each session has its own {@link IModelJobManager}.
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in sequence among other model jobs (mutual exclusion);</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to {@link ThreadLocal ThreadLocals}:<br/>
 * {@link IJob#CURRENT}, {@link IProgressMonitor#CURRENT}, {@link JobContext#CURRENT}, {@link ISession#CURRENT},
 * {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 * <p/>
 * Within the same {@link IModelJobManager}, jobs are executed in sequence so that no more than one job will be active
 * at any given time. If a job gets blocked by entering a {@link IBlockingCondition}, the model-mutex will be released
 * which allows another model-job to run. When being unblocked, the job must compete for the model-mutex anew in order
 * to continue its execution.
 *
 * @param <RESULT>
 *          the result type of the job's computation.
 * @see ModelJob
 * @see IModelJobManager
 * @since 5.1
 */
public abstract class ModelJobWithResult<RESULT> implements IModelJob<RESULT>, IClientSessionProvider {

  @Internal
  protected final IModelJobManager m_jobManager;
  @Internal
  protected final String m_name;
  @Internal
  protected final JobContext m_jobContext;
  @Internal
  protected final IClientSession m_clientSession;

  /**
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>.
   * @param clientSession
   *          the {@link IServerSession} which this job belongs to; must not be <code>null</code>.
   */
  public ModelJobWithResult(final String name, final IClientSession clientSession) {
    m_name = Assertions.assertNotNullOrEmpty(name);
    m_jobContext = JobContext.copy(JobContext.CURRENT.get());
    m_clientSession = Assertions.assertNotNull(clientSession);
    m_jobManager = Assertions.assertNotNull(createJobManager(clientSession));
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
  public final IClientSession getClientSession() {
    return m_clientSession;
  }

  @Override
  public final RESULT runNow() throws ProcessingException, JobExecutionException {
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

  @Override
  public final boolean isBlocked() {
    return m_jobManager.isBlocked(this);
  }

  /**
   * Creates the {@link Callable} to be given to the {@link JobManager} for execution.
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
   * @return {@link Callable} to be given to the {@link JobManager}
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
   * Overwrite this method to contribute some behavior to the {@link Callable} given to the {@link IModelJobManager} for
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
   * To be invoked <strong>before</strong> the super classes contributions, you can use constructions of the following
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
   *          {@link #onRun(IProgressMonitor)}-method.
   * @return the head of the chain to be invoked first.
   */
  protected Callable<RESULT> interceptCallable(final Callable<RESULT> next) {
    final Callable<RESULT> c5 = new InitThreadLocalCallable<>(next, ScoutTexts.CURRENT, m_clientSession.getTexts());
    final Callable<RESULT> c4 = new InitThreadLocalCallable<>(c5, NlsLocale.CURRENT, m_clientSession.getLocale());
    final Callable<RESULT> c3 = new InitThreadLocalCallable<>(c4, ISession.CURRENT, m_clientSession);
    final Callable<RESULT> c2 = new InitThreadLocalCallable<>(c3, JobContext.CURRENT, m_jobContext);
    final Callable<RESULT> c1 = new ThreadNameDecorator<RESULT>(c2, m_name);

    return c1;
  }

  /**
   * This method can be used to intercept the concrete {@link IFuture} returned to the caller.
   * The default implementation simply returns the given future as {@link IFuture}.
   *
   * @param future
   *          {@link java.util.concurrent.Future} returned by the {@link IModelJobManager}.
   * @return {@link IFuture} that encapsulates the {@link IModelJobManager}'s future and translates exceptions into
   *         {@link ProcessingException}s.
   */
  protected Future<RESULT> interceptFuture(final java.util.concurrent.Future<RESULT> future) {
    return new Future<RESULT>(future, getName());
  }

  /**
   * Method is invoked during initialization to bind jobs to a {@link IModelJobManager}.
   *
   * @return {@link IModelJobManager}; must not be <code>null</code>.
   */
  @Internal
  protected IModelJobManager createJobManager(final IClientSession clientSession) {
    return clientSession.getModelJobManager();
  }

  /**
   * Method is invoked to create a {@link Callable} to propagate control to {@link ModelJobWithResult#call()} once a
   * job starts running.
   *
   * @return {@link Callable}; must not be <code>null</code>.
   */
  @Internal
  protected Callable<RESULT> createCallInvoker() {
    return new Callable<RESULT>() {

      @Override
      public RESULT call() throws Exception {
        return ModelJobWithResult.this.call();
      }
    };
  }

  /**
   * This method is invoked by the {@link IModelJobManager} to run this job.
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
    builder.ref("clientSession", m_clientSession);
    return builder.toString();
  }
}
