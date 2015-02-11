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

import java.util.Locale;
import java.util.concurrent.Callable;

import org.eclipse.scout.commons.Assertions;
import org.eclipse.scout.commons.LocaleThreadLocal;
import org.eclipse.scout.commons.ToStringBuilder;
import org.eclipse.scout.commons.job.IAsyncFuture;
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.IProgressMonitor;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.ThreadLocalInitializer;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job that operates on a {@link IClientSession} and provides your executing code with a client-context.
 * Jobs of this type run in parallel among other jobs and are scheduled by the JVM-wide {@link JobManager}.
 * <p/>
 * <strong>If interacting with the client-model, use {@link ModelJob}.</strong>
 * <p/>
 * While running, a {@link ClientJob} has the following {@link ThreadLocal}s set:
 * <ul>
 * <li>{@link IJob#CURRENT}: to access this job</li>
 * <li>{@link JobContext#CURRENT}: to propagate properties to nested jobs</li>
 * <li>{@link ISession#CURRENT}: to access the session associated with this job</li>
 * <li>{@link LocaleThreadLocal#CURRENT}: to access the session's {@link Locale}</li>
 * <li>{@link ScoutTexts#CURRENT}: to access the session's {@link ScoutTexts}</li>
 * </ul>
 *
 * @param <R>
 *          the result type of the job's computation; use {@link Void} in combination with {@link #onRunVoid()} if this
 *          job does not return a result.
 * @see JobManager
 * @since 5.0
 */
public class ClientJob<R> extends Job<R> implements IClientSessionProvider {

  private final IClientSession m_clientSession;

  /**
   * Creates a {@link ClientJob} with the given name.
   *
   * @param name
   *          the name of the job primarily used for monitoring purpose; must not be <code>null</code>; must not be
   *          unique.
   * @param clientSession
   *          the {@link IClientSession} which this job belongs to.
   */
  public ClientJob(final String name, final IClientSession clientSession) {
    super(name);
    m_clientSession = Assertions.assertNotNull(clientSession);
  }

  @Override
  public IClientSession getClientSession() {
    return m_clientSession;
  }

  /**
   * This method can be used to intercept the concrete {@link Callable} given to the {@link JobManager} for execution.<br/>
   * The default implementation adds {@link IAsyncFuture}-support and sets the following {@link ThreadLocal}s:
   * <ul>
   * <li>{@link IJob#CURRENT}: to access this job</li>
   * <li>{@link JobContext#CURRENT}: to propagate properties to nested jobs</li>
   * <li>{@link ISession#CURRENT}: to access the session associated with this job</li>
   * <li>{@link LocaleThreadLocal#CURRENT}: to access the session's {@link Locale}</li>
   * <li>{@link ScoutTexts#CURRENT}: to access the session's {@link ScoutTexts}</li>
   * </ul>
   *
   * @param targetInvoker
   *          {@link Callable} which calls the job's {@link #onRun(IProgressMonitor)}-method.
   * @param asyncFuture
   *          {@link IAsyncFuture} to be notified about the job's completion or failure.
   * @return {@link Callable} to be given to the {@link JobManager}.
   */
  @Override
  protected Callable<R> interceptCallable(final Callable<R> targetInvoker, final IAsyncFuture<R> asyncFuture) {
    // Plugged according to design pattern: 'chain-of-responsibility'.

    final Callable<R> p4 = super.interceptCallable(targetInvoker, asyncFuture);
    final Callable<R> p3 = new ThreadLocalInitializer<>(p4, ScoutTexts.CURRENT, m_clientSession.getTexts());
    final Callable<R> p2 = new ThreadLocalInitializer<>(p3, LocaleThreadLocal.CURRENT, m_clientSession.getLocale());
    final Callable<R> head = new ThreadLocalInitializer<>(p2, ISession.CURRENT, m_clientSession);

    return head;
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
   * @return The {@link ClientJob} which is currently executed by the current thread; is <code>null</code> if the
   *         current execution context is not run on behalf of a {@link ClientJob}.
   */
  public static ClientJob<?> get() {
    final IJob<?> currentJob = CURRENT.get();
    return (ClientJob<?>) (currentJob instanceof ClientJob ? currentJob : null);
  }
}
