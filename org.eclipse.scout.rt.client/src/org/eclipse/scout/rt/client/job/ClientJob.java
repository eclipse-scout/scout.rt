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
import org.eclipse.scout.commons.job.IJob;
import org.eclipse.scout.commons.job.Job;
import org.eclipse.scout.commons.job.JobContext;
import org.eclipse.scout.commons.job.JobManager;
import org.eclipse.scout.commons.job.interceptor.ThreadLocalInitializer;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.IClientSessionProvider;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.shared.ScoutTexts;

/**
 * Job that operates on a {@link IClientSession} and provides your executing code with the client-context.
 * Jobs of this type run on behalf of the the JVM-wide {@link JobManager}.
 * <p/>
 * <strong>If interacting with the client-model, use {@link ModelJob}.</strong>
 * <p/>
 * While running, jobs of this type have the following characteristics:
 * <ul>
 * <li>run in parallel among other {@link ClientJob}s;</li>
 * <li>operate on named worker threads;</li>
 * <li>have a {@link JobContext} installed to propagate properties among nested jobs;</li>
 * <li>exceptions are translated into {@link ProcessingException}s;</li>
 * <li>have job relevant data bound to ThreadLocals: {@link IJob#CURRENT}, {@link JobContext#CURRENT},
 * {@link ISession#CURRENT}, {@link NlsLocale#CURRENT}, {@link ScoutTexts#CURRENT};</li>
 * </ul>
 *
 * @param <R>
 *          the result type of the job's computation; use {@link Void} in combination with {@link #onRunVoid()} if this
 *          job does not return a result.
 * @see IJob
 * @see Job
 * @see JobManager
 * @since 5.1
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

  @Override
  protected Callable<R> interceptCallable(final Callable<R> next) {
    final Callable<R> p3 = new ThreadLocalInitializer<>(next, ScoutTexts.CURRENT, m_clientSession.getTexts());
    final Callable<R> p2 = new ThreadLocalInitializer<>(p3, NlsLocale.CURRENT, m_clientSession.getLocale());
    final Callable<R> p1 = new ThreadLocalInitializer<>(p2, ISession.CURRENT, m_clientSession);
    final Callable<R> head = super.interceptCallable(p1);

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
